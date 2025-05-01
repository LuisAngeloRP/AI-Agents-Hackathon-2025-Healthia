package com.healthia.functions.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_medical_profiles")
public class UserMedicalProfileEntity {

    @Id
    @Column(name = "user_id")
    private String userId; // Corresponds to UserData.id

    private String nombre;
    private Integer edad;
    private Double peso; // in kg
    private Double altura; // in cm
    private String genero;
    private String fechaNacimiento; // Consider using LocalDate if format is consistent
    private String grupoSanguineo;
    private String email;
    private String telefono;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "medical_profile_condiciones", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "condicion")
    @Builder.Default
    private List<String> condicionesMedicas = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "medical_profile_alergias", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "alergia")
    @Builder.Default
    private List<String> alergias = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "medical_profile_restricciones_alimentarias", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "restriccion")
    @Builder.Default
    private List<String> restriccionesAlimentarias = new ArrayList<>();
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "medical_profile_objetivos_salud", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "objetivo")
    @Builder.Default
    private List<String> objetivosSalud = new ArrayList<>(); // Renamed from 'objetivos' to be more specific

    private String nivelActividad;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "medical_profile_preferencias_alimentarias", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "preferencia")
    @Builder.Default
    private List<String> preferenciasAlimentarias = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "medical_profile_antecedentes_familiares", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "antecedente")
    @Builder.Default
    private List<String> antecedentesFamiliares = new ArrayList<>();

    @Lob
    @Column(name = "habitos_json", columnDefinition = "TEXT")
    private String habitosJson; // For storing Map<String, Object> habitos as JSON

    private OffsetDateTime ultimaActualizacion; // Changed from String to OffsetDateTime

    // Relationships to detailed record entities (to be created)
    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MedicamentoRecordEntity> medicamentos = new ArrayList<>();

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CirugiaRecordEntity> cirugias = new ArrayList<>();

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VacunaRecordEntity> vacunas = new ArrayList<>();

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ConsultaMedicaRecordEntity> historialConsultas = new ArrayList<>();

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EstudioMedicoRecordEntity> estudiosMedicos = new ArrayList<>();

    // Helper methods for bidirectional relationships
    public void addMedicamento(MedicamentoRecordEntity medicamento) {
        medicamentos.add(medicamento);
        medicamento.setUserProfile(this);
    }

    public void removeMedicamento(MedicamentoRecordEntity medicamento) {
        medicamentos.remove(medicamento);
        medicamento.setUserProfile(null);
    }

    public void addCirugia(CirugiaRecordEntity cirugia) {
        cirugias.add(cirugia);
        cirugia.setUserProfile(this);
    }

    public void removeCirugia(CirugiaRecordEntity cirugia) {
        cirugias.remove(cirugia);
        cirugia.setUserProfile(null);
    }
    
    public void addVacuna(VacunaRecordEntity vacuna) {
        vacunas.add(vacuna);
        vacuna.setUserProfile(this);
    }

    public void removeVacuna(VacunaRecordEntity vacuna) {
        vacunas.remove(vacuna);
        vacuna.setUserProfile(null);
    }

    public void addConsultaMedica(ConsultaMedicaRecordEntity consulta) {
        historialConsultas.add(consulta);
        consulta.setUserProfile(this);
    }

    public void removeConsultaMedica(ConsultaMedicaRecordEntity consulta) {
        historialConsultas.remove(consulta);
        consulta.setUserProfile(null);
    }
    
    public void addEstudioMedico(EstudioMedicoRecordEntity estudio) {
        estudiosMedicos.add(estudio);
        estudio.setUserProfile(this);
    }

    public void removeEstudioMedico(EstudioMedicoRecordEntity estudio) {
        estudiosMedicos.remove(estudio);
        estudio.setUserProfile(null);
    }
} 