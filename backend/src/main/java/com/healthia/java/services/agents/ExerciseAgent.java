package com.healthia.java.services.agents;

import com.healthia.java.models.UserData;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
public class ExerciseAgent implements SupervisorService.SpecializedAgent {

    private static final Logger log = LoggerFactory.getLogger(ExerciseAgent.class);

    @Value("${app.openai.model}")
    private String chatModelName;

    private final OpenAIClient openAIClient;

    @Autowired
    public ExerciseAgent(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    @Override
    public String process(String input) {
        log.info("ExerciseAgent processing input (no user data).");
        String systemPrompt = """
        Eres un entrenador personal experto que proporciona información precisa sobre ejercicios,
        rutinas de entrenamiento, técnicas correctas y recomendaciones personalizadas.

        Tus conocimientos incluyen:
        - Diferentes tipos de ejercicios (cardiovasculares, fuerza, flexibilidad, etc.)
        - Técnicas correctas para evitar lesiones
        - Rutinas para diferentes objetivos (pérdida de peso, ganancia muscular, resistencia, etc.)
        - Adaptaciones para diferentes niveles de condición física
        - Recomendaciones para problemas específicos

        Proporciona respuestas claras, precisas y personalizadas. Cuando sea apropiado,
        sugiere ejercicios específicos con instrucciones detalladas.
        Usa formato Markdown.
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
            log.error("Error calling OpenAI in ExerciseAgent.process: {}", e.getMessage(), e);
            return "Lo siento, hubo un error al procesar tu consulta de ejercicio.";
        }
    }

    @Override
    public String processWithUserData(String input, UserData userData) {
        log.info("ExerciseAgent processing input for user: {}", userData.getId());
        String userDataSummary = buildUserDataSummary(userData);

        String systemPrompt = String.format("""
        Eres un entrenador personal experto que proporciona información precisa sobre ejercicios,
        rutinas de entrenamiento, técnicas correctas y recomendaciones personalizadas.

        Datos del usuario:
        %s

        Tus conocimientos incluyen:
        - Diferentes tipos de ejercicios (cardiovasculares, fuerza, flexibilidad, etc.)
        - Técnicas correctas para evitar lesiones
        - Rutinas para diferentes objetivos (pérdida de peso, ganancia muscular, resistencia, etc.)
        - Adaptaciones para diferentes niveles de condición física
        - Recomendaciones para problemas específicos

        Proporciona respuestas claras, precisas y personalizadas según los datos del usuario.
        Cuando sea apropiado, sugiere ejercicios específicos con instrucciones detalladas y
        adaptados a las características particulares del usuario.

        Ten especial cuidado con las condiciones médicas informadas y adapta tus recomendaciones
        para que sean seguras y apropiadas para el usuario.
        Usa formato Markdown.
        """, userDataSummary);

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(this.chatModelName)
                .addSystemMessage(systemPrompt)
                .addUserMessage(input) // Includes markdown instructions
                .build();
        try {
            ChatCompletion completion = openAIClient.chat().completions().create(params);
            return completion.choices().get(0).message().content();
        } catch (Exception e) {
            log.error("Error calling OpenAI in ExerciseAgent.processWithUserData: {}", e.getMessage(), e);
            return "Lo siento, hubo un error al procesar tu consulta de ejercicio personalizada.";
        }
    }

    @Override
    public String processImage(Path imagePath, String prompt, UserData userData) {
        log.info("ExerciseAgent processing image for user: {}, Path: {}", userData.getId(), imagePath);
        String base64Image;
        try {
            base64Image = encodeImageToBase64(imagePath);
        } catch (IOException e) {
            log.error("Error encoding image: {}", imagePath, e);
            return "Error al procesar la imagen.";
        }

        String userDataSummary = buildUserDataSummary(userData);
        String systemPrompt = String.format("""
        Eres un entrenador personal experto que analiza imágenes relacionadas con ejercicios,
        actividad física, equipos de gimnasio, postura, técnica deportiva y dispositivos de fitness.

        Datos del usuario:
        %s

        Al analizar esta imagen:
        1. Identifica el tipo de ejercicio, equipo o actividad mostrada
        2. Evalúa la técnica o postura si es visible (sin juzgar, solo informar)
        3. Proporciona recomendaciones para mejorar o adaptar el ejercicio al nivel y objetivos del usuario
        4. Sugiere variaciones o alternativas si corresponde
        5. Considera las condiciones médicas del usuario al dar recomendaciones

        Si la imagen muestra:
        - Un ejercicio específico: explica la técnica correcta, músculos trabajados y beneficios
        - Un equipo de gimnasio: describe su uso adecuado y ejercicios posibles con él
        - Datos de entrenamiento (smartwatch, app): interpreta los datos y sugiere mejoras
        - Un plan de entrenamiento: analiza su idoneidad para los objetivos del usuario

        Proporciona consejos prácticos, científicamente respaldados y adaptados a las características
        particulares del usuario. Enfatiza la seguridad y la correcta ejecución.
        Usa formato Markdown.
        """, userDataSummary);

         ImageUrl imageUrl = ImageUrl.builder()
                .url(String.format("data:image/jpeg;base64,%s", base64Image))
                .build();

        List<UserMessageContentPart> userContentParts = new ArrayList<>();
        userContentParts.add(UserMessageContentPartText.builder().text(prompt).build()); // Includes markdown instructions
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
            log.error("Error calling OpenAI in ExerciseAgent.processImage: {}", e.getMessage(), e);
            return "Lo siento, hubo un error al analizar la imagen de ejercicio.";
        }
    }

    private String encodeImageToBase64(Path imagePath) throws IOException {
        byte[] imageBytes = Files.readAllBytes(imagePath);
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private String buildUserDataSummary(UserData userData) {
        // Reusing the summary logic, could be extracted to a common utility
         return String.format("""
            - Nombre: %s
            - Edad: %s
            - Peso: %s kg
            - Altura: %s cm
            - Género: %s
            - Condiciones médicas: %s
            - Objetivos: %s
            - Nivel de actividad: %s
            """,
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