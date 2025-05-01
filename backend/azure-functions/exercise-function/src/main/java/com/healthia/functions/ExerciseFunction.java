package com.healthia.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.healthia.java.models.UserData; // Your original UserData model, ensure it's accessible
// If ExerciseFunction needs to fetch UserData itself, it would need JPAUtil and UserDataEntity
// import com.healthia.functions.entities.UserDataEntity;
// import com.healthia.functions.util.JPAUtil; // Assuming a shared util or copy for ExerciseFunction

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.*;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import jakarta.persistence.EntityManager; // Keep if direct DB access might be needed

import java.util.*;
import java.util.logging.Level;

/**
 * Azure Functions for Exercise-related tasks.
 */
public class ExerciseFunction {

    private static final OpenAIClient openAIClient;
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String chatModelName;

    static {
        String openaiApiKey = System.getenv("OPENAI_API_KEY");
        chatModelName = System.getenv("OPENAI_MODEL");
        if (openaiApiKey == null || openaiApiKey.trim().isEmpty()) {
             System.err.println("OPENAI_API_KEY is not set for ExerciseFunction. OpenAI dependent functions will fail.");
             openAIClient = null;
        } else {
            openAIClient = OpenAIClient.builder().responseFormat(OpenAIClient.ResponseFormat.JSON).apiKey(openaiApiKey).build();
        }
        // Note: No BlobStorageService or direct JPA initialization here as per current design.
        // If ExerciseFunction needed to load UserData itself, JPAUtil.getEntityManager() would be used.
    }

    // --- Request Models ---
    public static class ExerciseTextRequest {
        public String userInput;
        public UserData userData; // Expects com.healthia.java.models.UserData
    }

    public static class ExerciseImageRequest {
        public String userPrompt;
        public UserData userData; // Expects com.healthia.java.models.UserData
        public String imageBase64;
        public String originalFilename;
    }

