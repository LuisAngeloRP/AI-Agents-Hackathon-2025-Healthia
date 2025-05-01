package com.healthia.functions.repositories;

import com.healthia.functions.entities.AnalisisPlatoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalisisPlatoRepository extends JpaRepository<AnalisisPlatoEntity, Long> {
} 