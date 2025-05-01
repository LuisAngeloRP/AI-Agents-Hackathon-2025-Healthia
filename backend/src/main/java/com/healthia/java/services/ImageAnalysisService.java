package com.healthia.java.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.healthia.java.models.*;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.*;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ImageAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(ImageAnalysisService.class);
    private static final String PLATES_S3_FOLDER = "platos_ia"; // Define S3 folder for plates

    private final OpenAIClient openAIClient;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    @Value("${app.openai.model}")
    private String chatModelName;

    @Value("${app.user-data-dir:data_usuario}")
    private String dataDir;
    @Value("${app.image-analysis-persistence-file:analyses.json}")
    private String persistenceFile;

    private Path jsonFilePath;

    // In-memory cache / store
    private final Map<Integer, ImageAnalysisHistoryResponse> analyses = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    @Autowired
    public ImageAnalysisService(OpenAIClient openAIClient, S3Service s3Service) {
        this.openAIClient = openAIClient;
        this.s3Service = s3Service;
        // Configure ObjectMapper for Java Time API
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @PostConstruct
    private void initialize() {
        try {
            Path dataPath = Paths.get(dataDir);
            Files.createDirectories(dataPath);
            jsonFilePath = dataPath.resolve(persistenceFile);
            log.info("Image analysis persistence file path: {}", jsonFilePath);
            loadAnalysesFromJso();
        } catch (IOException e) {
            log.error("Failed to create data directory or set JSON file path: {}", dataDir, e);
        }
    }

    // --- Core Analysis Logic ---

    public AnalisisPlato analyzeImage(
            String imageBase64,
            Integer analysisId,
            byte[] mediaContent,
            String originalFilename
    ) {
        long startTime = System.currentTimeMillis();
        log.info("Starting image analysis. Provided ID: {}", analysisId);

        final int currentAnalysisId = determineAndReserveAnalysisId(analysisId);
        log.info("Using Analysis ID: {}", currentAnalysisId);

        String originalImageUrl = null;
        String processedImageUrl = null;
        ImageAnalysisHistoryResponse historyResponse = null;

        try {
            byte[] imageData;
            String fileExtension;

            if (mediaContent != null && mediaContent.length > 0) {
                log.debug("Processing image from byte array (mediaContent)");
                imageData = mediaContent;
                fileExtension = determineExtension(originalFilename, "jpg");
            } else if (imageBase64 != null && !imageBase64.isEmpty()) {
                log.debug("Processing image from base64 string");
                try {
                    // Handle potential data URI prefix (e.g., data:image/jpeg;base64,...)
                    String base64Data = imageBase64.contains(",") ? imageBase64.substring(imageBase64.indexOf(",") + 1) : imageBase64;
                    imageData = Base64.getDecoder().decode(base64Data);
                    // Try to determine extension from data URI if filename is missing
                    fileExtension = determineExtension(originalFilename, extractExtensionFromDataUri(imageBase64, "jpg"));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid base64 image data provided.", e);
                }
            } else {
                throw new IllegalArgumentException("No image data provided (mediaContent or imageBase64 is required).");
            }

            // Validate image format and get dimensions
            BufferedImage originalImage = validateAndReadImage(imageData);
            ImageDimensions dimensions = new ImageDimensions(originalImage.getWidth(), originalImage.getHeight());

            // 1. Upload original image to S3
            originalImageUrl = uploadToS3(imageData, fileExtension, currentAnalysisId, originalFilename, "original");

            // 2. Call OpenAI Vision API
            String openAIResponseJson = callOpenAIVision(imageData, fileExtension, dimensions);

            // 3. Parse and validate OpenAI response
            AnalisisPlato analysisResult = parseAndValidateAnalysis(openAIResponseJson, dimensions);
            analysisResult.setImagenOriginalUrl(originalImageUrl);

            // 4. Draw analysis on image
            byte[] processedImageData = drawAnalysisOnImage(imageData, analysisResult, dimensions);

            // 5. Upload processed image to S3
            processedImageUrl = uploadToS3(processedImageData, "jpg", currentAnalysisId, originalFilename, "processed");
            analysisResult.setImagenProcesadaUrl(processedImageUrl);

            // 6. Store result
            historyResponse = ImageAnalysisHistoryResponse.builder()
                    .id(currentAnalysisId)
                    .fecha(OffsetDateTime.now(ZoneOffset.UTC))
                    .analisis(analysisResult)
                    .imagenOriginalUrl(originalImageUrl)
                    .imagenProcesadaUrl(processedImageUrl)
                    .build();

            analyses.put(currentAnalysisId, historyResponse);
            saveAnalysesToJson(); // Persist changes

            log.info("Image analysis successful for ID {}. Duration: {} ms", currentAnalysisId, System.currentTimeMillis() - startTime);
            return analysisResult;

        } catch (Exception e) {
            log.error("Image analysis failed for ID {}: {}", currentAnalysisId, e.getMessage(), e);
            // Attempt to clean up S3 resources if urls were generated
            cleanupS3OnError(originalImageUrl, processedImageUrl);
            // Remove potentially incomplete entry from memory if added
            analyses.remove(currentAnalysisId);
            // Re-throw a more specific exception or handle as needed
            // For now, wrap in RuntimeException to signal failure
            throw new RuntimeException("Error durante el análisis de imagen: " + e.getMessage(), e);
        }
    }

    // --- Persistence Methods (JSON File) ---

    private synchronized void loadAnalysesFromJso() {
        if (jsonFilePath == null || !Files.exists(jsonFilePath)) {
            log.warn("Persistence file not found: {}. Starting with empty analysis map.", jsonFilePath);
            return;
        }
        try {
            Map<String, Object> data = objectMapper.readValue(jsonFilePath.toFile(), new TypeReference<>() {});
            Map<String, ImageAnalysisHistoryResponse> loadedAnalysesMap = objectMapper.convertValue(
                    data.getOrDefault("analyses", Collections.emptyMap()),
                    new TypeReference<Map<String, ImageAnalysisHistoryResponse>>() {}
            );

            analyses.clear();
            loadedAnalysesMap.forEach((idStr, analysis) -> analyses.put(Integer.parseInt(idStr), analysis));

            int maxId = analyses.keySet().stream().max(Integer::compareTo).orElse(0);
            int storedNextId = ((Number) data.getOrDefault("next_id", 1)).intValue();
            nextId.set(Math.max(maxId + 1, storedNextId));

            log.info("Loaded {} analyses from JSON. Next ID set to {}.", analyses.size(), nextId.get());

        } catch (IOException e) {
            log.error("Failed to load analyses from {}: {}", jsonFilePath, e.getMessage(), e);
            // Continue with empty map if load fails
            analyses.clear();
            nextId.set(1);
        }
    }

    private synchronized void saveAnalysesToJson() {
        if (jsonFilePath == null) {
             log.error("JSON file path not initialized. Cannot save analyses.");
             return;
        }
        try {
            Map<String, ImageAnalysisHistoryResponse> dataToSave = new HashMap<>();
            analyses.forEach((id, analysis) -> dataToSave.put(String.valueOf(id), analysis));

            Map<String, Object> fileData = Map.of(
                    "analyses", dataToSave,
                    "next_id", nextId.get(),
                    "last_updated", OffsetDateTime.now(ZoneOffset.UTC).toString()
            );

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFilePath.toFile(), fileData);
            log.debug("Saved {} analyses to {}", analyses.size(), jsonFilePath);
        } catch (IOException e) {
            log.error("Failed to save analyses to {}: {}", jsonFilePath, e.getMessage(), e);
        }
    }

    public boolean analysisExists(int analysisId) {
        // Check in-memory map first for performance
        return analyses.containsKey(analysisId);
        // Optionally re-read from JSON for absolute certainty, but map should be source of truth
    }

    public boolean deleteAnalysis(int analysisId) {
        log.info("Attempting to delete analysis ID: {}", analysisId);
        ImageAnalysisHistoryResponse removedAnalysis = analyses.remove(analysisId);

        if (removedAnalysis != null) {
            log.info("Removed analysis ID {} from memory.", analysisId);
            saveAnalysesToJson(); // Persist removal

            // Delete S3 folder associated with the analysis
            String s3FolderPath = String.format("%s/%d", PLATES_S3_FOLDER, analysisId);
            try {
                 Map<String, Object> deleteResult = s3Service.deleteFolderFromS3(s3FolderPath);
                 if (Boolean.TRUE.equals(deleteResult.get("success"))) {
                     log.info("Successfully deleted S3 folder: {}", s3FolderPath);
                 } else {
                      log.warn("Failed to delete S3 folder '{}': {}", s3FolderPath, deleteResult.get("error"));
                      // Continue deletion of analysis record even if S3 cleanup fails partially
                 }
            } catch (Exception e) {
                 log.error("Error deleting S3 folder '{}' for analysis ID {}: {}", s3FolderPath, analysisId, e.getMessage(), e);
                 // Continue deletion of analysis record
            }
            return true;
        } else {
            log.warn("Analysis ID {} not found for deletion.", analysisId);
            return false;
        }
    }

    public ImageAnalysisHistoryResponse getAnalysisHistory(int analysisId) {
        return analyses.get(analysisId);
    }

    public List<ImageAnalysisHistoryResponse> getAllAnalyses() {
        return new ArrayList<>(analyses.values());
    }

    public Map<String, Object> debugAnalysesState() {
        Map<String, Object> debugInfo = new LinkedHashMap<>();
        debugInfo.put("total_analyses_in_memory", analyses.size());
        debugInfo.put("next_id", nextId.get());
        debugInfo.put("analysis_ids_in_memory", new ArrayList<>(analyses.keySet()));
        debugInfo.put("json_file_path", jsonFilePath != null ? jsonFilePath.toString() : "Not Initialized");
        if (jsonFilePath != null && Files.exists(jsonFilePath)) {
             try {
                 debugInfo.put("json_file_exists", true);
                 debugInfo.put("json_file_size_bytes", Files.size(jsonFilePath));
                 debugInfo.put("json_file_last_modified", Files.getLastModifiedTime(jsonFilePath).toString());
             } catch (IOException e) {
                  log.warn("Could not read JSON file metadata: {}", e.getMessage());
                  debugInfo.put("json_file_error", e.getMessage());
             }
        } else {
             debugInfo.put("json_file_exists", false);
        }
        return debugInfo;
    }

    // --- Helper Methods ---

    private int determineAndReserveAnalysisId(Integer requestedId) {
        if (requestedId != null) {
            if (!analyses.containsKey(requestedId)) {
                // Try to use requested ID if available and update nextId if needed
                nextId.accumulateAndGet(requestedId + 1, Math::max);
                return requestedId;
            } else {
                log.warn("Requested Analysis ID {} already exists, generating a new one.", requestedId);
                // Fall through to generate new ID if requested one is taken
            }
        }
        // Generate new ID
        return nextId.getAndIncrement();
    }

    private String determineExtension(String filename, String defaultExt) {
        if (filename != null) {
            int dotIndex = filename.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < filename.length() - 1) {
                return filename.substring(dotIndex + 1).toLowerCase();
            }
        }
        return defaultExt;
    }

     private String extractExtensionFromDataUri(String dataUri, String defaultExt) {
        if (dataUri == null || !dataUri.startsWith("data:image/")) {
            return defaultExt;
        }
        int mimeEnd = dataUri.indexOf(';');
        if (mimeEnd == -1) mimeEnd = dataUri.indexOf(','); // If no ;base64 part
        if (mimeEnd == -1) return defaultExt;

        String mimeType = dataUri.substring("data:image/".length(), mimeEnd);
        return switch (mimeType.toLowerCase()) {
            case "jpeg", "jpg" -> "jpg";
            case "png" -> "png";
            case "gif" -> "gif";
            default -> defaultExt;
        };
    }

    private BufferedImage validateAndReadImage(byte[] imageData) throws IOException {
        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("Image data is empty.");
        }
        // Max size check (e.g., 20MB)
        if (imageData.length > 20 * 1024 * 1024) {
             throw new IllegalArgumentException("Image size exceeds the limit (20MB).");
        }
        try (InputStream is = new ByteArrayInputStream(imageData)) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                throw new IOException("Could not decode image data. Invalid format?");
            }
            log.info("Image validated: {}x{}, Type: {}", image.getWidth(), image.getHeight(), image.getType());
            return image;
        } catch (IOException e) {
             log.error("Failed to read image data: {}", e.getMessage());
             throw e;
        }
    }

    private String uploadToS3(byte[] data, String extension, int analysisId, String originalFilename, String type) {
        String filename = String.format("%s_%s.%s",
                type,
                (originalFilename != null ? Paths.get(originalFilename).getFileName().toString().replaceFirst("[.][^.]+$", "") : "image"),
                extension);

        Map<String, Object> s3Result = s3Service.uploadFileToS3(
                new ByteArrayInputStream(data),
                data.length,
                extension,
                analysisId,
                filename, // Use constructed filename
                PLATES_S3_FOLDER // Specific folder for plates
        );

        if (Boolean.TRUE.equals(s3Result.get("success"))) {
            return (String) s3Result.get("url");
        } else {
            throw new RuntimeException("Failed to upload " + type + " image to S3: " + s3Result.get("error"));
        }
    }

     private String callOpenAIVision(byte[] imageData, String extension, ImageDimensions dimensions) {
        String base64Image = Base64.getEncoder().encodeToString(imageData);
        String dataUri = String.format("data:image/%s;base64,%s", extension, base64Image);

        String systemPrompt = getPrompt(dimensions);

        ImageUrl imageUrl = ImageUrl.builder().url(dataUri).build();
        List<UserMessageContentPart> userContent = List.of(
                UserMessageContentPartImage.builder().imageUrl(imageUrl).build()
        );

         List<ChatCompletionMessageParam> messages = List.of(
                SystemMessage.builder().content(systemPrompt).build(),
                UserMessage.builder().content(userContent).build()
         );

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(chatModelName)
                .messages(messages)
                .maxTokens(1500) // Increased token limit for potentially detailed JSON
                .responseFormat(ChatCompletionResponseFormat.JSON_OBJECT)
                .build();

        log.info("Sending request to OpenAI Vision API (Model: {})...", chatModelName);
        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            String responseContent = completion.choices().get(0).message().content();
            log.info("Received response from OpenAI Vision API.");
            log.debug("OpenAI Raw Response: {}", responseContent);
            if (responseContent == null || responseContent.isBlank()) {
                throw new RuntimeException("OpenAI returned an empty response.");
            }
            return responseContent;
        } catch (Exception e) {
            log.error("Error calling OpenAI Vision API: {}", e.getMessage(), e);
            throw new RuntimeException("Error communicating with OpenAI: " + e.getMessage(), e);
        }
    }

     private AnalisisPlato parseAndValidateAnalysis(String jsonResponse, ImageDimensions dimensions) {
        try {
            AnalisisPlato analysis = objectMapper.readValue(jsonResponse, AnalisisPlato.class);

            // --- Validation and Normalization --- 
            if (analysis.getDetalleAlimentos() == null) {
                analysis.setDetalleAlimentos(new ArrayList<>());
            }
            if (analysis.getRecomendaciones() == null) {
                 analysis.setRecomendaciones(new ArrayList<>());
            }

             // Normalize coordinates and recalculate area percentages based on normalized coords
             double totalArea = (double) dimensions.getWidth() * dimensions.getHeight();
             double calculatedSumPercentages = 0.0;
             if (totalArea > 0) {
                 for (DetalleAlimento item : analysis.getDetalleAlimentos()) {
                     if (item.getCoordenadas() != null) {
                         Coordenadas normCoords = normalizeCoordinates(item.getCoordenadas(), dimensions.getWidth(), dimensions.getHeight());
                         item.setCoordenadas(normCoords);
                         // Recalculate area based on normalized integer coordinates
                         double itemArea = (double) (normCoords.getX2() - normCoords.getX1()) * (normCoords.getY2() - normCoords.getY1());
                         item.setPorcentajeArea( (itemArea / totalArea) * 100.0 );
                         calculatedSumPercentages += item.getPorcentajeArea();
                     } else {
                         log.warn("Alimento '{}' missing coordinates, setting area to 0.", item.getNombre());
                         item.setPorcentajeArea(0.0);
                     }
                 }
             }

             log.info("Calculated percentage sum after coordinate normalization: {}", calculatedSumPercentages);

            // Normalize percentages to sum to 100% if needed
            if (!analysis.getDetalleAlimentos().isEmpty() && Math.abs(calculatedSumPercentages - 100.0) > 0.1) {
                log.warn("Percentages do not sum to 100% ({}). Normalizing...", calculatedSumPercentages);
                double factor = (calculatedSumPercentages > 0) ? 100.0 / calculatedSumPercentages : 0;
                double normalizedSum = 0.0;
                for (DetalleAlimento item : analysis.getDetalleAlimentos()) {
                    item.setPorcentajeArea(item.getPorcentajeArea() * factor);
                    normalizedSum += item.getPorcentajeArea();
                }
                 // Final adjustment for precision
                if (!analysis.getDetalleAlimentos().isEmpty() && Math.abs(normalizedSum - 100.0) > 0.01) {
                    double diff = 100.0 - normalizedSum;
                    analysis.getDetalleAlimentos().get(0).setPorcentajeArea(analysis.getDetalleAlimentos().get(0).getPorcentajeArea() + diff);
                }
            }

            // Recalculate category percentages
            double verdurasPct = 0, proteinasPct = 0, carbohidratosPct = 0;
            for (DetalleAlimento item : analysis.getDetalleAlimentos()) {
                String category = item.getCategoria();
                double area = item.getPorcentajeArea();
                if ("Verduras/vegetales".equalsIgnoreCase(category)) verdurasPct += area;
                else if ("Proteínas".equalsIgnoreCase(category)) proteinasPct += area;
                else if ("Carbohidratos".equalsIgnoreCase(category)) carbohidratosPct += area;
            }
            analysis.setPorcentajeVerduras(verdurasPct);
            analysis.setPorcentajeProteinas(proteinasPct);
            analysis.setPorcentajeCarbohidratos(carbohidratosPct);
            log.info("Final Category Percentages: Verduras={}, Proteinas={}, Carbs={}", verdurasPct, proteinasPct, carbohidratosPct);

            // Validate recommendations (ensure 3 if applicable, check length)
             boolean isFood = !"No aplicable".equalsIgnoreCase(analysis.getEvaluacionGeneral());
             List<String> recommendations = analysis.getRecomendaciones();
             if (isFood) {
                 while (recommendations.size() < 3) {
                     recommendations.add("Recomendación genérica: Asegúrate de incluir variedad de alimentos en tu dieta.");
                 }
             } // If not food, keep whatever recommendations OpenAI provided (should be 0-1)
             // Truncate long recommendations
             List<String> finalRecommendations = recommendations.stream()
                     .map(rec -> rec.length() > 200 ? rec.substring(0, 197) + "..." : rec)
                     .limit(3) // Ensure max 3, especially if not food
                     .collect(Collectors.toList());
            analysis.setRecomendaciones(finalRecommendations);

            log.info("Successfully parsed and validated OpenAI response.");
            return analysis;

        } catch (IOException e) {
             log.error("Failed to parse JSON response from OpenAI: {}", jsonResponse, e);
             throw new RuntimeException("Error parsing analysis result: " + e.getMessage(), e);
        } catch (Exception e) { // Catch other potential errors during validation
            log.error("Error during validation of analysis result: {}", e.getMessage(), e);
            throw new RuntimeException("Error validating analysis result: " + e.getMessage(), e);
        }
     }

    private Coordenadas normalizeCoordinates(Coordenadas coords, int imageWidth, int imageHeight) {
        if (coords == null) return Coordenadas.builder().x1(0).y1(0).x2(0).y2(0).build();
        int x1 = Math.max(0, Math.min(coords.getX1(), imageWidth - 1));
        int y1 = Math.max(0, Math.min(coords.getY1(), imageHeight - 1));
        int x2 = Math.max(x1, Math.min(coords.getX2(), imageWidth - 1)); // Ensure x2 >= x1
        int y2 = Math.max(y1, Math.min(coords.getY2(), imageHeight - 1)); // Ensure y2 >= y1
        return Coordenadas.builder().x1(x1).y1(y1).x2(x2).y2(y2).build();
    }

    private byte[] drawAnalysisOnImage(byte[] originalImageData, AnalisisPlato analysis, ImageDimensions dimensions) throws IOException {
        log.info("Drawing analysis results onto image...");
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(originalImageData));
        if (image == null) throw new IOException("Could not read image data for drawing.");

        // Ensure image is in a drawable format (ARGB for transparency)
        BufferedImage drawableImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = drawableImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);

        // Configure graphics settings
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f)); // Transparency

        Font font = loadFont(16); // Load font (adjust size as needed)

        for (DetalleAlimento item : analysis.getDetalleAlimentos()) {
            Coordenadas coords = item.getCoordenadas();
            if (coords == null || coords.getX2() <= coords.getX1() || coords.getY2() <= coords.getY1()) continue;

            Color color = getColorForCategory(item.getCategoria());
            g2d.setColor(color);

            // Draw semi-transparent rectangle
            g2d.fillRect(coords.getX1(), coords.getY1(), coords.getX2() - coords.getX1(), coords.getY2() - coords.getY1());

            // Draw label (less transparent text/background)
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f)); // Less transparency for label
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics();
            String label = String.format("%s (%.1f%%)", item.getNombre(), item.getPorcentajeArea());
            int textWidth = fm.stringWidth(label);
            int textHeight = fm.getHeight();
            int textX = coords.getX1() + 2;
            int textY = coords.getY1() + fm.getAscent() + 2;

            // Simple background for text
            g2d.setColor(Color.BLACK);
            g2d.fillRect(textX - 1, textY - fm.getAscent() - 1, textWidth + 4, textHeight + 2);

            g2d.setColor(color); // Text color same as box color
            g2d.drawString(label, textX, textY);

            // Reset composite for next rectangle
             g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        }

        g2d.dispose();

        // Convert BufferedImage back to byte array (JPEG)
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(drawableImage, "jpg", baos);
            log.info("Successfully drew analysis on image.");
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to write processed image to byte array: {}", e.getMessage(), e);
            throw e;
        }
    }

     private Font loadFont(int size) {
         // Try loading a specific font, fall back to default
         try {
             // Assuming arial.ttf is in src/main/resources or classpath
             InputStream fontStream = new ClassPathResource("arial.ttf").getInputStream();
             Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
             return baseFont.deriveFont(Font.PLAIN, size);
         } catch (FontFormatException | IOException e) {
             log.warn("Could not load custom font arial.ttf: {}. Using default font.", e.getMessage());
             return new Font(Font.SANS_SERIF, Font.PLAIN, size);
         }
     }

    private Color getColorForCategory(String categoria) {
        if (categoria == null) return Color.GRAY;
        return switch (categoria.toLowerCase()) {
            case "verduras/vegetales" -> new Color(76, 175, 80); // Green
            case "proteínas" -> new Color(244, 67, 54); // Red
            case "carbohidratos" -> new Color(255, 193, 7); // Yellow
            default -> Color.GRAY;
        };
    }

     private String getPrompt(ImageDimensions dimensions) {
        // Using f-string equivalent with String.format
         return String.format("""
            Eres un sistema avanzado de visión por computadora especializado en el análisis nutricional de imágenes.

            INFORMACIÓN DE LA IMAGEN:
            - Dimensiones: %dx%d píxeles
            - Área total: %d píxeles cuadrados

            TAREA:
            Analiza la siguiente imagen de un plato de comida y proporciona:

            1. Lista de alimentos detectados:
               Identifica cada alimento visible en el plato con la mayor precisión posible.

            2. Cálculo de área ocupada:
               Para cada alimento, calcula:
               - Coordenadas precisas (x1, y1, x2, y2) del rectángulo que lo contiene
               - Porcentaje aproximado del área total del plato que ocupa

               Donde:
               - x1, y1: coordenadas exactas de la esquina superior izquierda
               - x2, y2: coordenadas exactas de la esquina inferior derecha
               - Porcentaje de área = (área del alimento / área total de la imagen) * 100

               Validaciones:
               * La suma de porcentajes de todos los alimentos debe ser EXACTAMENTE 100%%
               * Las coordenadas deben ser precisas y ajustarse exactamente al contorno del alimento
               * Evita solapamiento significativo entre áreas de diferentes alimentos

            3. Clasificación de alimentos según el Plato de Harvard:
               Categoriza cada alimento en una de estas categorías:
               - Verduras/vegetales (objetivo ideal: 50%% del plato)
               - Proteínas (objetivo ideal: 25%% del plato)
               - Carbohidratos (objetivo ideal: 25%% del plato)

               IMPORTANTE:
               * El porcentaje de verduras es la suma de los porcentajes de todos los alimentos clasificados como "Verduras/vegetales"
               * El porcentaje de proteínas es la suma de los porcentajes de todos los alimentos clasificados como "Proteínas"
               * El porcentaje de carbohidratos es la suma de los porcentajes de todos los alimentos clasificados como "Carbohidratos"

            4. Evaluación del plato:
               Basado en las proporciones reales detectadas:
               - "Plato saludable": Si las proporciones están dentro de ±10%% de los objetivos del Plato de Harvard
               - "Plato desequilibrado": Si alguna proporción se desvía más del 10%%

            5. Recomendaciones nutricionales personalizadas:
               PRIMERO: Analiza cuidadosamente si la imagen realmente muestra un plato de comida. Si no es comida o no tiene sentido dar recomendaciones nutricionales, proporciona observaciones adecuadas al contexto.

               Si ES un plato de comida, proporciona hasta 3 recomendaciones altamente personalizadas:

               - Observa el tipo específico de comida (por ejemplo, desayuno, almuerzo, cena, snack, plato típico específico)
               - Identifica el estilo culinario (mediterráneo, asiático, latinoamericano, etc.) y adapta tus recomendaciones
               - Analiza el equilibrio del plato según lo que realmente se ve, no te inventes alimentos que no estén visibles
               - Si el plato ya está bien equilibrado, no fuerces recomendaciones negativas; puedes reforzar lo positivo

               EJEMPLOS de recomendaciones personalizadas:
               - "El arroz integral que has elegido es excelente, pero ocupa el 45%% del plato. Reduce la porción a 1/4 e incrementa las verduras para mejor equilibrio."
               - "Tu plato de pasta contiene buena proteína, pero añade más vegetales de colores variados (50g de espinacas y 30g de pimientos) para aumentar nutrientes."
               - "Este desayuno tiene buena proteína del huevo, añade 1/2 aguacate y cambia el pan blanco por integral para mejorar grasas saludables y fibra."

               Las recomendaciones deben:
               - Ser ultrapersonalizadas, específicas para ESTA imagen concreta, no genéricas
               - Mencionar ingredientes exactos que se ven en la imagen
               - Sugerir mejoras con cantidades concretas
               - Adaptarse al tipo de comida/plato específico
               - Si el plato está bien equilibrado, reconócelo y refuerza los aspectos positivos
               - Cada recomendación debe tener MÁXIMO 200 caracteres
               - NO dar recomendaciones si no es comida o no tiene sentido nutricional

            FORMATO DE RESPUESTA:
            Responde ÚNICAMENTE en formato JSON con esta estructura exacta:
            {
              "evaluacion_general": "Plato saludable" o "Plato desequilibrado" o "No aplicable" (si no es comida),
              "porcentaje_verduras": número entre 0 y 100 (suma de todos los alimentos en esta categoría),
              "porcentaje_proteinas": número entre 0 y 100 (suma de todos los alimentos en esta categoría),
              "porcentaje_carbohidratos": número entre 0 y 100 (suma de todos los alimentos en esta categoría),
              "detalle_alimentos": [
                {{
                  "nombre": "nombre del alimento",
                  "categoria": "Verduras/vegetales|Proteínas|Carbohidratos",
                  "porcentaje_area": número entre 0.1 y 100.0,
                  "coordenadas": {{
                    "x1": entero entre 0 y %d,
                    "y1": entero entre 0 y %d,
                    "x2": entero entre x1 y %d,
                    "y2": entero entre y1 y %d
                  }}
                }}
              ],
              "recomendaciones": [
                "Primera recomendación ultrapersonalizada relacionada con los alimentos visibles",
                "Segunda recomendación ultrapersonalizada relacionada con los alimentos visibles",
                "Tercera recomendación ultrapersonalizada relacionada con los alimentos visibles"
              ]
            }

            IMPORTANTE:
            - Las coordenadas DEBEN ser números enteros precisos dentro de los límites especificados
            - Los porcentajes de área de todos los alimentos DEBEN sumar EXACTAMENTE 100%% (muy importante)
            - Si la imagen NO contiene comida o alimentos reconocibles, marca "No aplicable" en evaluación, 0%% en porcentajes y proporciona observaciones adecuadas en recomendaciones
            - Cada recomendación debe ser extremadamente específica a la imagen analizada, mencionando exactamente lo que ves
            - No sigas un patrón mecánico, adapta tus recomendaciones al tipo de plato específico (desayuno, almuerzo, etc.)
            - Si el plato ya tiene buenas proporciones, reconócelo en vez de inventar problemas
            - Cada recomendación debe tener un MÁXIMO de 200 caracteres
            """,
             dimensions.getWidth(), dimensions.getHeight(), (long)dimensions.getWidth() * dimensions.getHeight(),
             dimensions.getWidth() - 1, dimensions.getHeight() - 1, dimensions.getWidth() - 1, dimensions.getHeight() - 1);
    }

    private void cleanupS3OnError(String originalUrl, String processedUrl) {
        if (originalUrl != null) {
            try {
                log.warn("Cleaning up original S3 image due to error: {}", originalUrl);
                s3Service.deleteFileFromS3(originalUrl);
            } catch (Exception e) {
                log.error("Failed to cleanup original S3 image '{}': {}", originalUrl, e.getMessage());
            }
        }
         if (processedUrl != null) {
            try {
                log.warn("Cleaning up processed S3 image due to error: {}", processedUrl);
                s3Service.deleteFileFromS3(processedUrl);
            } catch (Exception e) {
                log.error("Failed to cleanup processed S3 image '{}': {}", processedUrl, e.getMessage());
            }
        }
        // Also consider deleting the folder if an ID was reserved but analysis failed completely
    }
} 