    // --- HTTP Triggered Functions ---
    @FunctionName("ProcessExerciseText")
    public HttpResponseMessage processText(
            @HttpTrigger(name = "req", 
                         methods = {HttpMethod.POST}, 
                         authLevel = AuthorizationLevel.FUNCTION, 
                         dataType = "json") 
            HttpRequestMessage<Optional<ExerciseTextRequest>> request,
            final ExecutionContext context) {
        context.getLogger().info("ExerciseFunction: ProcessExerciseText triggered.");

        ExerciseTextRequest reqBody = request.getBody().orElse(null);
        if (reqBody == null || reqBody.userInput == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass 'userInput' in the request body").build();
        }
        if (openAIClient == null) {
            context.getLogger().severe("OpenAIClient is not initialized for ExerciseFunction. Check API Key.");
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Server configuration error (OpenAI).").build();
        }

        try {
            String result;
            // If UserData is provided, ensure it's the correct model (com.healthia.java.models.UserData)
            // No direct DB operation here; assumes UserData is fetched by caller or another service if needed prior to calling this function.
            if (reqBody.userData != null) {
                result = processWithUserDataInternal(reqBody.userInput, reqBody.userData, context);
            } else {
                result = processInternal(reqBody.userInput, context);
            }
            return request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "text/plain").body(result).build();
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error processing exercise text request: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request: " + e.getMessage()).build();
        }
    }

    @FunctionName("ProcessExerciseImage")
    public HttpResponseMessage processImage(
            @HttpTrigger(name = "req", 
                         methods = {HttpMethod.POST}, 
                         authLevel = AuthorizationLevel.FUNCTION, 
                         dataType = "json") 
            HttpRequestMessage<Optional<ExerciseImageRequest>> request,
            final ExecutionContext context) {
        context.getLogger().info("ExerciseFunction: ProcessExerciseImage triggered.");

        ExerciseImageRequest reqBody = request.getBody().orElse(null);
        if (reqBody == null || reqBody.imageBase64 == null || reqBody.userPrompt == null || reqBody.userData == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass 'userPrompt', 'userData', and 'imageBase64'").build();
        }
        if (openAIClient == null) {
            context.getLogger().severe("OpenAIClient is not initialized for ExerciseFunction. Check API Key.");
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Server configuration error (OpenAI).").build();
        }

        try {
            byte[] imageData = Base64.getDecoder().decode(reqBody.imageBase64);
            String result = processImageInternal(imageData, reqBody.userPrompt, reqBody.userData, reqBody.originalFilename, context);
            return request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "text/plain").body(result).build();
        } catch (IllegalArgumentException e) {
            context.getLogger().log(Level.WARNING, "Invalid Base64 image data: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Invalid Base64 image data.").build();
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error processing exercise image request: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request: " + e.getMessage()).build();
        }
    }

    // --- Internal Logic (Adapted from ExerciseAgent) ---

    private String processInternal(String input, ExecutionContext context) {
        context.getLogger().info("ExerciseFunction processing input (no user data).");
        String systemPrompt = "Eres un entrenador personal experto que proporciona información precisa sobre ejercicios, rutinas de entrenamiento, técnicas correctas y recomendaciones personalizadas. No tienes acceso a datos específicos del usuario. Tus conocimientos incluyen: Diferentes tipos de ejercicios (cardiovasculares, fuerza, flexibilidad, etc.), Técnicas correctas para evitar lesiones, Rutinas para diferentes objetivos (pérdida de peso, ganancia muscular, resistencia, etc.), Adaptaciones para diferentes niveles de condición física. Proporciona respuestas claras, precisas y personalizadas. Cuando sea apropiado, sugiere ejercicios específicos con instrucciones detalladas. Usa formato Markdown.";

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(chatModelName)
                .addSystemMessage(systemPrompt)
                .addUserMessage(input)
                .build();
        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            return completion.choices().get(0).message().content();
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error calling OpenAI in ExerciseFunction.processInternal: " + e.getMessage(), e);
            return "Lo siento, hubo un error al procesar tu consulta de ejercicio.";
        }
    }

    private String processWithUserDataInternal(String input, UserData userData, ExecutionContext context) {
        context.getLogger().info("ExerciseFunction processing input for user: " + userData.getId());
        String userDataSummary = buildUserDataSummary(userData);

        String systemPrompt = String.format("Eres un entrenador personal experto que proporciona información precisa sobre ejercicios, rutinas de entrenamiento, técnicas correctas y recomendaciones personalizadas. Datos del usuario: %s. Tus conocimientos incluyen: Diferentes tipos de ejercicios (cardiovasculares, fuerza, flexibilidad, etc.), Técnicas correctas para evitar lesiones, Rutinas para diferentes objetivos (pérdida de peso, ganancia muscular, resistencia, etc.), Adaptaciones para diferentes niveles de condición física, Recomendaciones para problemas específicos. Proporciona respuestas claras, precisas y personalizadas según los datos del usuario. Cuando sea apropiado, sugiere ejercicios específicos con instrucciones detalladas y adaptados a las características particulares del usuario. Ten especial cuidado con las condiciones médicas informadas y adapta tus recomendaciones para que sean seguras y apropiadas para el usuario. Usa formato Markdown.", userDataSummary);

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(chatModelName)
                .addSystemMessage(systemPrompt)
                .addUserMessage(input)
                .build();
        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            return completion.choices().get(0).message().content();
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error calling OpenAI in ExerciseFunction.processWithUserDataInternal: " + e.getMessage(), e);
            return "Lo siento, hubo un error al procesar tu consulta de ejercicio personalizada.";
        }
    }

    private String processImageInternal(byte[] imageData, String prompt, UserData userData, String originalFilename, ExecutionContext context) {
        context.getLogger().info("ExerciseFunction processing image for user: " + userData.getId());

        String base64Image = Base64.getEncoder().encodeToString(imageData);
        String imageExtension = "jpeg"; 
        if (originalFilename != null) {
            int lastDot = originalFilename.lastIndexOf('.');
            if (lastDot > 0 && lastDot < originalFilename.length() - 1) {
                imageExtension = originalFilename.substring(lastDot + 1).toLowerCase();
            }
        }

        String userDataSummary = buildUserDataSummary(userData);
        String systemPrompt = String.format("Eres un entrenador personal experto que analiza imágenes relacionadas con ejercicios, actividad física, equipos de gimnasio, postura, técnica deportiva y dispositivos de fitness. Datos del usuario: %s. Al analizar esta imagen: 1. Identifica el tipo de ejercicio, equipo o actividad mostrada. 2. Evalúa la técnica o postura si es visible (sin juzgar, solo informar). 3. Proporciona recomendaciones para mejorar o adaptar el ejercicio al nivel y objetivos del usuario. 4. Sugiere variaciones o alternativas si corresponde. 5. Considera las condiciones médicas del usuario al dar recomendaciones. Si la imagen muestra: Un ejercicio específico: explica la técnica correcta, músculos trabajados y beneficios. Un equipo de gimnasio: describe su uso adecuado y ejercicios posibles con él. Datos de entrenamiento (smartwatch, app): interpreta los datos y sugiere mejoras. Un plan de entrenamiento: analiza su idoneidad para los objetivos del usuario. Proporciona consejos prácticos, científicamente respaldados y adaptados a las características particulares del usuario. Enfatiza la seguridad y la correcta ejecución. Usa formato Markdown.", userDataSummary);

        ImageUrl imageUrl = ImageUrl.builder()
                .url(String.format("data:image/%s;base64,%s", imageExtension, base64Image))
                .build();

        List<UserMessageContentPart> userContentParts = new ArrayList<>();
        userContentParts.add(UserMessageContentPartText.builder().text(prompt).build());
        userContentParts.add(UserMessageContentPartImage.builder().imageUrl(imageUrl).build());

        List<ChatCompletionMessageParam> messages = List.of(
                SystemMessage.builder().content(systemPrompt).build(),
                UserMessage.builder().content(userContentParts).build()
        );

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(chatModelName)
                .messages(messages)
                .build();

        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            return completion.choices().get(0).message().content();
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error calling OpenAI in ExerciseFunction.processImageInternal: " + e.getMessage(), e);
            return "Lo siento, hubo un error al analizar la imagen de ejercicio.";
        }
    }

    // --- Helper Methods (from ExerciseAgent) ---
    private String buildUserDataSummary(UserData userData) {
         return String.format("ID: %s, Nombre: %s, Edad: %s, Peso: %skg, Altura: %scm, Género: %s, Condiciones: %s, Objetivos: %s, Actividad: %s",
                Optional.ofNullable(userData.getId()).orElse("N/A"),
                Optional.ofNullable(userData.getNombre()).orElse("N/A"),
                Optional.ofNullable(userData.getEdad()).map(Object::toString).orElse("N/A"),
                Optional.ofNullable(userData.getPeso()).map(Object::toString).orElse("N/A"),
                Optional.ofNullable(userData.getAltura()).map(Object::toString).orElse("N/A"),
                Optional.ofNullable(userData.getGenero()).orElse("N/A"),
                Optional.ofNullable(userData.getCondicionesMedicas()).filter(l -> !l.isEmpty()).map(l -> String.join(", ", l)).orElse("Ninguna"),
                Optional.ofNullable(userData.getObjetivos()).filter(l -> !l.isEmpty()).map(l -> String.join(", ", l)).orElse("Ninguno"),
                Optional.ofNullable(userData.getNivelActividad()).orElse("N/A")
        );
    }
    // Note: encodeImageToBase64(Path imagePath) from ExerciseAgent is not directly needed here 
    // as we receive imageBase64 in the request for image processing.
    // If the function were to receive a URL to an image in Blob storage, it would first download
    // the byte[] using BlobStorageService, then proceed.
} 