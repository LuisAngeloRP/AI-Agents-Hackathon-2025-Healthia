package com.healthia.java.models;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationHistory {

    private int id;
    private String title;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastUpdatedAt;
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Message {
        private String role; // e.g., "user", "assistant", "system"
        private String content;
        private OffsetDateTime timestamp;
        // Add other relevant fields if needed, like media URLs associated with a message
    }
} 