package com.kt.uam.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "wind_direction")
public class WindDirection {
    @Id
    private String code; // "N", "NE", "E", "SE", "S", "SW", "W", "NW"
    private String description; // "북풍", "북동풍", "동풍", "남동풍", "남풍", "남서풍", "서풍", "북서풍"
}