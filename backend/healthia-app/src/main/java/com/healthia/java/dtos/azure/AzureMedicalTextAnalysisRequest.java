package com.healthia.java.dtos.azure;

import com.healthia.java.models.UserData; 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AzureMedicalTextAnalysisRequest {
    private String userInputText;
    private UserData userData; // Can be null if no specific user context
    private Integer conversationId; // For tracking
} 