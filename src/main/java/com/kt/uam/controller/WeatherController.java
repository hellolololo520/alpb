package com.kt.uam.controller;

import com.kt.uam.model.Weather;
import com.kt.uam.service.WeatherService;
import com.kt.uam.service.WeatherNewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private WeatherNewService weatherNewService;

    @GetMapping("/fetch")
    public ResponseEntity<?> fetchWeather() {
        try {
            List<Weather> result = weatherService.fetchAndSaveWeatherData();
            return ResponseEntity.ok().body(Map.of("message", "날씨 데이터 저장 완료", "count", result.size()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/table")
    public ResponseEntity<?> getWeatherTable() {
        try {
            System.out.println("날씨 데이터 변환 시작");
            List<Weather> weatherData = weatherService.fetchAndSaveWeatherData(); // 기존 Weather 테이블에 저장
            System.out.println("기존 Weather 데이터 저장 완료: " + weatherData.size() + "개");

            weatherNewService.convertAndSaveWeatherData(); // WeatherNew 테이블에도 저장
            System.out.println("WeatherNew 데이터 변환 및 저장 완료");

            return ResponseEntity.ok()
                    .body(Map.of(
                            "message", "날씨 데이터 변환 및 저장 완료",
                            "weatherCount", weatherData.size()));
        } catch (Exception e) {
            System.err.println("에러 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}