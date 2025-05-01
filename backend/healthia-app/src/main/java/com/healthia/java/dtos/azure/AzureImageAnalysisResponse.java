package com.healthia.java.dtos.azure;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AzureImageAnalysisResponse {
    private AzureAnalisisPlato analisis;
    // Add other fields from ImageAnalysisHistoryResponse if needed (e.g., id, fecha)
    private Integer id;
    private String fecha; // Using String for simplicity, consider OffsetDateTime if precision needed
} 