package com.kt.uam.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kt.uam.service.WeatherNewService;
import com.kt.uam.repository.WeatherNewRepository;
import com.kt.uam.model.WeatherNew;
import java.util.List;

@RestController
@RequestMapping("/api/weather-new")
@RequiredArgsConstructor
public class WeatherNewController {
    private final WeatherNewService weatherNewService;
    private final WeatherNewRepository weatherNewRepository;

    // 모든 날씨 데이터 조회 API
    @GetMapping
    public ResponseEntity<List<WeatherNew>> getAllWeatherData() {
        return ResponseEntity.ok(weatherNewRepository.findAll());
    }

    // 날씨 데이터 수집 API
    @PostMapping("/collect")
    public ResponseEntity<Void> collectWeatherData() {
        weatherNewService.convertAndSaveWeatherData();
        return ResponseEntity.ok().build();
    }
}