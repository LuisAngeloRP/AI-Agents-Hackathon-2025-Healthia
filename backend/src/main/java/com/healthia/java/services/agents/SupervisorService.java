package com.healthia.java.services.agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.healthia.java.models.UserData;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ImageUrl;
import com.openai.models.chat.completions.SystemMessage;
import com.openai.models.chat.completions.UserMessage;
import com.openai.models.chat.completions.UserMessageContentPart;
import com.openai.models.chat.completions.UserMessageContentPartImage;
import com.openai.models.chat.completions.UserMessageContentPartText;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.lang.reflect.Field;

@Component
public class SupervisorService {

    private static final Logger log = LoggerFactory.getLogger(SupervisorService.class);

    @Value("${app.openai.model}") // Inject model name from properties
    private String chatModelName;

    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper = new ObjectMapper(); // For converting UserData to String
    private final Map<String, SpecializedAgent> agents = new HashMap<>();

    // Using the UserData POJO
    // TODO: This needs to be loaded/persisted if state is required across requests
    private UserData currentUserData = UserData.builder().id("defaultUser").build();

    private final String markdownInstruction = """
        INSTRUCCIÓN DE FORMATO IMPORTANTE:
        Cuando generes listas, planes o contenido estructurado, utiliza formato Markdown:

        # Encabezado principal (Título)
        ## Encabezado secundario (Sección)
        ### Encabezado terciario (Subsección)
        #### Encabezado de cuarto nivel
        ##### Encabezado de quinto nivel
        ###### Encabezado de sexto nivel

        Para listas:
        * Elemento de lista con viñeta
        * Otro elemento
          * Subítem anidado
          * Otro subítem

        Para listas numeradas:
        1. Primer paso
        2. Segundo paso
           1. Subpaso 1
           2. Subpaso 2

        Para enfatizar texto:
        **texto en negrita**
        *texto en cursiva*

        Para bloques de código o ejemplos:
        ```
        código o ejemplo aquí
        ```

        Para tablas:
        | Columna 1 | Columna 2 |
        |-----------|-----------|
        | Valor 1   | Valor 2   |
        """;

    @Autowired
    private NutritionAgent nutritionAgent;
    @Autowired
    private ExerciseAgent exerciseAgent;
    @Autowired
    private MedicalAgent medicalAgent;

