package com.healthia.java.controllers;

import com.healthia.java.models.*;
import com.healthia.java.services.OpenAIService;
import com.healthia.java.services.S3Service;
import com.healthia.java.services.agents.SupervisorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Base64;

@RestController
@RequestMapping("/api/v1") // Using a base path for the API
public class ChatbotController {

    private static final Logger log = LoggerFactory.getLogger(ChatbotController.class);

    private final SupervisorService supervisorService;
    private final OpenAIService openAIService;
    private final S3Service s3Service;

    @Autowired
    public ChatbotController(SupervisorService supervisorService, OpenAIService openAIService, S3Service s3Service) {
        this.supervisorService = supervisorService;
        this.openAIService = openAIService;
        this.s3Service = s3Service;
    }

    // Handles JSON requests
    @PutMapping(value = "/chatbot", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatResponse> chatbotJson(@RequestBody ChatRequest chatRequest) {
        log.info("Received /chatbot [JSON] request for ID: {}", chatRequest.getId());
        if (chatRequest.getMessage() == null || chatRequest.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los campos 'message' e 'id' son obligatorios en JSON");
        }
        return processChatRequestInternal(
                chatRequest.getMessage(),
                chatRequest.getId(),
                chatRequest.getType(),
                null, // No multipart file in JSON request
                chatRequest.getMediaContent(), // Could be base64 data or null
                chatRequest.getOriginalFilename()
        );
    }

    // Handles Form Data requests
    @PutMapping(value = "/chatbot", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatResponse> chatbotForm(
            @RequestParam("message") String message,
            @RequestParam("id") Integer id,
            @RequestParam(value = "type", required = false, defaultValue = "TEXT") InputType type,
            @RequestPart(value = "media_file", required = false) MultipartFile mediaFile
    ) {
        log.info("Received /chatbot [Form] request for ID: {}. File attached: {}", id, (mediaFile != null && !mediaFile.isEmpty()));
        if (message == null || id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los campos 'message' e 'id' son obligatorios en Form");
        }
        return processChatRequestInternal(
                message,
                id,
                type,
                mediaFile,
                null, // No separate media content field in form data
                (mediaFile != null) ? mediaFile.getOriginalFilename() : null
        );
    }

    // Internal processing logic
    private ResponseEntity<ChatResponse> processChatRequestInternal(
            String message,
            int id,
            InputType inputType,
            MultipartFile mediaFile,
            String mediaContentJson, // Used for potential base64 in JSON
            String originalFilename
    ) {
        Path tempImagePath = null;
        String responseContent;
        String mediaUrl = null; // Will be populated by S3 upload
        byte[] imageData = null; // Hold image data for S3 upload
        String imageExtension = "tmp"; // Default extension

        try {
            // --- Handle potential image data --- 
            if (inputType == InputType.IMAGE) {
                InputStream imageInputStream = null;
                long imageSize = -1;

                if (mediaFile != null && !mediaFile.isEmpty()) {
                    log.info("Processing image from multipart file: {}", mediaFile.getOriginalFilename());
                    originalFilename = mediaFile.getOriginalFilename(); // Ensure we use the multipart filename
                    imageExtension = getFileExtension(originalFilename);
                    imageData = mediaFile.getBytes(); // Read into memory for S3 upload
                    imageInputStream = new ByteArrayInputStream(imageData);
                    imageSize = imageData.length;
                } else if (mediaContentJson != null && mediaContentJson.startsWith("data:image")) {
                    log.info("Processing image from base64 string in JSON request");
                    String base64Data = mediaContentJson.contains(",") ? mediaContentJson.substring(mediaContentJson.indexOf(",") + 1) : mediaContentJson;
                    try {
                        imageData = Base64.getDecoder().decode(base64Data);
                        imageExtension = extractExtensionFromDataUri(mediaContentJson, "jpg");
                         // If originalFilename wasn't provided in JSON, create one
                        if (originalFilename == null) {
                             originalFilename = "image_from_base64." + imageExtension;
                        }
                         imageInputStream = new ByteArrayInputStream(imageData);
                         imageSize = imageData.length;
                    } catch (IllegalArgumentException e) {
                         log.error("Invalid Base64 data provided in JSON", e);
                         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Base64 de imagen inválido en JSON", e);
                    }
                } else {
                     throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de entrada 'IMAGE' requiere 'media_file' (form) o 'media_content' (json base64)");
                }

                // Save to temporary file for supervisor/OpenAI service (if they need Path)
                String tempFileSuffix = "." + imageExtension;
                tempImagePath = Files.createTempFile("healthia_upload_", tempFileSuffix);
                Files.copy(new ByteArrayInputStream(imageData), tempImagePath, StandardCopyOption.REPLACE_EXISTING); // Use copied stream
                log.info("Image saved temporarily to: {}", tempImagePath);

                // Upload to S3
                log.info("Uploading image to S3...");
                 Map<String, Object> s3Result = s3Service.uploadFileToS3(
                         new ByteArrayInputStream(imageData), // Provide a new stream from bytes
                         imageSize,
                         imageExtension,
                         id,
                         originalFilename,
                         null // Use default S3 folder configured in properties
                 );
                 if (Boolean.TRUE.equals(s3Result.get("success"))) {
                     mediaUrl = (String) s3Result.get("url");
                     log.info("Image uploaded to S3: {}", mediaUrl);
                 } else {
                     log.warn("S3 upload failed: {}. Proceeding without S3 URL.", s3Result.get("error"));
                     // Fallback: Python code saved locally. Here we just proceed without URL.
                     // Could implement local saving/serving as a fallback if needed.
                 }
            }

            // --- Call appropriate service --- 
            if (message.startsWith("@openai")) {
                 log.info("Routing request directly to OpenAIService for ID: {}", id);
                ChatResponse directResponse = openAIService.chatWithOpenai(
                        message.substring(7).trim(),
                        id,
                        inputType,
                        tempImagePath, // Pass temp path for direct OpenAI call if needed
                        originalFilename
                );
                responseContent = directResponse.getRespuesta();
                // Use S3 mediaUrl if available, otherwise keep OpenAI service one (likely null)
                mediaUrl = (mediaUrl != null) ? mediaUrl : directResponse.getMediaUrl();
            } else {
                 log.info("Routing request to SupervisorService for ID: {}", id);
                 // Supervisor expects Path for image
                responseContent = supervisorService.processRequest(message, tempImagePath);
                // S3 upload was handled before calling supervisor if image was present
            }

            // --- Construct final response --- 
            ChatResponse response = ChatResponse.builder()
                    .respuesta(responseContent)
                    .id(id)
                    .title(String.format("Conversación #%d", id))
                    .createdAt(OffsetDateTime.now(ZoneOffset.UTC)) // Use UTC time
                    .mediaUrl(mediaUrl) // Include S3 URL if generated
                    .error(null)
                    .build();

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("IOException processing request for ID {}: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error de I/O al procesar archivo", e);
        } catch (ResponseStatusException rse) { // Re-throw client errors
             throw rse;
        } catch (Exception e) {
            log.error("Error processing chatbot request for ID {}: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al procesar la solicitud", e);
        } finally {
            // Clean up temporary file
            if (tempImagePath != null) {
                try {
                    Files.deleteIfExists(tempImagePath);
                    log.info("Deleted temporary image file: {}", tempImagePath);
                } catch (IOException e) {
                    log.error("Failed to delete temporary image file: {}", tempImagePath, e);
                }
            }
        }
    }


    @DeleteMapping("/delete-chat/{chat_id}")
    public ResponseEntity<Map<String, String>> deleteChat(@PathVariable("chat_id") int chatId) {
        log.info("Received /delete-chat request for ID: {}", chatId);
        try {
            boolean exists = openAIService.conversationExists(chatId);
            if (!exists) {
                 throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la conversación con ID " + chatId);
            }
            boolean success = openAIService.deleteConversation(chatId);
            if (success) {
                return ResponseEntity.ok(Collections.singletonMap("mensaje", "Conversación con ID " + chatId + " eliminada correctamente"));
            } else {
                 throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo eliminar la conversación con ID " + chatId);
            }
        } catch (ResponseStatusException rse) {
            throw rse; // Re-throw specific HTTP exceptions
        } catch (Exception e) {
             log.error("Error deleting chat ID {}: {}", chatId, e.getMessage(), e);
             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al eliminar la conversación", e);
        }
    }

    @GetMapping("/show-chats")
    public ResponseEntity<AllChatsResponse> showAllChats() {
        log.info("Received /show-chats request");
        try {
             List<AllChatsResponse.ConversationDetail> chats = openAIService.getAllConversations();
             return ResponseEntity.ok(AllChatsResponse.builder().chats(chats).build());
        } catch (Exception e) {
             log.error("Error retrieving all chats: {}", e.getMessage(), e);
             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al obtener las conversaciones", e);
        }
    }

     private String getFileExtension(String filename) {
        if (filename == null) {
            return "tmp";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return "tmp";
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
} 