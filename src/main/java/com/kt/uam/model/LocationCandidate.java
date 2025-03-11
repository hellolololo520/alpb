package com.kt.uam.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "location_candidate")
@Data
public class LocationCandidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String locationName;
    private int nx;
    private int ny;
    private boolean isActive = false; // 현재 날씨 정보를 수집하고 있는지 여부
}