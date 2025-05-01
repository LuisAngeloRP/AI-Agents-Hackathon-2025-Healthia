package com.healthia.functions.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable // Mark as Embeddable as it will be part of DetalleAlimentoEntity
public class CoordenadasEntity {
    private int x1;
    private int y1;
    private int x2;
    private int y2;
} 