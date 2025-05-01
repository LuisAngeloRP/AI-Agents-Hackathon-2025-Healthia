package com.healthia.java.services.agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthia.java.models.UserData;
import com.healthia.java.services.MealPlanGeneratorService;
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
import java.util.*;
import java.util.stream.Collectors;

@Component
public class NutritionAgent implements SupervisorService.SpecializedAgent {

    private static final Logger log = LoggerFactory.getLogger(NutritionAgent.class);

    private final OpenAIClient openAIClient;
    private final MealPlanGeneratorService mealPlanGeneratorService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.user-data-dir:data_usuario}") // Default directory if not set in properties
    private String userDataDir;

    @Value("${app.openai.model}") // Inject model name from properties
    private String chatModelName;

    @Autowired
    public NutritionAgent(OpenAIClient openAIClient, MealPlanGeneratorService mealPlanGeneratorService) {
        this.openAIClient = openAIClient;
        this.mealPlanGeneratorService = mealPlanGeneratorService;
    }

    @PostConstruct
    private void init() {
        // Create user data directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(userDataDir));
            log.info("User data directory checked/created: {}", userDataDir);
        } catch (IOException e) {
            log.error("Could not create user data directory: {}", userDataDir, e);
        }
    }

    @Override
    public String process(String input) {
        log.info("NutritionAgent processing input (no user data).");
        String systemPrompt = """
        Eres un experto en nutrición que proporciona información precisa sobre alimentación
        saludable, nutrientes, dietas y recomendaciones alimentarias.

        Tus conocimientos incluyen:
        - Principios de una alimentación equilibrada
        - Propiedades y fuentes de diferentes nutrientes
        - Recomendaciones dietéticas para diferentes objetivos y condiciones
        - Alternativas alimentarias para diferentes restricciones dietéticas
        - Información sobre alimentos funcionales y suplementos

        Proporciona consejos claros, precisos y basados en evidencia científica. No promueves
        dietas extremas o no saludables. Cuando sea apropiado, personaliza tus recomendaciones
        considerando factores como objetivos de salud, preferencias personales y restricciones
        alimentarias.
        """;

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(this.chatModelName)
                .addSystemMessage(systemPrompt)
                .addUserMessage(input)
                .build();
        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            return completion.choices().get(0).message().content();
        } catch (Exception e) {
            log.error("Error calling OpenAI in NutritionAgent.process: {}", e.getMessage(), e);
            return "Lo siento, hubo un error al procesar tu consulta de nutrición.";
        }
    }

    @Override
    public String processWithUserData(String input, UserData userData) {
        log.info("NutritionAgent processing input for user: {}", userData.getId());
        saveUserData(userData); // Save data before processing

        String lowerInput = input.toLowerCase();
        if (lowerInput.contains("plan alimenticio") || lowerInput.contains("dieta") || lowerInput.contains("plan nutricional")) {
            log.info("Request identified as meal plan generation for user: {}", userData.getId());
            return createMealPlan(input, userData);
        }

        String userDataSummary = buildUserDataSummary(userData);
        String systemPrompt = String.format("""
        Eres un experto en nutrición que proporciona información precisa sobre alimentación
        saludable, nutrientes, dietas y recomendaciones alimentarias.

        Datos del usuario:
        %s

        Tus conocimientos incluyen:
        - Principios de una alimentación equilibrada
        - Propiedades y fuentes de diferentes nutrientes
        - Recomendaciones dietéticas para diferentes objetivos y condiciones
        - Alternativas alimentarias para diferentes restricciones dietéticas
        - Información sobre alimentos funcionales y suplementos

        Proporciona consejos claros, precisos y personalizados según los datos del usuario.
        No promueves dietas extremas o no saludables. Personaliza tus recomendaciones
        considerando los datos proporcionados sobre el usuario.
        """, userDataSummary);

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(this.chatModelName)
                .addSystemMessage(systemPrompt)
                .addUserMessage(input) // User input already includes markdown instructions from supervisor
                .build();
        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            return completion.choices().get(0).message().content();
        } catch (Exception e) {
            log.error("Error calling OpenAI in NutritionAgent.processWithUserData: {}", e.getMessage(), e);
            return "Lo siento, hubo un error al procesar tu consulta de nutrición personalizada.";
        }
    }

    @Override
    public String processImage(Path imagePath, String prompt, UserData userData) {
        log.info("NutritionAgent processing image for user: {}, Path: {}", userData.getId(), imagePath);
        saveUserData(userData); // Save data before processing

        String base64Image;
        try {
            base64Image = encodeImageToBase64(imagePath);
        } catch (IOException e) {
            log.error("Error encoding image: {}", imagePath, e);
            return "Error al procesar la imagen.";
        }

        String userDataSummary = buildUserDataSummary(userData);
        String systemPrompt = String.format("""
        Eres un experto nutricionista que analiza imágenes de alimentos, recetas, comidas y
        proporciona información nutricional detallada y recomendaciones.

        Datos del usuario:
        %s

        Cuando analices esta imagen:
        1. Identifica los alimentos o platos presentes
        2. Estima su valor nutricional aproximado (calorías, macronutrientes)
        3. Evalúa si es adecuado para los objetivos del usuario
        4. Proporciona sugerencias o mejoras si corresponde
        5. Menciona posibles ingredientes que podrían causar problemas según las alergias o restricciones del usuario

        Si la imagen muestra una receta o plan alimenticio, analízalo en detalle y comenta su idoneidad.
        Si es una etiqueta nutricional, interpreta la información y explica si es adecuada para el usuario.
        Si es un alimento o plato específico, proporciona alternativas más saludables si es necesario.

        Basa tus recomendaciones en evidencia científica y principios de nutrición sólidos.
        """, userDataSummary);

        ImageUrl imageUrl = ImageUrl.builder()
                .url(String.format("data:image/jpeg;base64,%s", base64Image))
                .build();

        List<UserMessageContentPart> userContentParts = new ArrayList<>();
        userContentParts.add(UserMessageContentPartText.builder().text(prompt).build()); // Prompt includes user text + markdown instructions
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
            return completion.choices().get(0).message().content();
        } catch (Exception e) {
            log.error("Error calling OpenAI in NutritionAgent.processImage: {}", e.getMessage(), e);
            return "Lo siento, hubo un error al analizar la imagen.";
        }
    }

    private String createMealPlan(String userInput, UserData userData) {
        // Generate the structured meal plan
        String mealPlanJson = mealPlanGeneratorService.generateMealPlanJson(userData);
        saveMealPlan(userData, mealPlanJson); // Save the generated plan

        // If user just wants the raw JSON
        if (userInput.toLowerCase().contains("json") || userInput.toLowerCase().contains("raw") || userInput.toLowerCase().contains("formato json")) {
            return mealPlanJson;
        }

        // Ask LLM to explain the generated plan
        String systemPrompt = """
        Eres un dietista-nutricionista profesional especializado en explicar planes alimenticios personalizados.

        Debes explicar de forma clara y detallada el siguiente plan alimenticio generado para el usuario.
        El plan se presenta en formato JSON con la siguiente estructura:
        - Información del usuario
        - Recomendaciones nutricionales con calorías y macronutrientes
        - Plan semanal con comidas para cada día (desayuno, almuerzo, merienda, cena)
        - Información nutricional y justificación de cada comida

        Haz un resumen claro, destacando:
        1. Las recomendaciones calóricas y de macronutrientes para el usuario
        2. Los aspectos más importantes del plan semanal
        3. Las características nutricionales de las comidas seleccionadas
        4. Cómo este plan se adapta a los objetivos y restricciones del usuario

        Sé conciso pero informativo, enfocándote en la información más relevante para el usuario.
        Usa formato Markdown para la respuesta.
        """;

        String userPromptForExplanation = String.format("Aquí está el plan alimenticio generado:\n%s\n\nExplica este plan de manera clara y concisa para el usuario.", mealPlanJson);

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(this.chatModelName)
                .addSystemMessage(systemPrompt)
                .addUserMessage(userPromptForExplanation)
                .build();
        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            String explanation = completion.choices().get(0).message().content();
            String saveMessage = String.format("\n\nSe ha guardado tu plan alimenticio en: %s", getMealPlanFilePath(userData));
            return explanation + saveMessage;
        } catch (Exception e) {
            log.error("Error calling OpenAI for meal plan explanation: {}", e.getMessage(), e);
            // Return the JSON plan as fallback if explanation fails
             String saveMessage = String.format("\n\nSe ha guardado tu plan alimenticio en: %s", getMealPlanFilePath(userData));
            return "Hubo un error al generar la explicación, pero aquí tienes el plan en formato JSON:\n" + mealPlanJson + saveMessage;
        }
    }

    // --- Persistence Methods ---

    private Path getUserDataFilePath(UserData userData) {
        String userId = Optional.ofNullable(userData.getId()).orElse("defaultUser");
        String filename = userId + "_datos.json";
        return Paths.get(userDataDir, filename);
    }

    private Path getMealPlanFilePath(UserData userData) {
         String userId = Optional.ofNullable(userData.getId()).orElse("defaultUser");
        String filename = userId + "_plan_alimenticio.json";
        return Paths.get(userDataDir, filename);
    }

    private void saveUserData(UserData userData) {
        Path filepath = getUserDataFilePath(userData);
        try {
            // Read existing data if file exists
            UserData dataToSave = userData;
            if (Files.exists(filepath)) {
                UserData existingData = objectMapper.readValue(filepath.toFile(), UserData.class);
                // Simple merge: overwrite existing with new, could be more sophisticated
                 existingData.setNombre(userData.getNombre());
                 existingData.setEdad(userData.getEdad());
                 existingData.setPeso(userData.getPeso());
                 // ... merge other fields as needed ...
                dataToSave = existingData; 
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(filepath.toFile(), dataToSave);
            log.info("Saved user data to: {}", filepath);
        } catch (IOException e) {
            log.error("Error saving user data to {}: {}", filepath, e.getMessage(), e);
        }
    }

    private void saveMealPlan(UserData userData, String mealPlanJson) {
        Path filepath = getMealPlanFilePath(userData);
        try {
            // Parse JSON string back to Map to save pretty printed
            Object mealPlanObject = objectMapper.readValue(mealPlanJson, Object.class);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(filepath.toFile(), mealPlanObject);
            log.info("Saved meal plan to: {}", filepath);
        } catch (IOException e) {
            log.error("Error saving meal plan to {}: {}", filepath, e.getMessage(), e);
        }
    }

     public Optional<UserData> getUserData(String userId) {
        Path filepath = Paths.get(userDataDir, userId + "_datos.json");
        if (!Files.exists(filepath)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(filepath.toFile(), UserData.class));
        } catch (IOException e) {
            log.error("Error loading user data for {}: {}", userId, e.getMessage(), e);
            return Optional.empty();
        }
    }

     public Optional<Map<String, Object>> getUserMealPlan(String userId) {
         Path filepath = Paths.get(userDataDir, userId + "_plan_alimenticio.json");
        if (!Files.exists(filepath)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(filepath.toFile(), new TypeReference<Map<String, Object>>() {}));
        } catch (IOException e) {
            log.error("Error loading meal plan for {}: {}", userId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    public List<String> listUsers() {
         File dir = new File(userDataDir);
         File[] files = dir.listFiles((d, name) -> name.endsWith("_datos.json"));
         if (files == null) {
             return Collections.emptyList();
         }
         return Arrays.stream(files)
                 .map(file -> file.getName().replace("_datos.json", ""))
                 .collect(Collectors.toList());
     }

    // --- Helper Methods ---

    private String encodeImageToBase64(Path imagePath) throws IOException {
        byte[] imageBytes = Files.readAllBytes(imagePath);
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private String buildUserDataSummary(UserData userData) {
        // Helper to create a string summary of user data for prompts
        return String.format("""
            - Nombre: %s
            - Edad: %s
            - Peso: %s kg
            - Altura: %s cm
            - Género: %s
            - Condiciones médicas: %s
            - Alergias: %s
            - Restricciones alimentarias: %s
            - Objetivos: %s
            - Nivel de actividad: %s
            - Preferencias alimentarias: %s
            """,
                Optional.ofNullable(userData.getNombre()).orElse("No especificado"),
                Optional.ofNullable(userData.getEdad()).map(Object::toString).orElse("No especificada"),
                Optional.ofNullable(userData.getPeso()).map(Object::toString).orElse("No especificado"),
                Optional.ofNullable(userData.getAltura()).map(Object::toString).orElse("No especificada"),
                Optional.ofNullable(userData.getGenero()).orElse("No especificado"),
                Optional.ofNullable(userData.getCondicionesMedicas()).filter(l -> !l.isEmpty()).map(l -> String.join(", ", l)).orElse("Ninguna reportada"),
                Optional.ofNullable(userData.getAlergias()).filter(l -> !l.isEmpty()).map(l -> String.join(", ", l)).orElse("Ninguna reportada"),
                Optional.ofNullable(userData.getRestriccionesAlimentarias()).filter(l -> !l.isEmpty()).map(l -> String.join(", ", l)).orElse("Ninguna reportada"),
                Optional.ofNullable(userData.getObjetivos()).filter(l -> !l.isEmpty()).map(l -> String.join(", ", l)).orElse("No especificados"),
                Optional.ofNullable(userData.getNivelActividad()).orElse("No especificado"),
                Optional.ofNullable(userData.getPreferenciasAlimentarias()).filter(l -> !l.isEmpty()).map(l -> String.join(", ", l)).orElse("No especificadas")
        );
    }
} 