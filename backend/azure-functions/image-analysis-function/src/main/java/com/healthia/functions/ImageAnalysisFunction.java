package com.healthia.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthia.functions.entities.ImageAnalysisHistoryEntity;
import com.healthia.functions.repositories.ImageAnalysisHistoryRepository;
import com.healthia.functions.util.ImageAnalysisDtoConverter;
import com.healthia.java.models.*;
import com.healthia.java.services.BlobStorageService; 
import com.openai.client.OpenAIClient; 

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.*;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.microsoft.azure.functions.spring.SpringbootFunctionApplication;

@SpringBootApplication
@org.springframework.boot.autoconfigure.domain.EntityScan("com.healthia.functions.entities")
@org.springframework.data.jpa.repository.config.EnableJpaRepositories("com.healthia.functions.repositories")
@org.springframework.context.annotation.ComponentScan({
    "com.healthia.functions", 
    "com.healthia.java.services", // For BlobStorageService
    "com.openai.client", // Assuming OpenAIClient is here or auto-configured
    "com.healthia.config" // Or wherever OpenAIClient/BlobStorageService might be configured by user
})
public class ImageAnalysisFunction extends SpringbootFunctionApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ImageAnalysisFunction.class, args);
    }
}

/**
 * Azure Functions for Image Analysis. 
 * This component will contain the HTTP Triggers.
 */
@Component
class FunctionHandlers {

    private static final String PLATES_BLOB_FOLDER = "platos_ia_func";

    @Autowired
    private OpenAIClient openAIClient;

    @Autowired
    private BlobStorageService blobStorageService;

    @Autowired
    private ImageAnalysisHistoryRepository imageAnalysisHistoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${OpenAI.Model}")
    private String chatModelName;
    
    @Value("${app.openai.prompt.image_analysis}") // Assuming prompt is in config
    private String imageAnalysisPromptTemplate;

    @FunctionName("analyzeImage")
    @Transactional
    public HttpResponseMessage analyzeImage(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "analyze-image")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        context.getLogger().info("Java HTTP trigger processed a request for analyzeImage.");

