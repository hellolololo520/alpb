package com.kt.uam.repository;

import com.kt.uam.model.WindSpeed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WindSpeedRepository extends JpaRepository<WindSpeed, String> {
    @Query("SELECT w FROM WindSpeed w WHERE " +
            "(:speed < 4.0 AND w.code = 'WEAK') OR " +
            "(:speed >= 4.0 AND :speed < 9.0 AND w.code = 'MODERATE') OR " +
            "(:speed >= 9.0 AND w.code = 'STRONG')")
    WindSpeed findBySpeed(@Param("speed") Double speed);
}