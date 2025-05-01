package com.healthia.java.dtos.azure;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AzureAnalisisPlato {
    private String evaluacionGeneral;
    private Map<String, Object> nutricion; // Simplified as a map for now
    private List<Map<String, Object>> detallesAlimentos; // Simplified as a list of maps
    private List<String> recomendaciones;
    private String imagenOriginalUrl;
    private String imagenProcesadaUrl;
    // Add other fields as needed, mirroring AnalisisPlato from the function
} 