        try {
            String requestBody = request.getBody().orElse(null);
            if (requestBody == null || requestBody.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Request body is empty.").build();
            }

            ImageAnalysisRequest analysisRequest = objectMapper.readValue(requestBody, ImageAnalysisRequest.class);
            if (analysisRequest.getImageBase64() == null || analysisRequest.getImageBase64().isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Field 'imageBase64' is mandatory.").build();
            }

            Integer requestedId = analysisRequest.getConversationId();
            context.getLogger().info("Starting image analysis. Requested ID (optional): " + requestedId);

            String imageBase64 = analysisRequest.getImageBase64();
            byte[] imageData;
            String fileExtension;
            String originalFilename = "image_from_base64.jpg"; 

            try {
                String base64Data = imageBase64.contains(",") ? imageBase64.substring(imageBase64.indexOf(",") + 1) : imageBase64;
                imageData = Base64.getDecoder().decode(base64Data);
                fileExtension = extractExtensionFromDataUri(imageBase64, "jpg");
                originalFilename = "image_from_base64." + fileExtension;
            } catch (IllegalArgumentException e) {
                context.getLogger().log(Level.WARNING, "Invalid base64 image data: " + e.getMessage(), e);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Invalid base64 image data provided.").build();
            }

            BufferedImage originalImage = validateAndReadImage(imageData, context);
            ImageDimensions dimensions = new ImageDimensions(originalImage.getWidth(), originalImage.getHeight());

            String originalImageUrl = uploadToBlob(imageData, fileExtension, requestedId, originalFilename, "original", context);
            String openAIResponseJson = callOpenAIVision(imageData, fileExtension, dimensions, context);
            AnalisisPlato parsedAnalysisDto = parseAndValidateOpenAIResponse(openAIResponseJson, dimensions, objectMapper, context);
            
            // Update DTO with URLs from Blob storage and other details
            parsedAnalysisDto.setImagenOriginalUrl(originalImageUrl);
             // The ID and Fecha for AnalisisPlato DTO will be set based on the history entry later
            if (requestedId != null) parsedAnalysisDto.setId(requestedId); // Set if provided
            parsedAnalysisDto.setFecha(OffsetDateTime.now(ZoneOffset.UTC)); // Set current time


            byte[] processedImageData = drawAnalysisOnImage(imageData, parsedAnalysisDto, dimensions, context);
            String processedImageUrl = uploadToBlob(processedImageData, "jpg", requestedId, originalFilename, "processed", context);
            parsedAnalysisDto.setImagenProcesadaUrl(processedImageUrl);

            ImageAnalysisHistoryResponse historyDtoForConversion = ImageAnalysisHistoryResponse.builder()
                .id(requestedId) 
                .fecha(parsedAnalysisDto.getFecha() != null ? parsedAnalysisDto.getFecha() : OffsetDateTime.now(ZoneOffset.UTC))
                .analisis(parsedAnalysisDto) 
                .imagenOriginalUrl(originalImageUrl) 
                .imagenProcesadaUrl(processedImageUrl)
                .evaluacionGeneral(parsedAnalysisDto.getEvaluacionGeneral())
                .nutricion(parsedAnalysisDto.getNutricion())
                .detallesAlimentos(parsedAnalysisDto.getDetallesAlimentos())
                .recomendaciones(parsedAnalysisDto.getRecomendaciones())
                .build();    
            
            ImageAnalysisHistoryEntity historyEntityToSave = ImageAnalysisDtoConverter.convertImageAnalysisHistoryResponseToEntity(historyDtoForConversion);

            if (requestedId != null) {
                 if (imageAnalysisHistoryRepository.existsById(requestedId)) {
                     context.getLogger().log(Level.WARNING, "Analysis with requested ID " + requestedId + " already exists. Overwriting.");
                     // Fetch existing to ensure JPA updates correctly, or rely on save to overwrite based on ID.
                     // For simplicity, save() will perform an update if ID exists.
                 }
                 historyEntityToSave.setId(requestedId);
                 if (historyEntityToSave.getAnalisis() != null) {
                    // historyEntityToSave.getAnalisis().setId(Long.valueOf(requestedId)); // This is tricky with @MapsId if AnalisisPlato ID must also be this
                 }
            } else {
                 // If no requestedId, AnalisisPlatoEntity gets new ID from DB.
                 // ImageAnalysisHistoryEntity gets this ID via @MapsId. So, historyEntityToSave.id should be null here.
                 historyEntityToSave.setId(null); 
            }

            ImageAnalysisHistoryEntity savedEntity = imageAnalysisHistoryRepository.save(historyEntityToSave);
            ImageAnalysisHistoryResponse responseDto = ImageAnalysisDtoConverter.convertImageAnalysisHistoryEntityToDto(savedEntity);

            return request.createResponseBuilder(HttpStatus.OK).body(objectMapper.writeValueAsString(responseDto)).build();

        } catch (IOException e) {
            context.getLogger().log(Level.SEVERE, "IO Error during image analysis: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("IO error: " + e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            context.getLogger().log(Level.WARNING, "Bad request during image analysis: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Bad request: " + e.getMessage()).build();
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "General Error during image analysis: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage()).build();
        }
    }

