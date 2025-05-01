package com.healthia.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.healthia.java.models.UserData; // Your original UserData model
import com.healthia.functions.entities.MealPlanEntity;
import com.healthia.functions.entities.UserDataEntity;
import com.healthia.functions.util.JPAUtil;

// Assuming MealPlanGeneratorService is accessible (e.g. via shared library or copied source)
import com.healthia.java.services.MealPlanGeneratorService; 

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.*;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.logging.Level;


/**
 * Azure Functions for Nutrition-related tasks.
 */
public class NutritionFunction {

    private static final OpenAIClient openAIClient;
    private static final MealPlanGeneratorService mealPlanGeneratorService;
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String chatModelName;

    static {
        String openaiApiKey = System.getenv("OPENAI_API_KEY");
        chatModelName = System.getenv("OPENAI_MODEL");
        if (openaiApiKey == null || openaiApiKey.trim().isEmpty()) {
             System.err.println("OPENAI_API_KEY is not set. OpenAI dependent functions will fail.");
             openAIClient = null;
        } else {
            openAIClient = OpenAIClient.builder().responseFormat(OpenAIClient.ResponseFormat.JSON).apiKey(openaiApiKey).build();
        }
        // MealPlanGeneratorService might need specific initialization if it loads resources like meals.js
        // For now, assuming a simple constructor. This needs to be adapted based on its actual implementation.
        try {
            mealPlanGeneratorService = new MealPlanGeneratorService(); 
        } catch (Exception e) {
            System.err.println("Failed to initialize MealPlanGeneratorService: " + e.getMessage());
            throw new RuntimeException("Could not initialize MealPlanGeneratorService", e);
        }
    }

    // --- Request Models (Inner classes or separate files) ---
    public static class NutritionTextRequest {
        public String userInput;
        public UserData userData; // This is your original com.healthia.java.models.UserData
    }

    public static class NutritionImageRequest {
        public String userPrompt;
        public UserData userData; // Original com.healthia.java.models.UserData
        public String imageBase64;
        public String originalFilename;
    }

    // --- HTTP Triggered Functions ---

