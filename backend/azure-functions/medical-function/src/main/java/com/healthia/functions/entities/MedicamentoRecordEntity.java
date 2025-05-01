package com.healthia.functions.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "medical_profile_medicamentos")
public class MedicamentoRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserMedicalProfileEntity userProfile;

    private String nombre;
    private String dosis;
    private String frecuencia;
    private String fechaInicio; // Consider LocalDate
    private String fechaFin; // Consider LocalDate, optional
    @Lob
    @Column(columnDefinition = "TEXT")
    private String notas;
} 