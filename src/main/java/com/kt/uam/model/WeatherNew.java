package com.kt.uam.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "weather_new")
@Getter
@Setter
public class WeatherNew {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne
    @JoinColumn(name = "wind_speed")
    private WindSpeed windSpeed;

    @ManyToOne
    @JoinColumn(name = "wind_direction")
    private WindDirection windDirection;

    @ManyToOne
    @JoinColumn(name = "sky_condition")
    private SkyCondition skyCondition;

    private String baseDate;
    private String baseTime;
    private String fcstDate;
    private String fcstTime;
    private Double temperature;
    private Double eastWestWind;
    private Double northSouthWind;
    private Integer humidity;
    private Integer precipitationProb;
    private String precipitationType;
    private String precipitationAmount;
    private String snowAmount;
    private Integer nx;
    private Integer ny;
}