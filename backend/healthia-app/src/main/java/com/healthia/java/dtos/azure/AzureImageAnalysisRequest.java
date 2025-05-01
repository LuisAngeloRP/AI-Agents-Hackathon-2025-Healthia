package com.healthia.java.dtos.azure;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AzureImageAnalysisRequest {
    private String imageBase64;
    private Integer conversationId; // Optional, can be used for tracking/linking
} 