    @FunctionName("ProcessNutritionText")
    public HttpResponseMessage processText(
            @HttpTrigger(name = "req", 
                         methods = {HttpMethod.POST}, 
                         authLevel = AuthorizationLevel.FUNCTION, 
                         dataType = "json") 
            HttpRequestMessage<Optional<NutritionTextRequest>> request,
            final ExecutionContext context) {
        context.getLogger().info("NutritionFunction: ProcessNutritionText triggered.");

        NutritionTextRequest reqBody = request.getBody().orElse(null);

        if (reqBody == null || reqBody.userInput == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass valid 'userInput' in the request body").build();
        }
        if (openAIClient == null) {
            context.getLogger().severe("OpenAIClient is not initialized. Check API Key.");
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Server configuration error (OpenAI).").build();
        }

        try {
            String result;
            if (reqBody.userData != null) {
                // Persist/update UserData to MySQL
                saveOrUpdateUserData(reqBody.userData, context);
                result = processWithUserDataInternal(reqBody.userInput, reqBody.userData, context);
            } else {
                result = processInternal(reqBody.userInput, context);
            }
            return request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "text/plain").body(result).build();
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error processing nutrition text request: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request: " + e.getMessage()).build();
        }
    }

    @FunctionName("ProcessNutritionImage")
    public HttpResponseMessage processImage(
            @HttpTrigger(name = "req", 
                         methods = {HttpMethod.POST}, 
                         authLevel = AuthorizationLevel.FUNCTION, 
                         dataType = "json") 
            HttpRequestMessage<Optional<NutritionImageRequest>> request,
            final ExecutionContext context) {
        context.getLogger().info("NutritionFunction: ProcessNutritionImage triggered.");

        NutritionImageRequest reqBody = request.getBody().orElse(null);

        if (reqBody == null || reqBody.imageBase64 == null || reqBody.userPrompt == null || reqBody.userData == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass 'userPrompt', 'userData', and 'imageBase64' in the request body").build();
        }
         if (openAIClient == null) {
            context.getLogger().severe("OpenAIClient is not initialized. Check API Key.");
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Server configuration error (OpenAI).").build();
        }

        try {
            saveOrUpdateUserData(reqBody.userData, context); // Save user data before processing image
            byte[] imageData = Base64.getDecoder().decode(reqBody.imageBase64);
            String result = processImageInternal(imageData, reqBody.userPrompt, reqBody.userData, reqBody.originalFilename, context);
            return request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "text/plain").body(result).build();
        } catch (IllegalArgumentException e) {
            context.getLogger().log(Level.WARNING, "Invalid Base64 image data: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Invalid Base64 image data.").build();
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error processing nutrition image request: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request: " + e.getMessage()).build();
        }
    }

    // --- Internal Logic (Adapted from NutritionAgent) ---
    // These methods will contain the core logic migrated from NutritionAgent.java
    // All file system access MUST be replaced with blobStorageService calls.
    // Logging should use context.getLogger().

    private String processInternal(String input, ExecutionContext context) {
        context.getLogger().info("NutritionFunction processing input (no user data).");
        String systemPrompt = "Eres un experto en nutrición que proporciona información precisa sobre alimentación saludable, nutrientes, dietas y recomendaciones alimentarias. No tienes acceso a datos específicos del usuario. Tus conocimientos incluyen: Principios de una alimentación equilibrada, Propiedades y fuentes de diferentes nutrientes, Recomendaciones dietéticas para diferentes objetivos y condiciones, Alternativas alimentarias para diferentes restricciones dietéticas, Información sobre alimentos funcionales y suplementos. Proporciona consejos claros, precisos y basados en evidencia científica. No promueves dietas extremas o no saludables.";

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(chatModelName)
                .addSystemMessage(systemPrompt)
                .addUserMessage(input)
                .build();
        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            return completion.choices().get(0).message().content();
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error calling OpenAI in NutritionFunction.processInternal: " + e.getMessage(), e);
            return "Lo siento, hubo un error al procesar tu consulta de nutrición.";
        }
    }

    private String processWithUserDataInternal(String input, UserData userData, ExecutionContext context) {
        context.getLogger().info("NutritionFunction processing input for user: " + userData.getId());
        
        String lowerInput = input.toLowerCase();
        if (mealPlanGeneratorService != null && (lowerInput.contains("plan alimenticio") || lowerInput.contains("dieta") || lowerInput.contains("plan nutricional"))) {
            context.getLogger().info("Request identified as meal plan generation for user: " + userData.getId());
            return createMealPlanWithJPA(input, userData, context);
        }
        
        String userDataSummary = buildUserDataSummary(userData);
        String systemPrompt = String.format("Eres un experto en nutrición que proporciona información precisa sobre alimentación saludable, nutrientes, dietas y recomendaciones alimentarias. Datos del usuario: %s. Tus conocimientos incluyen: Principios de una alimentación equilibrada, Propiedades y fuentes de diferentes nutrientes, Recomendaciones dietéticas para diferentes objetivos y condiciones, Alternativas alimentarias para diferentes restricciones dietéticas, Información sobre alimentos funcionales y suplementos. Proporciona consejos claros, precisos y personalizados según los datos del usuario. No promueves dietas extremas o no saludables. Personaliza tus recomendaciones considerando los datos proporcionados sobre el usuario.", userDataSummary);

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(chatModelName)
                .addSystemMessage(systemPrompt)
                .addUserMessage(input)
                .build();
        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            return completion.choices().get(0).message().content();
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error calling OpenAI in NutritionFunction.processWithUserDataInternal: " + e.getMessage(), e);
            return "Lo siento, hubo un error al procesar tu consulta de nutrición personalizada.";
        }
    }

    private String processImageInternal(byte[] imageData, String prompt, UserData userData, String originalFilename, ExecutionContext context) {
        context.getLogger().info("NutritionFunction processing image for user: " + userData.getId());

        String base64Image = Base64.getEncoder().encodeToString(imageData);
        String imageExtension = "jpeg"; 
        if (originalFilename != null) {
            int lastDot = originalFilename.lastIndexOf('.');
            if (lastDot > 0 && lastDot < originalFilename.length() - 1) {
                imageExtension = originalFilename.substring(lastDot + 1).toLowerCase();
            }
        }

        String userDataSummary = buildUserDataSummary(userData);
        String systemPrompt = String.format("Eres un experto nutricionista que analiza imágenes de alimentos, recetas, comidas y proporciona información nutricional detallada y recomendaciones. Datos del usuario: %s. Al analizar esta imagen: 1. Identifica los alimentos o platos presentes. 2. Estima su valor nutricional aproximado (calorías, macronutrientes). 3. Evalúa si es adecuado para los objetivos del usuario. 4. Proporciona sugerencias o mejoras si corresponde. 5. Menciona posibles ingredientes que podrían causar problemas según las alergias o restricciones del usuario. Si la imagen muestra una receta o plan alimenticio, analízalo en detalle y comenta su idoneidad. Si es una etiqueta nutricional, interpreta la información y explica si es adecuada para el usuario. Si es un alimento o plato específico, proporciona alternativas más saludables si es necesario. Basa tus recomendaciones en evidencia científica y principios de nutrición sólidos.", userDataSummary);

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
            context.getLogger().log(Level.SEVERE, "Error calling OpenAI in NutritionFunction.processImageInternal: " + e.getMessage(), e);
            return "Lo siento, hubo un error al analizar la imagen.";
        }
    }

    private String createMealPlanWithJPA(String userInput, UserData userData, ExecutionContext context) {
        if (mealPlanGeneratorService == null) {
            context.getLogger().severe("MealPlanGeneratorService is not initialized!");
            return "Error: Servicio de plan de comidas no disponible.";
        }
        String mealPlanJson = mealPlanGeneratorService.generateMealPlanJson(userData);
        saveMealPlanToDB(userData.getId(), mealPlanJson, context);

        if (userInput.toLowerCase().contains("json") || userInput.toLowerCase().contains("raw")) {
            return mealPlanJson;
        }

        String systemPrompt = "Eres un dietista-nutricionista profesional especializado en explicar planes alimenticios personalizados. Debes explicar de forma clara y detallada el siguiente plan alimenticio generado para el usuario. El plan se presenta en formato JSON. Haz un resumen claro, destacando: 1. Las recomendaciones calóricas y de macronutrientes para el usuario. 2. Los aspectos más importantes del plan semanal. 3. Las características nutricionales de las comidas seleccionadas. 4. Cómo este plan se adapta a los objetivos y restricciones del usuario. Sé conciso pero informativo, enfocándote en la información más relevante para el usuario. Usa formato Markdown para la respuesta.";
        String userPromptForExplanation = String.format("Aquí está el plan alimenticio generado:\n%s\n\nExplica este plan de manera clara y concisa para el usuario.", mealPlanJson);

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(chatModelName)
                .addSystemMessage(systemPrompt)
                .addUserMessage(userPromptForExplanation)
                .build();
        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            String explanation = completion.choices().get(0).message().content();
            String saveMessage = String.format("\n\nSe ha guardado tu plan alimenticio en el sistema.");
            return explanation + saveMessage;
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error calling OpenAI for meal plan explanation: " + e.getMessage(), e);
            return "Hubo un error al generar la explicación, pero aquí tienes el plan en formato JSON:\n" + mealPlanJson;
        }
    }

    // --- JPA Persistence Methods ---
    private void saveOrUpdateUserData(UserData userData, ExecutionContext context) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();

            UserDataEntity entity = em.find(UserDataEntity.class, userData.getId());
            if (entity == null) { // New user
                entity = new UserDataEntity();
                entity.setUserId(userData.getId()); 
            }
            // Map fields from UserData (your model) to UserDataEntity (JPA entity)
            entity.setNombre(userData.getNombre());
            entity.setEdad(userData.getEdad());
            entity.setPeso(userData.getPeso());
            entity.setAltura(userData.getAltura());
            entity.setGenero(userData.getGenero());
            entity.setCondicionesMedicas(userData.getCondicionesMedicas());
            entity.setObjetivos(userData.getObjetivos());
            entity.setNivelActividad(userData.getNivelActividad());
            
            em.merge(entity); // Persist or update
            transaction.commit();
            context.getLogger().info("Saved/Updated UserData for user ID: " + userData.getId());
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            context.getLogger().log(Level.SEVERE, "Error saving/updating UserData for ID " + userData.getId() + ": " + e.getMessage(), e);
            // Optionally rethrow or handle as a critical error
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    private void saveMealPlanToDB(String userId, String mealPlanJson, ExecutionContext context) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();

            MealPlanEntity mealPlan = new MealPlanEntity();
            mealPlan.setUserId(userId);
            mealPlan.setPlanJsonContent(mealPlanJson);
            mealPlan.setCreatedAt(OffsetDateTime.now());
            
