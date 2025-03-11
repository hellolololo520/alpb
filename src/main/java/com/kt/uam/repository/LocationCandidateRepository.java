package com.kt.uam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kt.uam.model.LocationCandidate;

import java.util.List;

@Repository
public interface LocationCandidateRepository extends JpaRepository<LocationCandidate, Long> {
    List<LocationCandidate> findByIsActiveTrue();
}