package com.healthia.functions.util;

import com.healthia.functions.entities.*;
import com.healthia.java.models.AnalisisPlato;
import com.healthia.java.models.ImageAnalysisHistoryResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ImageAnalysisDtoConverter {

    // Helper to convert List<Integer> [x1, y1, x2, y2] to CoordenadasEntity
    private static CoordenadasEntity convertBoundingBoxToCoordenadasEntity(List<Integer> bbox) {
        if (bbox == null || bbox.size() != 4) {
            // Return a default or null, or throw exception, based on desired handling
            return CoordenadasEntity.builder().x1(0).y1(0).x2(0).y2(0).build(); 
        }
        return CoordenadasEntity.builder()
                .x1(bbox.get(0))
                .y1(bbox.get(1))
                .x2(bbox.get(2))
                .y2(bbox.get(3))
                .build();
    }

    // Helper to convert CoordenadasEntity to List<Integer> [x1, y1, x2, y2]
    private static List<Integer> convertCoordenadasEntityToBoundingBox(CoordenadasEntity coordenadas) {
        if (coordenadas == null) {
            return List.of(0, 0, 0, 0); // Or null, or empty list
        }
        return List.of(coordenadas.getX1(), coordenadas.getY1(), coordenadas.getX2(), coordenadas.getY2());
    }

    public static CoordenadasEntity convertCoordenadasToEntity(AnalisisPlato.AlimentoDetalle.Coordenadas source) {
        if (source == null) return null;
        return CoordenadasEntity.builder()
                .x1(source.getX1())
                .y1(source.getY1())
                .x2(source.getX2())
                .y2(source.getY2())
                .build();
    }

    public static DetalleAlimentoEntity convertAlimentoDetalleToEntity(AnalisisPlato.AlimentoDetalle source) {
        if (source == null) return null;
        return DetalleAlimentoEntity.builder()
                .nombre(source.getNombre())
                .categoria(source.getCategoria())
                .porcentajeArea(source.getAreaOcupadaPorcentaje())
                .coordenadas(convertBoundingBoxToCoordenadasEntity(source.getCoordenadasBoundingBox()))
                .build();
    }

    public static AnalisisPlatoEntity convertAnalisisPlatoDtoToEntity(AnalisisPlato sourceDto) {
        if (sourceDto == null) return null;

        AnalisisPlatoEntity entity = AnalisisPlatoEntity.builder()
                .evaluacionGeneral(sourceDto.getEvaluacionGeneral())
                .porcentajeVerduras(sourceDto.getNutricion() != null ? sourceDto.getNutricion().getPorcentajeVerduras() : null)
                .porcentajeProteinas(sourceDto.getNutricion() != null ? sourceDto.getNutricion().getPorcentajeProteinas() : null)
                .porcentajeCarbohidratos(sourceDto.getNutricion() != null ? sourceDto.getNutricion().getPorcentajeCarbohidratos() : null)
                .recomendaciones(sourceDto.getRecomendaciones() != null ? new ArrayList<>(sourceDto.getRecomendaciones()) : new ArrayList<>())
                .imagenOriginalUrl(sourceDto.getImagenOriginalUrl())
                .imagenProcesadaUrl(sourceDto.getImagenProcesadaUrl())
                .build();

        if (sourceDto.getDetallesAlimentos() != null) {
            entity.setDetalleAlimentos(
                sourceDto.getDetallesAlimentos().stream()
                    .map(ImageAnalysisDtoConverter::convertAlimentoDetalleToEntity)
                    .peek(detalleEntity -> detalleEntity.setAnalisisPlato(entity)) // Set bidirectional link
                    .collect(Collectors.toList())
            );
        } else {
            entity.setDetalleAlimentos(new ArrayList<>());
        }
        return entity;
    }
    
    public static ImageAnalysisHistoryEntity convertImageAnalysisHistoryResponseToEntity(ImageAnalysisHistoryResponse source) {
        if (source == null) return null;

        // ImageAnalysisHistoryResponse extends AnalisisPlato, so we directly use its fields for AnalisisPlatoEntity part
        // and its specific fields for ImageAnalysisHistoryEntity.
        
        AnalisisPlatoEntity analisisPlatoEntity = AnalisisPlatoEntity.builder()
            .evaluacionGeneral(source.getEvaluacionGeneral())
            .porcentajeVerduras(source.getNutricion() != null ? source.getNutricion().getPorcentajeVerduras() : null)
            .porcentajeProteinas(source.getNutricion() != null ? source.getNutricion().getPorcentajeProteinas() : null)
            .porcentajeCarbohidratos(source.getNutricion() != null ? source.getNutricion().getPorcentajeCarbohidratos() : null)
            .recomendaciones(source.getRecomendaciones() != null ? new ArrayList<>(source.getRecomendaciones()) : new ArrayList<>())
            .imagenOriginalUrl(source.getImagenOriginalUrl()) // from AnalisisPlato part
            .imagenProcesadaUrl(source.getImagenProcesadaUrl()) // from AnalisisPlato part
            .build();

        if (source.getDetallesAlimentos() != null) {
            analisisPlatoEntity.setDetalleAlimentos(
                source.getDetallesAlimentos().stream()
                    .map(ImageAnalysisDtoConverter::convertAlimentoDetalleToEntity)
                    .peek(detalleEntity -> detalleEntity.setAnalisisPlato(analisisPlatoEntity))
                    .collect(Collectors.toList())
            );
        } else {
            analisisPlatoEntity.setDetalleAlimentos(new ArrayList<>());
        }

        ImageAnalysisHistoryEntity historyEntity = ImageAnalysisHistoryEntity.builder()
                .id(source.getId()) // ID from the ImageAnalysisHistoryResponse
                .fecha(source.getFecha()) // Fecha from the ImageAnalysisHistoryResponse
                .imagenOriginalUrl(source.getImagenOriginalUrl()) // Can also be from AnalisisPlato part, ensure consistency
                .imagenProcesadaUrl(source.getImagenProcesadaUrl()) // Can also be from AnalisisPlato part
                .build();

        historyEntity.setAnalisis(analisisPlatoEntity); // Links AnalisisPlatoEntity to History, and sets back-reference

        return historyEntity;
    }

    // --- Conversion from Entity to DTO (if needed for responses) ---

    public static AnalisisPlato.AlimentoDetalle.Coordenadas convertCoordenadasEntityToDto(CoordenadasEntity source) {
        if (source == null) return null;
        return AnalisisPlato.AlimentoDetalle.Coordenadas.builder()
            .x1(source.getX1())
            .y1(source.getY1())
            .x2(source.getX2())
            .y2(source.getY2())
            .build();
    }

    public static AnalisisPlato.AlimentoDetalle convertDetalleAlimentoEntityToDto(DetalleAlimentoEntity source) {
        if (source == null) return null;
        return AnalisisPlato.AlimentoDetalle.builder()
                .nombre(source.getNombre())
                .categoria(source.getCategoria())
                .areaOcupadaPorcentaje(source.getPorcentajeArea())
                .coordenadasBoundingBox(convertCoordenadasEntityToBoundingBox(source.getCoordenadas()))
                .build();
    }

    public static AnalisisPlato convertAnalisisPlatoEntityToDto(AnalisisPlatoEntity sourceEntity, ImageAnalysisHistoryEntity historySource) {
        if (sourceEntity == null) return null;
        
        AnalisisPlato.NutricionDetalle nutricionDto = (sourceEntity.getPorcentajeVerduras() != null ||
                                                     sourceEntity.getPorcentajeProteinas() != null ||
                                                     sourceEntity.getPorcentajeCarbohidratos() != null) ?
            AnalisisPlato.NutricionDetalle.builder()
                .porcentajeVerduras(sourceEntity.getPorcentajeVerduras())
                .porcentajeProteinas(sourceEntity.getPorcentajeProteinas())
                .porcentajeCarbohidratos(sourceEntity.getPorcentajeCarbohidratos())
                .build()
            : null;

        return AnalisisPlato.builder()
                .id(historySource != null ? historySource.getId() : null) 
                .fecha(historySource != null ? historySource.getFecha() : null) 
                .evaluacionGeneral(sourceEntity.getEvaluacionGeneral())
                .nutricion(nutricionDto)
                .detallesAlimentos(sourceEntity.getDetalleAlimentos() != null ? 
                    sourceEntity.getDetalleAlimentos().stream()
                        .map(ImageAnalysisDtoConverter::convertDetalleAlimentoEntityToDto)
                        .collect(Collectors.toList()) 
                    : new ArrayList<>())
                .recomendaciones(sourceEntity.getRecomendaciones() != null ? new ArrayList<>(sourceEntity.getRecomendaciones()) : new ArrayList<>())
                .imagenOriginalUrl(sourceEntity.getImagenOriginalUrl())
                .imagenProcesadaUrl(sourceEntity.getImagenProcesadaUrl())
                .build();
    }

    public static ImageAnalysisHistoryResponse convertImageAnalysisHistoryEntityToDto(ImageAnalysisHistoryEntity sourceHistoryEntity) {
        if (sourceHistoryEntity == null) return null;
        
        AnalisisPlato analisisPlatoDto = null;
        if (sourceHistoryEntity.getAnalisis() != null) {
            // Pass the history entity itself to fill ID and Fecha into AnalisisPlato DTO
            analisisPlatoDto = convertAnalisisPlatoEntityToDto(sourceHistoryEntity.getAnalisis(), sourceHistoryEntity);
        }


        // ImageAnalysisHistoryResponse DTO has all fields of AnalisisPlato plus its own id and fecha.
        // The builder pattern of the DTO will take care of this if AnalisisPlato is the superclass or fields are duplicated.
        // We must ensure all fields required by ImageAnalysisHistoryResponse are populated.
        // The AnalisisPlato DTO from convertAnalisisPlatoEntityToDto already contains id and fecha.

        // Directly use fields from analisisPlatoDto and sourceHistoryEntity for clarity if DTO structure is flat (no inheritance in DTO)
        // However, ImageAnalysisHistoryResponse extends AnalisisPlato so its builder should handle it.

        return ImageAnalysisHistoryResponse.builder()
            .id(sourceHistoryEntity.getId()) // From History Entity
            .fecha(sourceHistoryEntity.getFecha()) // From History Entity
            .evaluacionGeneral(analisisPlatoDto != null ? analisisPlatoDto.getEvaluacionGeneral() : null)
            .nutricion(analisisPlatoDto != null ? analisisPlatoDto.getNutricion() : null)
            .detallesAlimentos(analisisPlatoDto != null ? analisisPlatoDto.getDetallesAlimentos() : new ArrayList<>())
            .recomendaciones(analisisPlatoDto != null ? analisisPlatoDto.getRecomendaciones() : new ArrayList<>())
            .imagenOriginalUrl(sourceHistoryEntity.getImagenOriginalUrl()) // Or from analisisPlatoDto.getImagenOriginalUrl()
            .imagenProcesadaUrl(sourceHistoryEntity.getImagenProcesadaUrl()) // Or from analisisPlatoDto.getImagenProcesadaUrl()
            // The AnalisisPlato object itself is not a field in ImageAnalysisHistoryResponse, its fields are inherited.
            // So, we don't do .analisis(analisisPlatoDto)
            .build();
    }
} 