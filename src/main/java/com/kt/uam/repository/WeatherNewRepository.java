package com.kt.uam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import com.kt.uam.model.WeatherNew;

@Repository
public interface WeatherNewRepository extends JpaRepository<WeatherNew, Long> {
    // 필요한 쿼리 메서드 추가
    @Modifying
    @Query("DELETE FROM WeatherNew w WHERE w.location.nx = :nx AND w.location.ny = :ny")
    void deleteByLocationId(@Param("nx") int nx, @Param("ny") int ny);
}