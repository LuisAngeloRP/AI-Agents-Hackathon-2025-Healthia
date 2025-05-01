package com.healthia.java.services.agents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthia.java.models.UserData;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class MedicalAgent implements SupervisorService.SpecializedAgent {

    private static final Logger log = LoggerFactory.getLogger(MedicalAgent.class);
    private static final String DISCLAIMER = "\n\n[Nota importante: Esta información es educativa y no constituye un diagnóstico médico. Consulte siempre a un profesional de la salud calificado para una evaluación adecuada.]";

    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.user-data-dir:data_usuario}")
    private String userDataDir;
    @Value("${app.medical-info-file:medical_info.json}")
    private String medicalInfoFile;

    @Value("${app.openai.model}")
    private String chatModelName;

    private Path medicalInfoPath;

    @Autowired
    public MedicalAgent(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    @PostConstruct
    private void init() {
        try {
            Files.createDirectories(Paths.get(userDataDir));
            medicalInfoPath = Paths.get(userDataDir, medicalInfoFile);
            log.info("Medical info file path set to: {}", medicalInfoPath);
        } catch (IOException e) {
            log.error("Could not create user data directory for medical info: {}", userDataDir, e);
        }
    }

    @Override
    public String process(String input) {
        log.info("MedicalAgent processing input (no user data).");
        String systemPrompt = """
        Eres un asistente médico especializado en proporcionar información sobre temas de salud.

        Importante: No eres un médico licenciado y no puedes diagnosticar enfermedades ni recetar
        medicamentos. Siempre debes aconsejar a los usuarios que consulten a profesionales de la
        salud para diagnósticos y tratamientos.

        Tus conocimientos incluyen:
        - Información general sobre condiciones médicas comunes
        - Explicaciones de procedimientos médicos habituales
        - Consejos generales de salud y prevención
        - Interpretación de términos médicos en lenguaje comprensible
        - Orientación sobre cuándo buscar atención médica

        Proporciona información clara, precisa y basada en evidencia científica.
        Evita dar consejos médicos específicos y en caso de emergencia siempre recomienda
        buscar atención médica inmediata.
        Usa formato Markdown.
        """;

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(this.chatModelName)
                .addSystemMessage(systemPrompt)
                .addUserMessage(input) // Includes markdown instructions
                .build();
        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            return completion.choices().get(0).message().content() + DISCLAIMER;
        } catch (Exception e) {
            log.error("Error calling OpenAI in MedicalAgent.process: {}", e.getMessage(), e);
            return "Lo siento, hubo un error al procesar tu consulta médica." + DISCLAIMER;
        }
    }

    @Override
    public String processWithUserData(String input, UserData userDataFromSupervisor) {
        log.info("MedicalAgent processing input with user data context.");
        Optional<UserData> existingMedicalDataOpt = getUserMedicalData();

        if (!existingMedicalDataOpt.isPresent()) {
             log.warn("No existing medical data found. Processing without history.");
            // Use data passed from supervisor if no file exists
             return processWithProvidedData(input, userDataFromSupervisor);
        }

        UserData medicalHistory = existingMedicalDataOpt.get();
        // Update medicalHistory with any *new basic* info from supervisor if needed,
        // but prioritize the persisted detailed medical history.
        updateBasicInfoIfMissing(medicalHistory, userDataFromSupervisor);

        // Add current query to history before processing
        addRecordToHistory(medicalHistory, Map.of(
                "tipo", "consulta",
                "descripcion", input // Raw input before markdown addition
        ));
        saveUserMedicalData(medicalHistory); // Save updated history

        String contextPrompt = buildMedicalContextPrompt(medicalHistory);

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(this.chatModelName)
                .addSystemMessage(contextPrompt)
                .addUserMessage(input) // Includes markdown instructions
                .build();
        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            return completion.choices().get(0).message().content() + DISCLAIMER;
        } catch (Exception e) {
            log.error("Error calling OpenAI in MedicalAgent.processWithUserData: {}", e.getMessage(), e);
            return "Lo siento, hubo un error al procesar tu consulta médica con tu historial." + DISCLAIMER;
        }
    }

    private String processWithProvidedData(String input, UserData userData) {
         String userDataSummary = buildUserDataSummaryForPrompt(userData); // Basic summary
         String systemPrompt = String.format("""
            Eres un asistente médico especializado...
            Importante: No eres un médico licenciado...
            Datos del usuario (proporcionados, sin historial completo):
            %s
            Proporciona información clara... (similar al prompt sin datos)
            Usa formato Markdown.
            """, userDataSummary);

         ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(this.chatModelName)
                .addSystemMessage(systemPrompt)
                .addUserMessage(input)
                .build();
          try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            return completion.choices().get(0).message().content() + DISCLAIMER;
        } catch (Exception e) {
            log.error("Error calling OpenAI in MedicalAgent.processWithProvidedData: {}", e.getMessage(), e);
            return "Lo siento, hubo un error al procesar tu consulta médica." + DISCLAIMER;
        }
    }

    @Override
    public String processImage(Path imagePath, String prompt, UserData userDataFromSupervisor) {
        log.info("MedicalAgent processing image with user data context. Path: {}", imagePath);
        Optional<UserData> existingMedicalDataOpt = getUserMedicalData();

         if (!existingMedicalDataOpt.isPresent()) {
             log.warn("No existing medical data found for image processing.");
              // Decide how to handle - maybe use supervisor data or return error?
              // For now, use supervisor data and proceed.
              return processImageWithProvidedData(imagePath, prompt, userDataFromSupervisor);
         }

        UserData medicalHistory = existingMedicalDataOpt.get();
        updateBasicInfoIfMissing(medicalHistory, userDataFromSupervisor);

        // Add image query to history
        addRecordToHistory(medicalHistory, Map.of(
                "tipo", "consulta_imagen",
                "descripcion", prompt, // Prompt includes user text + markdown
                "imagen_referencia", imagePath.getFileName().toString()
        ));
        saveUserMedicalData(medicalHistory); // Save updated history

        String base64Image;
        try {
            base64Image = encodeImageToBase64(imagePath);
        } catch (IOException e) {
            log.error("Error encoding image: {}", imagePath, e);
            return "Error al procesar la imagen." + DISCLAIMER;
        }

        String contextPrompt = buildMedicalImageContextPrompt(medicalHistory);
        ImageUrl imageUrl = ImageUrl.builder()
                .url(String.format("data:image/jpeg;base64,%s", base64Image))
                .build();

        List<UserMessageContentPart> userContentParts = new ArrayList<>();
        userContentParts.add(UserMessageContentPartText.builder().text(prompt).build());
        userContentParts.add(UserMessageContentPartImage.builder().imageUrl(imageUrl).build());

        List<ChatCompletionMessageParam> messages = List.of(
                SystemMessage.builder().content(contextPrompt).build(),
                UserMessage.builder().content(userContentParts).build()
        );

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(this.chatModelName)
                .messages(messages)
                .build();

        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            return completion.choices().get(0).message().content() + DISCLAIMER;
        } catch (Exception e) {
            log.error("Error calling OpenAI in MedicalAgent.processImage: {}", e.getMessage(), e);
            return "Lo siento, hubo un error al analizar la imagen médica." + DISCLAIMER;
        }
    }

     private String processImageWithProvidedData(Path imagePath, String prompt, UserData userData) {
          String base64Image;
        try {
            base64Image = encodeImageToBase64(imagePath);
        } catch (IOException e) {
            log.error("Error encoding image: {}", imagePath, e);
            return "Error al procesar la imagen." + DISCLAIMER;
        }
        String userDataSummary = buildUserDataSummaryForPrompt(userData);
        String systemPrompt = String.format("""
            Eres un asistente médico especializado que analiza imágenes...
            IMPORTANTE: NO PUEDES DIAGNOSTICAR...
            Datos del usuario (proporcionados, sin historial completo):
            %s
            Al analizar esta imagen...
            Usa formato Markdown.
            """, userDataSummary);

         ImageUrl imageUrl = ImageUrl.builder()
                .url(String.format("data:image/jpeg;base64,%s", base64Image))
                .build();

        List<UserMessageContentPart> userContentParts = new ArrayList<>();
        userContentParts.add(UserMessageContentPartText.builder().text(prompt).build());
        userContentParts.add(UserMessageContentPartImage.builder().imageUrl(imageUrl).build());

         List<ChatCompletionMessageParam> messages = List.of(
                SystemMessage.builder().content(systemPrompt).build(),
                UserMessage.builder().content(userContentParts).build()
        );

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(this.chatModelName)
                .messages(messages)
                .build();
          try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            return completion.choices().get(0).message().content() + DISCLAIMER;
        } catch (Exception e) {
            log.error("Error calling OpenAI in MedicalAgent.processImageWithProvidedData: {}", e.getMessage(), e);
            return "Lo siento, hubo un error al analizar la imagen médica." + DISCLAIMER;
        }
    }

    // --- Persistence Methods ---

    private Optional<UserData> getUserMedicalData() {
        if (medicalInfoPath == null || !Files.exists(medicalInfoPath)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(medicalInfoPath.toFile(), UserData.class));
        } catch (IOException e) {
            log.error("Error loading medical data from {}: {}", medicalInfoPath, e.getMessage(), e);
            return Optional.empty();
        }
    }

    private void saveUserMedicalData(UserData userData) {
         if (medicalInfoPath == null) {
             log.error("Medical info path is not initialized, cannot save data.");
             return;
         }
        try {
            // Ensure ultimaActualizacion is set
            userData.setUltimaActualizacion(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(medicalInfoPath.toFile(), userData);
            log.info("Saved medical data to: {}", medicalInfoPath);
        } catch (IOException e) {
            log.error("Error saving medical data to {}: {}", medicalInfoPath, e.getMessage(), e);
        }
    }

    // Simplified version of Python's _save_user_medical_data - focuses on adding records
    private void addRecordToHistory(UserData history, Map<String, Object> record) {
        Map<String, Object> newRecord = new HashMap<>(record);
        newRecord.putIfAbsent("fecha", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        String type = (String) record.getOrDefault("tipo", "consulta");

        switch (type) {
            case "consulta":
            case "consulta_imagen":
                history.getHistorialConsultas().add(newRecord);
                break;
            case "medicamento":
                history.getMedicamentos().add(newRecord);
                break;
            case "estudio":
            case "examen":
                history.getEstudiosMedicos().add(newRecord);
                break;
            case "cirugia":
                history.getCirugias().add(newRecord);
                break;
            case "alergia":
                if (record.get("detalle") instanceof String detalle && !history.getAlergias().contains(detalle)) {
                    history.getAlergias().add(detalle);
                }
                break;
            case "condicion_medica":
                 if (record.get("detalle") instanceof String detalle && !history.getCondicionesMedicas().contains(detalle)) {
                    history.getCondicionesMedicas().add(detalle);
                }
                break;
            case "vacuna":
                history.getVacunas().add(newRecord);
                break;
            default:
                log.warn("Unknown medical record type: {}", type);
        }
    }

     // Called by processWithUserData and processImage to fill basic details if missing in persisted file
     private void updateBasicInfoIfMissing(UserData persistedData, UserData supervisorData) {
        if (persistedData == null || supervisorData == null) return;

        if (persistedData.getNombre() == null && supervisorData.getNombre() != null) persistedData.setNombre(supervisorData.getNombre());
        if (persistedData.getEdad() == null && supervisorData.getEdad() != null) persistedData.setEdad(supervisorData.getEdad());
        if (persistedData.getPeso() == null && supervisorData.getPeso() != null) persistedData.setPeso(supervisorData.getPeso());
        if (persistedData.getAltura() == null && supervisorData.getAltura() != null) persistedData.setAltura(supervisorData.getAltura());
        if (persistedData.getGenero() == null && supervisorData.getGenero() != null) persistedData.setGenero(supervisorData.getGenero());
        // Add other basic fields if necessary
    }

    // --- Helper Methods ---
    private String encodeImageToBase64(Path imagePath) throws IOException {
        byte[] imageBytes = Files.readAllBytes(imagePath);
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private String buildUserDataSummaryForPrompt(UserData data) {
        // Builds a summary string from UserData object for prompts
        if (data == null) return "No hay datos de usuario disponibles.";
        return String.format("""
            - Nombre: %s
            - Edad: %s
            - Peso: %s kg
            - Altura: %s cm
            - Género: %s
            - Grupo sanguíneo: %s
            - Condiciones médicas: %s
            - Alergias: %s
            """,
            Optional.ofNullable(data.getNombre()).orElse("No especificado"),
            Optional.ofNullable(data.getEdad()).map(Object::toString).orElse("No especificada"),
            Optional.ofNullable(data.getPeso()).map(Object::toString).orElse("No especificado"),
            Optional.ofNullable(data.getAltura()).map(Object::toString).orElse("No especificada"),
            Optional.ofNullable(data.getGenero()).orElse("No especificado"),
            Optional.ofNullable(data.getGrupoSanguineo()).orElse("No especificado"),
            Optional.ofNullable(data.getCondicionesMedicas()).filter(l->!l.isEmpty()).map(l->String.join(", ",l)).orElse("Ninguna reportada"),
            Optional.ofNullable(data.getAlergias()).filter(l->!l.isEmpty()).map(l->String.join(", ",l)).orElse("Ninguna reportada")
        );
    }

    private String buildMedicalContextPrompt(UserData medicalHistory) {
         String userDataSummary = buildUserDataSummaryForPrompt(medicalHistory);
         String consultationsSummary = buildListSummary("Consultas previas (más recientes)", medicalHistory.getHistorialConsultas(), 5, item -> formatMapItem(item, "descripcion"));
         String medicationsSummary = buildListSummary("Medicamentos actuales", medicalHistory.getMedicamentos(), 0, item -> formatMapItem(item, "nombre", "dosis", "frecuencia"));
         String studiesSummary = buildListSummary("Estudios médicos recientes", medicalHistory.getEstudiosMedicos(), 3, item -> formatMapItem(item, "nombre", "fecha", "resultado"));

         return String.format("""
            Eres un asistente médico especializado en proporcionar información sobre temas de salud.

            Importante: No eres un médico licenciado...

            Datos del usuario y contexto médico:
            %s
            %s
            %s
            %s

            Tus conocimientos incluyen...

            Proporciona información clara, precisa y basada en evidencia...
            Evita dar consejos médicos específicos...
            Ten en cuenta las condiciones médicas y alergias del usuario...
            Si la consulta actual está relacionada con consultas previas o con las condiciones médicas...
            Usa formato Markdown.
            """, userDataSummary, medicationsSummary, studiesSummary, consultationsSummary);
    }

     private String buildMedicalImageContextPrompt(UserData medicalHistory) {
         String userDataSummary = buildUserDataSummaryForPrompt(medicalHistory);
         return String.format("""
            Eres un asistente médico especializado que analiza imágenes relacionadas con temas de salud.

            IMPORTANTE: NO PUEDES DIAGNOSTICAR ENFERMEDADES NI RECETAR TRATAMIENTOS...

            Datos del usuario:
            %s

            Al analizar esta imagen:
            1. Describe lo que observas de manera objetiva
            2. Proporciona información educativa sobre lo que muestra la imagen
            3. Explica posibles implicaciones generales para la salud (SIN DIAGNOSTICAR)
            4. Si es pertinente, sugerir cuándo buscar atención médica
            5. SIEMPRE enfatiza la importancia de consultar a un profesional de la salud

            Si la imagen muestra:
            - Medicamentos: ...
            - Lesiones o condiciones de la piel: ...
            - Resultados de exámenes médicos: ...
            - Equipamiento médico: ...

            Usa un lenguaje claro... Mantén un tono empático...
            Usa formato Markdown.
            """, userDataSummary);
    }

    // Helper to summarize lists for the context prompt
    private String buildListSummary(String title, List<Map<String, Object>> list, int limit, java.util.function.Function<Map<String, Object>, String> formatter) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder summary = new StringBuilder(title + ":\n");
        List<Map<String, Object>> itemsToDisplay = (limit > 0 && list.size() > limit)
            ? list.subList(list.size() - limit, list.size())
            : list;

        Collections.reverse(itemsToDisplay); // Show most recent first

        for (Map<String, Object> item : itemsToDisplay) {
            summary.append("- ").append(formatter.apply(item)).append("\n");
        }
        return summary.toString();
    }

    // Helper to format map items for summary
    private String formatMapItem(Map<String, Object> item, String... keys) {
         if (item == null) return "No especificado";
         StringBuilder formatted = new StringBuilder();
         boolean first = true;
         for (String key : keys) {
             Object value = item.get(key);
             if (value != null) {
                 if (!first) formatted.append(" | ");
                 if(keys.length > 1) formatted.append(key).append(": ");
                 formatted.append(value.toString());
                 first = false;
             }
         }
         return formatted.length() > 0 ? formatted.toString() : "Detalle no especificado";
     }
} 