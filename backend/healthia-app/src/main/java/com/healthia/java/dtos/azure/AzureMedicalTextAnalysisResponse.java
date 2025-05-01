package com.healthia.java.dtos.azure;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AzureMedicalTextAnalysisResponse {
    private String analysisResult;
    private String disclaimer;
    private Integer conversationId;
} 