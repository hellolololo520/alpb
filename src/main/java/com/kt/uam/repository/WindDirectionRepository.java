package com.kt.uam.repository;

import com.kt.uam.model.WindDirection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WindDirectionRepository extends JpaRepository<WindDirection, String> {
    @Query("SELECT w FROM WindDirection w WHERE w.code = :code")
    WindDirection findByCode(@Param("code") String code);
}