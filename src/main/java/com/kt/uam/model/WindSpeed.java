package com.kt.uam.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "wind_speed")
public class WindSpeed {
    @Id
    private String code;
    private String description;
}