em.persist(mealPlan);
            transaction.commit();
            context.getLogger().info("Saved MealPlan for user ID: " + userId);
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            context.getLogger().log(Level.SEVERE, "Error saving MealPlan for user ID " + userId + ": " + e.getMessage(), e);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    public Optional<UserData> getUserDataFromDB(String userId, ExecutionContext context) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            UserDataEntity entity = em.find(UserDataEntity.class, userId);
            if (entity != null) {
                // Map from UserDataEntity back to your UserData model
                UserData model = new UserData(); // Assuming com.healthia.java.models.UserData
                model.setId(entity.getUserId());
                model.setNombre(entity.getNombre());
                model.setEdad(entity.getEdad());
                model.setPeso(entity.getPeso());
                model.setAltura(entity.getAltura());
                model.setGenero(entity.getGenero());
                model.setCondicionesMedicas(entity.getCondicionesMedicas());
                model.setObjetivos(entity.getObjetivos());
                model.setNivelActividad(entity.getNivelActividad());
                return Optional.of(model);
            }
            return Optional.empty();
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error fetching UserData for ID " + userId + ": " + e.getMessage(), e);
            return Optional.empty();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    // You might also want a method to retrieve the latest meal plan for a user
    public Optional<String> getLatestMealPlanJsonForUser(String userId, ExecutionContext context) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // This query assumes you want the most recently created plan.
            // Adjust if you have a different way of identifying the "latest" or "active" plan.
            TypedQuery<MealPlanEntity> query = em.createQuery(
                "SELECT mp FROM MealPlanEntity mp WHERE mp.userId = :userId ORDER BY mp.createdAt DESC", 
                MealPlanEntity.class
            );
            query.setParameter("userId", userId);
            query.setMaxResults(1); // Get only the latest one
            MealPlanEntity mealPlan = query.getSingleResult();
            return Optional.of(mealPlan.getPlanJsonContent());
        } catch (NoResultException e) {
            context.getLogger().info("No meal plan found for user ID: " + userId);
            return Optional.empty();
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error fetching latest meal plan for user ID " + userId + ": " + e.getMessage(), e);
            return Optional.empty();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    private String buildUserDataSummary(UserData userData) {
         return String.format("ID: %s, Nombre: %s, Edad: %s, Peso: %skg, Altura: %scm, Género: %s, Condiciones: %s, Objetivos: %s, Actividad: %s",
                Optional.ofNullable(userData.getId()).orElse("No especificado"),
                Optional.ofNullable(userData.getNombre()).orElse("No especificado"),
                Optional.ofNullable(userData.getEdad()).map(Object::toString).orElse("No especificada"),
                Optional.ofNullable(userData.getPeso()).map(Object::toString).orElse("No especificado"),
                Optional.ofNullable(userData.getAltura()).map(Object::toString).orElse("No especificada"),
                Optional.ofNullable(userData.getGenero()).orElse("No especificado"),
                Optional.ofNullable(userData.getCondicionesMedicas()).filter(l -> !l.isEmpty()).map(l -> String.join(", ", l)).orElse("Ninguna reportada"),
                Optional.ofNullable(userData.getObjetivos()).filter(l -> !l.isEmpty()).map(l -> String.join(", ", l)).orElse("No especificados"),
                Optional.ofNullable(userData.getNivelActividad()).orElse("No especificado")
        );
    }
} 