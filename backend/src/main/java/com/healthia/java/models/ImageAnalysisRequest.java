package com.healthia.java.models;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;

// Corresponds to ImageAnalysisRequest in Python
@Value
@Builder
public class ImageAnalysisRequest {
    @NotBlank
    String imageBase64;
    Integer conversationId; // Optional ID
} 