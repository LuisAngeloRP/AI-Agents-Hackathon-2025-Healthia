package com.healthia.java.models;

import lombok.Builder;
import lombok.Value;

// Corresponds to ImageAnalysisHistoryResponse model in Python
@Value
@Builder
public class ImageAnalysisHistoryResponse extends AnalisisPlato {
    // Inherits all fields from AnalisisPlato
    // Add any additional fields specific to the history response if they exist
} 