    @Autowired
    public SupervisorService(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    @PostConstruct
    private void registerAgents() {
        registerAgent("nutricion", nutritionAgent);
        registerAgent("ejercicio", exerciseAgent);
        registerAgent("medico", medicalAgent);
        log.info("Specialized agents registered with Supervisor.");
    }

    public void registerAgent(String agentName, SpecializedAgent agentInstance) {
        agents.put(agentName, agentInstance);
    }

    public String processRequest(String userInput, Path imagePath) {
        log.info("Supervisor processing request. Input: '{}', ImagePath: {}", userInput, imagePath);

        if (imagePath != null) {
            return processImage(imagePath, userInput);
        }

        // Handle /datos command (Simplified parsing)
        if (userInput.startsWith("/datos")) {
            try {
                Map<String, String> updates = parseDatosCommand(userInput);
                updateUserDataFromMap(updates);
                return "[Agente Supervisor] He actualizado tus datos: " + updates;
            } catch (Exception e) {
                log.error("Error parsing /datos command: {}", userInput, e);
                return "[Agente Supervisor] Error al actualizar datos. Usa el formato: /datos clave1=valor1 clave2=valor2";
            }
        }

        // Check if it's a personal medical query
        if (isPersonalMedicalQuery(userInput)) {
            log.info("Query identified as personal medical. Routing to MedicalAgent.");
            SpecializedAgent medical = agents.get("medico");
            if (medical != null) {
                String mdEnhancedInput = userInput + "\n\n" + markdownInstruction;
                // Pass the current UserData object
                return "[Agente de Médico] " + medical.processWithUserData(mdEnhancedInput, currentUserData);
            }
        }

        // General agent selection
        String selectedAgentName = selectAgentForText(userInput);
        log.info("Selected agent for text query: {}", selectedAgentName);

        if (agents.containsKey(selectedAgentName)) {
            SpecializedAgent selectedAgent = agents.get(selectedAgentName);
            String mdEnhancedInput = userInput + "\n\n" + markdownInstruction;
            String response = selectedAgent.processWithUserData(mdEnhancedInput, currentUserData);
            return String.format("[Agente de %s] %s", capitalize(selectedAgentName), response);
        } else {
            // Fallback response
            return generateFallbackResponse(userInput);
        }
    }

    private String processImage(Path imagePath, String userPrompt) {
        log.info("Processing image: {}, Prompt: '{}'", imagePath, userPrompt);
        String base64Image;
        try {
            base64Image = encodeImageToBase64(imagePath);
        } catch (IOException e) {
            log.error("Error encoding image: {}", imagePath, e);
            return "[Agente Supervisor] Error al procesar la imagen.";
        }

        String selectedAgentName = selectAgentForImage(base64Image, userPrompt);
        log.info("Selected agent for image: {}", selectedAgentName);

        if (agents.containsKey(selectedAgentName)) {
            SpecializedAgent selectedAgent = agents.get(selectedAgentName);
            String mdEnhancedPrompt = (userPrompt != null ? userPrompt : "") + "\n\n" + markdownInstruction;
            // Pass the imagePath and UserData object
            String response = selectedAgent.processImage(imagePath, mdEnhancedPrompt, currentUserData);
            return String.format("[Agente de %s] %s", capitalize(selectedAgentName), response);
        } else {
            // Fallback for image
            return generateImageFallbackResponse(base64Image);
        }
    }

    private String selectAgentForImage(String base64Image, String userPrompt) {
        String systemPrompt = "Eres un analizador de imágenes que determina qué agente especializado debe procesarlas.";
        String userText = String.format("""
            Analiza la siguiente imagen y determina qué agente especializado
            debe manejarla. Los agentes disponibles son: %s

            Reglas para determinar el agente:
            - Si la imagen muestra algo médico (medicamentos, condiciones médicas, etc.), asignar al agente 'medico'
            - Si la imagen muestra comida, recetas, ingredientes o está relacionada con nutrición, asignar al agente 'nutricion'
            - Si la imagen muestra ejercicios, equipos de gimnasio, actividades físicas o dispositivos fitness, asignar al agente 'ejercicio'
            - Si no corresponde claramente a ninguna categoría, responder con 'ninguno'

            Texto adicional del usuario: %s

            Responde SOLO con el nombre del agente más adecuado: 'medico', 'nutricion', 'ejercicio' o 'ninguno'.
            """, agents.keySet(), userPrompt != null ? userPrompt : "");

        ImageUrl imageUrl = ImageUrl.builder()
                .url(String.format("data:image/jpeg;base64,%s", base64Image))
                // .detail(ImageUrlDetail.AUTO) // Default is AUTO
                .build();

        List<UserMessageContentPart> userContentParts = new ArrayList<>();
        userContentParts.add(UserMessageContentPartText.builder().text(userText).build());
        userContentParts.add(UserMessageContentPartImage.builder().imageUrl(imageUrl).build());

        List<ChatCompletionMessageParam> messages = List.of(
                SystemMessage.builder().content(systemPrompt).build(),
                UserMessage.builder().content(userContentParts).build()
        );

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(this.chatModelName) // Use injected property
                .messages(messages)
                .maxTokens(10) // Expecting a single word
                .build();

        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            String result = completion.choices().get(0).message().content().trim().toLowerCase();
            return result.isEmpty() || !agents.containsKey(result) ? "ninguno" : result;
        } catch (Exception e) {
            log.error("Error selecting agent for image: {}", e.getMessage(), e);
            return "ninguno"; // Fallback on error
        }
    }

