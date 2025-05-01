package com.healthia.java.models;

import lombok.Builder;
import lombok.Value;
import java.time.OffsetDateTime;

// Corresponds to ChatResponse in Python
@Value
@Builder
public class ChatResponse {
    String respuesta;
    Integer id;
    String title;
    OffsetDateTime createdAt; // Using OffsetDateTime for time zone handling
    String mediaUrl;
    String error;
} 