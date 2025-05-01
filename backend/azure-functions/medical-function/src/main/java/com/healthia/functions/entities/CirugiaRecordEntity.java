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
@Table(name = "medical_profile_cirugias")
public class CirugiaRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserMedicalProfileEntity userProfile;

    private String nombreProcedimiento;
    private String fecha; // Consider LocalDate
    private String hospital;
    private String cirujano;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String notas;
} 