    private boolean isPersonalMedicalQuery(String userInput) {
        String prompt = String.format("""
            Analiza si la siguiente consulta del usuario se refiere a SUS PROPIOS datos médicos personales,
            historial médico, medicamentos, condiciones de salud o resultados médicos.

            Consulta: "%s"

            Responde SOLO con "si" o "no".
            """, userInput);

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(this.chatModelName) // Use injected property
                .addSystemMessage("Eres un analizador preciso que determina si una consulta se refiere a datos médicos personales del usuario.")
                .addUserMessage(prompt)
                .maxTokens(5)
                .build();

        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            String response = completion.choices().get(0).message().content().trim().toLowerCase();
            return "si".equals(response);
        } catch (Exception e) {
            log.error("Error checking for personal medical query: {}", e.getMessage(), e);
            return false; // Assume not personal on error
        }
    }

    private String selectAgentForText(String userInput) {
        String userDataJson;
        try {
             // Convert relevant parts of UserData to JSON string for the prompt
            Map<String, Object> relevantData = Map.of(
                "nombre", currentUserData.getNombre(),
                "edad", currentUserData.getEdad(),
                "peso", currentUserData.getPeso(),
                "altura", currentUserData.getAltura(),
                "genero", currentUserData.getGenero(),
                "condiciones_medicas", currentUserData.getCondicionesMedicas(),
                "alergias", currentUserData.getAlergias(),
                "restricciones_alimentarias", currentUserData.getRestriccionesAlimentarias(),
                "objetivos", currentUserData.getObjetivos(),
                "nivel_actividad", currentUserData.getNivelActividad()
            );
            userDataJson = objectMapper.writeValueAsString(relevantData);
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize user data for prompt: {}", e.getMessage());
            userDataJson = "{}";
        }

        String prompt = String.format("""
            Analiza la siguiente solicitud del usuario y determina qué agente especializado
            debe manejarla. Los agentes disponibles son: %s

            Reglas claras para determinar el agente:
            1. Si la consulta está relacionada con ALIMENTACIÓN, DIETAS, COMIDAS, NUTRICIÓN, ALIMENTOS, RECETAS o PLANES ALIMENTICIOS, asignar al agente 'nutricion'
            2. Si la consulta está relacionada con EJERCICIO, ENTRENAMIENTO, ACTIVIDAD FÍSICA o RUTINAS DEPORTIVAS, asignar al agente 'ejercicio'
            3. Si la consulta está relacionada con MEDICAMENTOS, SÍNTOMAS, ENFERMEDADES o CONSULTAS MÉDICAS (que NO sean datos personales ya verificados), asignar al agente 'medico'
            4. Si no corresponde claramente a ninguna categoría, responder con 'ninguno'

            Datos del usuario:
            %s

            Solicitud del usuario: %s

            Responde ÚNICAMENTE con el nombre del agente que debe manejar esta consulta: 'nutricion', 'ejercicio', 'medico' o 'ninguno'.
            """, agents.keySet(), userDataJson, userInput);

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(this.chatModelName) // Use injected property
                .addSystemMessage("Eres un coordinador preciso que asigna consultas al agente especializado correcto, siguiendo estrictamente las reglas definidas.")
                .addUserMessage(prompt)
                .maxTokens(10)
                .build();

        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            String result = completion.choices().get(0).message().content().trim().toLowerCase();
             return result.isEmpty() || !agents.containsKey(result) ? "ninguno" : result;
        } catch (Exception e) {
            log.error("Error selecting agent for text: {}", e.getMessage(), e);
            return "ninguno";
        }
    }

    private String generateFallbackResponse(String userInput) {
        String systemPrompt = """
            Eres un asistente especializado en nutrición, ejercicio y salud.
            Responde concisamente (máximo 40 palabras) reconociendo brevemente lo que preguntó el usuario,
            pero indicando amablemente que solo puedes ayudar con temas de salud, nutrición o ejercicio.
            """;
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(this.chatModelName) // Use injected property
                .addSystemMessage(systemPrompt)
                .addUserMessage(userInput)
                .maxTokens(100)
                .build();
        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            return "[Agente Supervisor] " + completion.choices().get(0).message().content();
        } catch (Exception e) {
            log.error("Error generating fallback response: {}", e.getMessage(), e);
            return "[Agente Supervisor] Lo siento, no puedo ayudarte con eso en este momento.";
        }
    }

     private String generateImageFallbackResponse(String base64Image) {
        String systemPrompt = "Eres un asistente especializado en salud que responde brevemente.";
        String descriptionPrompt = """
            Describe muy brevemente lo que ves en esta imagen (máximo 20 palabras) y
            luego explica amablemente que solo puedes ayudar con temas de nutrición, ejercicio o salud.
            La respuesta completa debe ser concisa y natural.
            """;

        ImageUrl imageUrl = ImageUrl.builder()
                .url(String.format("data:image/jpeg;base64,%s", base64Image))
                .build();

        List<UserMessageContentPart> userContentParts = new ArrayList<>();
        userContentParts.add(UserMessageContentPartText.builder().text(descriptionPrompt).build());
        userContentParts.add(UserMessageContentPartImage.builder().imageUrl(imageUrl).build());

        List<ChatCompletionMessageParam> messages = List.of(
                SystemMessage.builder().content(systemPrompt).build(),
                UserMessage.builder().content(userContentParts).build()
        );

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(this.chatModelName) // Use injected property
                .messages(messages)
                .maxTokens(150)
                .build();

        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            return "[Agente Supervisor] " + completion.choices().get(0).message().content();
        } catch (Exception e) {
            log.error("Error generating image fallback response: {}", e.getMessage(), e);
            return "[Agente Supervisor] Lo siento, no puedo analizar esa imagen en este momento.";
        }
    }

    private String encodeImageToBase64(Path imagePath) throws IOException {
        byte[] imageBytes = Files.readAllBytes(imagePath);
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private Map<String, String> parseDatosCommand(String command) {
        Map<String, String> data = new HashMap<>();
        String payload = command.replace("/datos", "").trim();
        String[] pairs = payload.split("\s+"); // Split by whitespace
        for (String pair : pairs) {
            if (pair.contains("=")) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    data.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }
        return data;
    }

    // Replace placeholder updateUserDataFromMap
    public void updateUserDataFromMap(Map<String, String> data) {
        if (currentUserData == null) {
            log.warn("Cannot update null currentUserData");
            currentUserData = UserData.builder().id("defaultUser").build(); // Initialize if null
        }
        log.info("Attempting to update UserData with: {}", data);

        // Use Jackson ObjectMapper to help with type conversion
        ObjectNode userDataNode = objectMapper.valueToTree(currentUserData);

        data.forEach((key, valueStr) -> {
            try {
                Field field = findField(UserData.class, key);
                if (field != null) {
                    Object convertedValue = convertValue(valueStr, field.getType());
                    // Use Jackson node for easier update handling
                    JsonNode valueNode = objectMapper.convertValue(convertedValue, JsonNode.class);
                    userDataNode.set(key, valueNode);
                    log.debug("Updating field '{}' to {}", key, convertedValue);
                } else {
                    log.warn("Field '{}' not found in UserData class", key);
                }
            } catch (Exception e) {
                log.warn("Could not update field '{}' with value '{}': {}", key, valueStr, e.getMessage());
            }
        });

        // Convert the updated JsonNode back to UserData object
        try {
            currentUserData = objectMapper.treeToValue(userDataNode, UserData.class);
            log.info("Successfully updated UserData. Current state: {}", currentUserData);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert updated JsonNode back to UserData: {}", e.getMessage(), e);
        }
    }

    // Helper to find field by name (case-insensitive potentially)
    private Field findField(Class<?> clazz, String fieldName) {
         return Stream.of(clazz.getDeclaredFields())
                .filter(f -> f.getName().equalsIgnoreCase(fieldName))
                .findFirst()
                .orElse(null);
    }

    // Helper to convert String value based on target field type
    private Object convertValue(String valueStr, Class<?> targetType) {
        if (targetType == String.class) {
            return valueStr;
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(valueStr);
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(valueStr);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(valueStr.toLowerCase());
        } else if (List.class.isAssignableFrom(targetType)) {
            // Simple comma-separated list for strings, adjust if other list types are needed
            return Arrays.asList(valueStr.split(","));
        } else {
             log.warn("Unsupported target type for conversion: {}", targetType.getName());
             return valueStr; // Return original string if type is unknown
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // --- SpecializedAgent Interface ---
    public interface SpecializedAgent {
        String process(String input);
        String processWithUserData(String input, UserData userData);
        String processImage(Path imagePath, String prompt, UserData userData);
    }
} 