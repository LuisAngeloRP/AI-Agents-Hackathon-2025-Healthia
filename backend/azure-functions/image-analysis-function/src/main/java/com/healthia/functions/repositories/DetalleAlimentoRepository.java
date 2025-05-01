package com.healthia.functions.repositories;

import com.healthia.functions.entities.DetalleAlimentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleAlimentoRepository extends JpaRepository<DetalleAlimentoEntity, Long> {
} 