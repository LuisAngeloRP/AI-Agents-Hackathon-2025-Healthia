package com.healthia.java.models;

import lombok.Builder;
import lombok.Value;
import java.util.List;
import java.util.Map;

// Corresponds to AllChatsResponse in Python (assuming it needs conversation details)
// Structure might need adjustment based on actual implementation of get_all_conversations
@Value
@Builder
public class AllChatsResponse {
    List<ConversationDetail> chats;

    // Placeholder for conversation details - adjust as needed
    @Value
    @Builder
    public static class ConversationDetail {
        Integer id;
        String title;
        List<Map<String, String>> messages; // Example: List of {"role": "user", "content": "..."}
    }
} 