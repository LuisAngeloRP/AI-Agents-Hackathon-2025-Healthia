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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class OpenAIService {

    private static final Logger log = LoggerFactory.getLogger(OpenAIService.class);

    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper;
    // TODO: Inject S3Service if direct OpenAI calls need to upload images

    @Value("${app.openai.model}")
    private String chatModelName;

    @Value("${app.user-data-dir:data_usuario}")
    private String dataDir;
    @Value("${app.conversation-persistence-file:conversations.json}")
    private String persistenceFile;

    private Path jsonFilePath;

    // In-memory cache for conversations
    private final Map<Integer, ConversationHistory> conversations = new ConcurrentHashMap<>();

    @Autowired
    public OpenAIService(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @PostConstruct
    private void initialize() {
        try {
            Path dataPath = Paths.get(dataDir);
            Files.createDirectories(dataPath);
            jsonFilePath = dataPath.resolve(persistenceFile);
            log.info("Conversation persistence file path: {}", jsonFilePath);
            loadConversationsFromJson();
        } catch (IOException e) {
            log.error("Failed to create data directory or set JSON file path for conversations: {}", dataDir, e);
        }
    }

    // Method corresponding to chat_with_openai in Python
    // Handles direct calls when user message starts with @openai
    public ChatResponse chatWithOpenai(
            String message,
            int conversationId,
            InputType inputType,
            Path imagePath, // Path to temp image file if provided
            String originalFilename
    ) {
        log.info("Processing direct OpenAI request for conversation ID: {}. Type: {}. Image: {}",
                 conversationId, inputType, (imagePath != null));

        ConversationHistory history = conversations.computeIfAbsent(conversationId, id ->
                ConversationHistory.builder()
                        .id(id)
                        .title("Conversation #" + id)
                        .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .build()
        );

        // Add user message to history
        ConversationHistory.Message userMsg = ConversationHistory.Message.builder()
                .role("user")
                .content(message)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
        history.getMessages().add(userMsg);
        history.setLastUpdatedAt(userMsg.getTimestamp());

        // Prepare API call parameters
        List<ChatCompletionMessageParam> apiMessages = buildApiMessages(history);
        ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                .model(chatModelName)
                .messages(apiMessages);

        // Handle image input for vision model
        if (inputType == InputType.IMAGE && imagePath != null) {
             try {
                String base64Image = encodeImageToBase64(imagePath);
                String extension = getFileExtension(imagePath.getFileName().toString(), "jpg");
                ImageUrl imageUrl = ImageUrl.builder()
                    .url(String.format("data:image/%s;base64,%s", extension, base64Image))
                    .build();

                 // Modify the last user message to include the image
                UserMessage lastUserMessage = (UserMessage) apiMessages.get(apiMessages.size() - 1);
                List<UserMessageContentPart> contentParts = new ArrayList<>();
                contentParts.add(UserMessageContentPartText.builder().text(message).build());
                contentParts.add(UserMessageContentPartImage.builder().imageUrl(imageUrl).build());
                apiMessages.set(apiMessages.size() - 1, UserMessage.builder().content(contentParts).build());

                paramsBuilder.messages(apiMessages); // Update messages in builder

                 // TODO: S3 Upload if needed here?
                 // The Python code didn't explicitly upload to S3 in the @openai path
                 // Assuming we just pass base64 for now.

             } catch (IOException e) {
                  log.error("Failed to encode image for OpenAI call: {}", e.getMessage(), e);
                  // Decide how to handle - maybe proceed without image or return error?
                  // For now, log error and proceed with text only
                  // Reset messages to text-only version
                  apiMessages.set(apiMessages.size() - 1, UserMessage.builder().content(message).build());
                  paramsBuilder.messages(apiMessages);
             }
        }
        // TODO: Handle AUDIO input type if necessary

        // Call OpenAI API
        try {
            ChatCompletion completion = openAIClient.chat().completions().create(paramsBuilder.build());
            String responseContent = completion.choices().get(0).message().content();

            // Add assistant response to history
            ConversationHistory.Message assistantMsg = ConversationHistory.Message.builder()
                    .role("assistant")
                    .content(responseContent)
                    .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                    .build();
            history.getMessages().add(assistantMsg);
            history.setLastUpdatedAt(assistantMsg.getTimestamp());

            saveConversationsToJson(); // Persist the updated conversation

            return ChatResponse.builder()
                    .respuesta(responseContent)
                    .id(conversationId)
                    .title(history.getTitle())
                    .createdAt(history.getCreatedAt().toString()) // Return string representation
                    .mediaUrl(null) // No media URL generated in this path currently
                    .error(null)
                    .build();

        } catch (Exception e) {
            log.error("Error calling OpenAI for conversation ID {}: {}", conversationId, e.getMessage(), e);
            return ChatResponse.builder()
                    .respuesta("Error communicating with OpenAI.")
                    .id(conversationId)
                    .title(history.getTitle())
                    .createdAt(history.getCreatedAt().toString())
                    .error(e.getMessage())
                    .build();
        }
    }

    public boolean conversationExists(int chatId) {
        return conversations.containsKey(chatId);
    }

    public boolean deleteConversation(int chatId) {
        log.info("Deleting conversation ID: {}", chatId);
        ConversationHistory removed = conversations.remove(chatId);
        if (removed != null) {
            saveConversationsToJson(); // Persist the change
            log.info("Conversation ID {} deleted successfully.", chatId);
            return true;
        } else {
            log.warn("Conversation ID {} not found for deletion.", chatId);
            return false;
        }
    }

    public List<AllChatsResponse.ConversationDetail> getAllConversations() {
        log.info("Retrieving all conversations. Count: {}", conversations.size());
        return conversations.values().stream()
                .sorted(Comparator.comparing(ConversationHistory::getLastUpdatedAt).reversed()) // Sort by last updated
                .map(history -> AllChatsResponse.ConversationDetail.builder()
                        .id(history.getId())
                        .title(history.getTitle())
                        .messages(history.getMessages().stream()
                                .map(msg -> Map.of("role", msg.getRole(), "content", msg.getContent()))
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    // --- Persistence Methods ---

    private synchronized void loadConversationsFromJson() {
        if (jsonFilePath == null || !Files.exists(jsonFilePath)) {
            log.warn("Conversation persistence file not found: {}. Starting with empty map.", jsonFilePath);
            return;
        }
        try {
            Map<String, ConversationHistory> loadedMap = objectMapper.readValue(
                    jsonFilePath.toFile(),
                    new TypeReference<Map<String, ConversationHistory>>() {}
            );
            conversations.clear();
            loadedMap.forEach((idStr, history) -> conversations.put(Integer.parseInt(idStr), history));
            log.info("Loaded {} conversations from JSON.", conversations.size());
            // Optional: update nextId based on loaded IDs if needed for other services
        } catch (IOException e) {
            log.error("Failed to load conversations from {}: {}", jsonFilePath, e.getMessage(), e);
            conversations.clear(); // Start fresh if loading fails
        }
    }

    private synchronized void saveConversationsToJson() {
        if (jsonFilePath == null) {
            log.error("JSON file path not initialized. Cannot save conversations.");
            return;
        }
        try {
            Map<String, ConversationHistory> dataToSave = new HashMap<>();
            conversations.forEach((id, history) -> dataToSave.put(String.valueOf(id), history));
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFilePath.toFile(), dataToSave);
            log.debug("Saved {} conversations to {}", conversations.size(), jsonFilePath);
        } catch (IOException e) {
            log.error("Failed to save conversations to {}: {}", jsonFilePath, e.getMessage(), e);
        }
    }

    // --- Helper Methods ---

    private List<ChatCompletionMessageParam> buildApiMessages(ConversationHistory history) {
        // Convert stored messages to the format required by the OpenAI API
        return history.getMessages().stream()
                .map(msg -> {
                    if ("user".equalsIgnoreCase(msg.getRole())) {
                        return UserMessage.builder().content(msg.getContent()).build();
                    } else if ("assistant".equalsIgnoreCase(msg.getRole())) {
                        return AssistantMessage.builder().content(msg.getContent()).build();
                    } else if ("system".equalsIgnoreCase(msg.getRole())) {
                        return SystemMessage.builder().content(msg.getContent()).build();
                    } else {
                        log.warn("Unknown role in conversation history: {}", msg.getRole());
                        return UserMessage.builder().content(msg.getContent()).build(); // Default to user
                    }
                })
                .collect(Collectors.toList());
    }

    private String encodeImageToBase64(Path imagePath) throws IOException {
        byte[] imageBytes = Files.readAllBytes(imagePath);
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private String getFileExtension(String filename, String defaultExt) {
        if (filename == null) return defaultExt;
        int lastDot = filename.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return defaultExt;
    }
} 