package com.kt.uam.repository;

import com.kt.uam.model.SkyCondition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkyConditionRepository extends JpaRepository<SkyCondition, String> {
    SkyCondition findByCode(String code);
}