    private BufferedImage validateAndReadImage(byte[] imageData, ExecutionContext context) throws IOException {
        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("Image data is empty.");
        }
        if (imageData.length > 20 * 1024 * 1024) { 
             throw new IllegalArgumentException("Image size exceeds the limit (20MB).");
        }
        try (InputStream is = new ByteArrayInputStream(imageData)) {
            BufferedImage image = javax.imageio.ImageIO.read(is);
            if (image == null) {
                throw new IOException("Could not decode image data. Invalid format?");
            }
            context.getLogger().info(String.format("Image validated: %dx%d, Type: %d", image.getWidth(), image.getHeight(), image.getType()));
            return image;
        } catch (IOException e) {
             context.getLogger().log(Level.SEVERE, "Failed to read image data: " + e.getMessage(), e);
             throw e;
        }
    }

    private String uploadToBlob(byte[] data, String extension, Integer analysisId, String originalFilename, String type, ExecutionContext context) {
        String filename = String.format("%s_%s_%s.%s",
                type,
                (analysisId != null ? String.valueOf(analysisId) : "new"), // Use "new" or a timestamp if ID is not yet known for new analyses
                Paths.get(originalFilename).getFileName().toString().replaceFirst("[.][^.]+$", ""),
                extension);

        Map<String, Object> blobResult = blobStorageService.uploadFileToBlob(
                new ByteArrayInputStream(data),
                data.length,
                extension,
                analysisId != null ? analysisId : 0, // Folder ID, 0 or a temp ID if not known
                filename, 
                PLATES_BLOB_FOLDER 
        );

        if (Boolean.TRUE.equals(blobResult.get("success"))) {
            String url = (String) blobResult.get("url");
            context.getLogger().info("Uploaded " + type + " image to Blob: " + url);
            return url;
        } else {
            String errorMsg = "Failed to upload " + type + " image to Blob Storage: " + blobResult.get("error");
            context.getLogger().log(Level.SEVERE, errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }

    private String callOpenAIVision(byte[] imageData, String extension, ImageDimensions dimensions, ExecutionContext context) {
        String base64Image = Base64.getEncoder().encodeToString(imageData);
        String dataUri = String.format("data:image/%s;base64,%s", extension, base64Image);
        String systemPrompt = String.format(imageAnalysisPromptTemplate, dimensions.getWidth(), dimensions.getHeight());

        com.openai.models.chat.completions.ImageUrl imageUrl = com.openai.models.chat.completions.ImageUrl.builder().url(dataUri).build();
        List<com.openai.models.chat.completions.UserMessageContentPart> userContent = List.of(
                com.openai.models.chat.completions.UserMessageContentPartImage.builder().imageUrl(imageUrl).build()
        );

        List<com.openai.models.chat.completions.ChatCompletionMessageParam> messages = List.of(
                com.openai.models.chat.completions.SystemMessage.builder().content(systemPrompt).build(),
                com.openai.models.chat.completions.UserMessage.builder().content(userContent).build()
        );

        com.openai.models.chat.completions.ChatCompletionCreateParams params = com.openai.models.chat.completions.ChatCompletionCreateParams.builder()
                .model(chatModelName)
                .messages(messages)
                .maxTokens(2000) 
                .responseFormat(com.openai.models.chat.completions.ChatCompletionResponseFormat.JSON_OBJECT)
                .build();

        context.getLogger().info("Sending request to OpenAI Vision API (Model: " + chatModelName + ")...");
        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            String responseContent = completion.choices().get(0).message().content();
            context.getLogger().info("Received response from OpenAI Vision API.");
            context.getLogger().fine("OpenAI Raw Response: " + responseContent);
            if (responseContent == null || responseContent.isBlank()) {
                throw new RuntimeException("OpenAI returned an empty response.");
            }
            return responseContent;
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error calling OpenAI Vision API: " + e.getMessage(), e);
            throw new RuntimeException("Error communicating with OpenAI: " + e.getMessage(), e);
        }
    }

    private AnalisisPlato parseAndValidateOpenAIResponse(String jsonResponse, ImageDimensions dimensions, ObjectMapper mapper, ExecutionContext context) throws IOException {
        AnalisisPlato analysis = mapper.readValue(jsonResponse, AnalisisPlato.class);

        if (analysis.getDetallesAlimentos() == null) {
            analysis.setDetallesAlimentos(new ArrayList<>());
        }
        if (analysis.getRecomendaciones() == null) {
            analysis.setRecomendaciones(new ArrayList<>());
        }
        if (analysis.getNutricion() == null) { 
             analysis.setNutricion(AnalisisPlato.NutricionDetalle.builder().build());
        }

        double totalArea = (double) dimensions.getWidth() * dimensions.getHeight();
        double calculatedSumPercentages = 0.0;

        if (totalArea > 0 && analysis.getDetallesAlimentos() != null) {
            for (AnalisisPlato.AlimentoDetalle item : analysis.getDetallesAlimentos()) {
                if (item.getCoordenadasBoundingBox() != null && item.getCoordenadasBoundingBox().size() == 4) {
                    List<Integer> normCoordsList = normalizeCoordinatesList(item.getCoordenadasBoundingBox(), dimensions.getWidth(), dimensions.getHeight());
                    item.setCoordenadasBoundingBox(normCoordsList);
                    double itemArea = (double) (normCoordsList.get(2) - normCoordsList.get(0)) * (normCoordsList.get(3) - normCoordsList.get(1));
                    item.setAreaOcupadaPorcentaje((itemArea / totalArea) * 100.0);
                    calculatedSumPercentages += item.getAreaOcupadaPorcentaje();
                } else {
                    context.getLogger().warning("Alimento '" + item.getNombre() + "' missing or invalid coordinates, setting area to 0.");
                    item.setAreaOcupadaPorcentaje(0.0);
                }
            }
        }
        context.getLogger().info("Calculated percentage sum after coordinate normalization: " + calculatedSumPercentages);
        // TODO: Add OpenAI response field normalization from original service (normalizePercentages)
        return analysis;
    }

    private List<Integer> normalizeCoordinatesList(List<Integer> coords, int imageWidth, int imageHeight) {
        if (coords == null || coords.size() != 4) return List.of(0,0,0,0);
        int x1 = Math.max(0, Math.min(coords.get(0), imageWidth - 1));
        int y1 = Math.max(0, Math.min(coords.get(1), imageHeight - 1));
        int x2 = Math.max(x1, Math.min(coords.get(2), imageWidth - 1)); 
        int y2 = Math.max(y1, Math.min(coords.get(3), imageHeight - 1)); 
        return List.of(x1, y1, x2, y2);
    }

    private byte[] drawAnalysisOnImage(byte[] originalImageData, AnalisisPlato analysis, ImageDimensions dimensions, ExecutionContext context) throws IOException {
        context.getLogger().info("Drawing analysis results onto image...");
        BufferedImage image = javax.imageio.ImageIO.read(new ByteArrayInputStream(originalImageData));
        if (image == null) throw new IOException("Could not read image data for drawing.");

        BufferedImage drawableImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = drawableImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);

        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.4f));
        java.awt.Font font = new java.awt.Font("SansSerif", java.awt.Font.BOLD, 16);

        if (analysis.getDetallesAlimentos() != null) {
            for (AnalisisPlato.AlimentoDetalle item : analysis.getDetallesAlimentos()) {
                List<Integer> coordsList = item.getCoordenadasBoundingBox();
                if (coordsList == null || coordsList.size() != 4 || coordsList.get(2) <= coordsList.get(0) || coordsList.get(3) <= coordsList.get(1)) continue;

                java.awt.Color color = getColorForCategory(item.getCategoria());
                g2d.setColor(color);
                g2d.fillRect(coordsList.get(0), coordsList.get(1), coordsList.get(2) - coordsList.get(0), coordsList.get(3) - coordsList.get(1));

                g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.85f));
                g2d.setFont(font);
                java.awt.FontMetrics fm = g2d.getFontMetrics();
                String label = String.format("%s (%.0f%%)", item.getNombre(), item.getAreaOcupadaPorcentaje());
                int textWidth = fm.stringWidth(label);
                int textHeight = fm.getHeight() - fm.getDescent(); 
                int textX = coordsList.get(0) + 5;
                int textY = coordsList.get(1) + fm.getAscent() + 5;

                g2d.setColor(java.awt.Color.BLACK);
                g2d.fillRect(textX - 2, textY - fm.getAscent() - 2, textWidth + 4, textHeight + 4);
                g2d.setColor(java.awt.Color.WHITE);
                g2d.drawString(label, textX, textY);
                g2d.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.4f));
            }
        }
        g2d.dispose();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            javax.imageio.ImageIO.write(drawableImage, "jpg", baos);
            context.getLogger().info("Successfully drew analysis on image.");
            return baos.toByteArray();
        } catch (IOException e) {
            context.getLogger().log(Level.SEVERE, "Failed to write processed image to byte array: " + e.getMessage(), e);
            throw e;
        }
    }

    private String extractExtensionFromDataUri(String dataUri, String defaultExt) {
        if (dataUri == null || !dataUri.startsWith("data:image/")) {
            return defaultExt;
        }
        int mimeEnd = dataUri.indexOf(';');
        if (mimeEnd == -1) mimeEnd = dataUri.indexOf(',');
        if (mimeEnd == -1) return defaultExt;
        String mimeType = dataUri.substring("data:image/".length(), mimeEnd);
        return switch (mimeType.toLowerCase()) {
            case "jpeg", "jpg" -> "jpg";
            case "png" -> "png";
            case "gif" -> "gif";
            default -> defaultExt;
        };
    }
    
    private java.awt.Color getColorForCategory(String category) {
        if (category == null) return new java.awt.Color(128, 128, 128, 150); // Gray with transparency
        return switch (category.toLowerCase().trim()) {
            case "verduras", "vegetales", "frutas" -> new java.awt.Color(34, 139, 34, 150);  // ForestGreen
            case "proteínas", "proteina" -> new java.awt.Color(255, 99, 71, 150); // Tomato
            case "carbohidratos", "granos" -> new java.awt.Color(100, 149, 237, 150); // CornflowerBlue
            case "lácteos", "lacteos" -> new java.awt.Color(240, 248, 255, 150); // AliceBlue (text will need to be dark)
            case "grasas saludables", "grasas" -> new java.awt.Color(240, 230, 140, 150); // Khaki
            default -> new java.awt.Color(255, 215, 0, 150); // Gold for others
        };
    }

    private static class ImageDimensions {
        private final int width;
        private final int height;
        public ImageDimensions(int w, int h) { this.width = w; this.height = h;}
        public int getWidth() { return width; }
        public int getHeight() { return height; }
    }

    @FunctionName("showAnalysis")
    public HttpResponseMessage showAnalysis(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "show-analysis/{analysis_id}")
            HttpRequestMessage<Optional<String>> request,
            @BindingName("analysis_id") int analysisId,
            final ExecutionContext context) {
        
        context.getLogger().info("Java HTTP trigger processed a request for showAnalysis, ID: " + analysisId);
        try {
            Optional<ImageAnalysisHistoryEntity> historyEntityOptional = imageAnalysisHistoryRepository.findById(analysisId);
            if (historyEntityOptional.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body("Analysis with ID " + analysisId + " not found.").build();
            }
            ImageAnalysisHistoryResponse responseDto = ImageAnalysisDtoConverter.convertImageAnalysisHistoryEntityToDto(historyEntityOptional.get());
            return request.createResponseBuilder(HttpStatus.OK).body(objectMapper.writeValueAsString(responseDto)).build();
        } catch (IOException e) {
            context.getLogger().log(Level.SEVERE, "Error serializing response: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating response: " + e.getMessage()).build();
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error retrieving analysis: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage()).build();
        }
    }

    @FunctionName("listAnalyses")
    @Transactional(readOnly = true) 
    public HttpResponseMessage listAnalyses(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "list-analyses")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        context.getLogger().info("Java HTTP trigger processed a request for listAnalyses.");
        try {
            List<ImageAnalysisHistoryEntity> allEntities = imageAnalysisHistoryRepository.findAll();
            List<ImageAnalysisHistoryResponse> responseDtos = allEntities.stream()
                .map(ImageAnalysisDtoConverter::convertImageAnalysisHistoryEntityToDto)
                .collect(Collectors.toList());
            return request.createResponseBuilder(HttpStatus.OK).body(objectMapper.writeValueAsString(Collections.singletonMap("analyses", responseDtos))).build();
        } catch (IOException e) {
            context.getLogger().log(Level.SEVERE, "Error serializing response: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating response: " + e.getMessage()).build();
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error listing analyses: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage()).build();
        }
    }

    @FunctionName("deleteAnalysis")
    @Transactional 
    public HttpResponseMessage deleteAnalysis(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.DELETE},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "delete-analysis") 
            HttpRequestMessage<Optional<String>> request, 
            final ExecutionContext context) {
        
        context.getLogger().info("Java HTTP trigger processed a request for deleteAnalysis.");
        try {
            String requestBody = request.getBody().orElse(null);
            if (requestBody == null || requestBody.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Request body is empty. Expecting {\"id\": <value>}.").build();
            }
            Map<String, Integer> deleteRequest = objectMapper.readValue(requestBody, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Integer>>() {});
            Integer analysisId = deleteRequest.get("id");

            if (analysisId == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Missing 'id' in request body.").build();
            }

            Optional<ImageAnalysisHistoryEntity> entityOptional = imageAnalysisHistoryRepository.findById(analysisId);
            if (entityOptional.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                    .body("Analysis with ID " + analysisId + " not found for deletion.").build();
            }
            
            ImageAnalysisHistoryEntity historyEntry = entityOptional.get();
            if (historyEntry.getImagenOriginalUrl() != null) {
                try { blobStorageService.deleteFileFromBlob(historyEntry.getImagenOriginalUrl()); } 
                catch (Exception e) { context.getLogger().warning("Failed to delete original blob: " + historyEntry.getImagenOriginalUrl() + " Error: " + e.getMessage());}
            }
            if (historyEntry.getImagenProcesadaUrl() != null) {
                 try { blobStorageService.deleteFileFromBlob(historyEntry.getImagenProcesadaUrl()); }
                 catch (Exception e) { context.getLogger().warning("Failed to delete processed blob: " + historyEntry.getImagenProcesadaUrl() + " Error: " + e.getMessage());}
            }
            // String blobFolderPath = String.format("%s/%d", PLATES_BLOB_FOLDER, analysisId); // Folder path by ID
            // try { blobStorageService.deleteFolderFromBlob(blobFolderPath); } // If you have deleteFolder
            // catch (Exception e) { context.getLogger().warning("Failed to delete blob folder: " + blobFolderPath + " Error: " + e.getMessage());}

            imageAnalysisHistoryRepository.deleteById(analysisId);
            context.getLogger().info("Deleted analysis with ID: " + analysisId);

            return request.createResponseBuilder(HttpStatus.OK)
                .body(objectMapper.writeValueAsString(Collections.singletonMap("message", "Analysis with ID " + analysisId + " deleted successfully.")))
                .build();
        } catch (IOException e) {
            context.getLogger().log(Level.SEVERE, "Error parsing request or serializing response: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Error processing JSON: " + e.getMessage()).build();
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error deleting analysis: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error: " + e.getMessage()).build();
        }
    }
} 