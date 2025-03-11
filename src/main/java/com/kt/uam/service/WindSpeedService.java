package com.kt.uam.service;

import com.kt.uam.model.WindSpeed;
import com.kt.uam.repository.WindSpeedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class WindSpeedService {

    @Autowired
    private WindSpeedRepository windSpeedRepository;

    @PostConstruct
    public void initializeWindSpeeds() {
        if (windSpeedRepository.count() == 0) {
            List<WindSpeed> windSpeeds = new ArrayList<>();

            windSpeeds.add(createWindSpeed("WEAK", "약함"));
            windSpeeds.add(createWindSpeed("MODERATE", "약간강함"));
            windSpeeds.add(createWindSpeed("STRONG", "강함"));

            windSpeedRepository.saveAll(windSpeeds);
        }
    }

    private WindSpeed createWindSpeed(String code, String description) {
        WindSpeed windSpeed = new WindSpeed();
        windSpeed.setCode(code);
        windSpeed.setDescription(description);
        return windSpeed;
    }

    public WindSpeed getWindSpeed(String speedValue) {
        try {
            Double speed = Double.parseDouble(speedValue);
            String code;

            if (speed < 4.0) {
                code = "WEAK";
            } else if (speed < 9.0) {
                code = "MODERATE";
            } else {
                code = "STRONG";
            }

            return windSpeedRepository.findById(code)
                    .orElseThrow(() -> new RuntimeException("WindSpeed not found for code: " + code));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}