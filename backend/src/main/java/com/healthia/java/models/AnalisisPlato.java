package com.healthia.java.models;

import lombok.Builder;
import lombok.Value;
import java.util.List;
import java.time.OffsetDateTime;

// Corresponds to AnalisisPlato and nested models in Python
// This will likely need refinement based on the exact structure returned by ImageAnalysisService
@Value
@Builder
public class AnalisisPlato {
    String evaluacionGeneral;
    List<AlimentoDetalle> detallesAlimentos;
    String imagenOriginalUrl;
    String imagenProcesadaUrl;
    Integer id;
    OffsetDateTime fecha;
    NutricionDetalle nutricion;
    List<String> recomendaciones;

    @Value
    @Builder
    public static class AlimentoDetalle {
        String nombre;
        String categoria;
        Double areaOcupadaPorcentaje;
        List<Integer> coordenadasBoundingBox; // Assuming [x1, y1, x2, y2]
    }

    @Value
    @Builder
    public static class NutricionDetalle {
        Double porcentajeVerduras;
        Double porcentajeProteinas;
        Double porcentajeCarbohidratos;
    }
} 