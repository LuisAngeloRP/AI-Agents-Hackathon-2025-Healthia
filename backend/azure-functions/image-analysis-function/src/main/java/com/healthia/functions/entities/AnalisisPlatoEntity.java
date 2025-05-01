package com.healthia.functions.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "analisis_platos")
public class AnalisisPlatoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "analisis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ImageAnalysisHistoryEntity historyEntry;

    private String evaluacionGeneral;
    private Double porcentajeVerduras;
    private Double porcentajeProteinas;
    private Double porcentajeCarbohidratos;

    @OneToMany(mappedBy = "analisisPlato", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DetalleAlimentoEntity> detalleAlimentos;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "analisis_recomendaciones", joinColumns = @JoinColumn(name = "analisis_plato_id"))
    @Column(name = "recomendacion", length = 512) // Increased length for recommendations
    private List<String> recomendaciones;

    private String imagenOriginalUrl;
    private String imagenProcesadaUrl;

    // Helper method to associate details with this analysis
    public void setDetalleAlimentos(List<DetalleAlimentoEntity> detalleAlimentos) {
        this.detalleAlimentos = detalleAlimentos;
        if (detalleAlimentos != null) {
            for (DetalleAlimentoEntity detalle : detalleAlimentos) {
                detalle.setAnalisisPlato(this);
            }
        }
    }
} 