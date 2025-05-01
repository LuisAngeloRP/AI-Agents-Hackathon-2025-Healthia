package com.healthia.functions.repositories;

import com.healthia.functions.entities.ImageAnalysisHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageAnalysisHistoryRepository extends JpaRepository<ImageAnalysisHistoryEntity, Integer> {
    Optional<ImageAnalysisHistoryEntity> findById(Integer id);
} 