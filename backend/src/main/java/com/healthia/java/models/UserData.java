package com.healthia.java.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserData {
    // Basic info corresponding to Python dict keys
    private String id; // Added for easier identification
    private String nombre;
    private Integer edad;
    private Double peso;
    private Double altura;
    private String genero;
    @Builder.Default
    private List<String> condicionesMedicas = new ArrayList<>();
    @Builder.Default
    private List<String> alergias = new ArrayList<>();
    @Builder.Default
    private List<String> restriccionesAlimentarias = new ArrayList<>();
    @Builder.Default
    private List<String> objetivos = new ArrayList<>();
    private String nivelActividad;
    @Builder.Default
    private List<String> preferenciasAlimentarias = new ArrayList<>();

    // Medical specific fields from MedicalAgent persistence
    private String fechaNacimiento;
    private String grupoSanguineo;
    private String email;
    private String telefono;
    @Builder.Default
    private List<Map<String, Object>> medicamentos = new ArrayList<>(); // Generic map for now
    @Builder.Default
    private List<Map<String, Object>> cirugias = new ArrayList<>(); // Generic map
    @Builder.Default
    private List<String> antecedentesFamiliares = new ArrayList<>();
    @Builder.Default
    private List<Map<String, Object>> vacunas = new ArrayList<>(); // Generic map
    @Builder.Default
    private List<Map<String, Object>> historialConsultas = new ArrayList<>(); // Generic map
    @Builder.Default
    private List<Map<String, Object>> estudiosMedicos = new ArrayList<>(); // Generic map
    private Map<String, Object> habitos; // Generic map
    private String ultimaActualizacion;
} 