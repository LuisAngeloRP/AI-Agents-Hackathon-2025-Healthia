package com.healthia.java.models;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Corresponds to DeleteImageAnalysisRequest model in Python
@Data // Using @Data for potential mutability if needed, or use @Value
public class DeleteImageAnalysisRequest {
    @NotNull
    Integer id;
} 