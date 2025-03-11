package com.kt.uam.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "sky_condition")
public class SkyCondition {
    @Id
    private String code; // "1", "3", "4"
    private String description; // "맑음", "구름많음", "흐림"
}