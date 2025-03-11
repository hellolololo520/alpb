package com.kt.uam.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "weather")
public class Weather {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String baseDate;
    private String baseTime;
    private String category;
    private String fcstDate;
    private String fcstTime;
    private String fcstValue;
    private int nx;
    private int ny;

    private Long locationId;
    private String skyConditionCode;
    private String windDirectionCode;
    private String windSpeedCode;
}
