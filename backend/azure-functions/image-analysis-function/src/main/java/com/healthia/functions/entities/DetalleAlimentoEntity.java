package com.healthia.functions.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "detalle_alimentos")
public class DetalleAlimentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analisis_plato_id", nullable = false)
    private AnalisisPlatoEntity analisisPlato;

    private String nombre;
    private String categoria;
    private Double porcentajeArea;

    @Embedded
    private CoordenadasEntity coordenadas;
} 