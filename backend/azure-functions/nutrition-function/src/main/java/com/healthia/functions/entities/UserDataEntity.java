package com.healthia.functions.entities;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "user_data")
public class UserDataEntity {

    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId; // Assuming 'id' from UserData model is the primary key

    @Column(name = "name")
    private String nombre;

    @Column(name = "age")
    private Integer edad;

    @Column(name = "weight_kg")
    private Double peso;

    @Column(name = "height_cm")
    private Double altura;

    @Column(name = "gender")
    private String genero;

    @ElementCollection(fetch = FetchType.EAGER) // Or LAZY with proper handling
    @CollectionTable(name = "user_medical_conditions", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "condition_name")
    private List<String> condicionesMedicas;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_objectives", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "objective_name")
    private List<String> objetivos;

    @Column(name = "activity_level")
    private String nivelActividad;

    // Add other fields from com.healthia.java.models.UserData as needed
    // Timestamps for creation/update can also be added here.

    public UserDataEntity() {
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public Double getAltura() {
        return altura;
    }

    public void setAltura(Double altura) {
        this.altura = altura;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public List<String> getCondicionesMedicas() {
        return condicionesMedicas;
    }

    public void setCondicionesMedicas(List<String> condicionesMedicas) {
        this.condicionesMedicas = condicionesMedicas;
    }

    public List<String> getObjetivos() {
        return objetivos;
    }

    public void setObjetivos(List<String> objetivos) {
        this.objetivos = objetivos;
    }

    public String getNivelActividad() {
        return nivelActividad;
    }

    public void setNivelActividad(String nivelActividad) {
        this.nivelActividad = nivelActividad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDataEntity that = (UserDataEntity) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
} 