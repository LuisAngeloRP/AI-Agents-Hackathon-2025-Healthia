package com.healthia.java.models;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

// InputType enum moved to its own file (InputType.java)

// Corresponds to ChatRequest in Python
@Value
@Builder
public class ChatRequest {
    @NotNull
    String message;
    @NotNull
    Integer id;
    @Builder.Default
    InputType type = InputType.TEXT; // Now references the public enum
    String mediaContent; // Could be base64 string or URL
    String originalFilename;
} 