package com.kt.uam.repository;

import com.kt.uam.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    Location findByLocationName(String locationName);

    boolean existsByNxAndNy(int nx, int ny);

    void deleteByNxAndNy(int nx, int ny);
}