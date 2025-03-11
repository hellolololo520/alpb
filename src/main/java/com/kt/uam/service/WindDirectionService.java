package com.kt.uam.service;

import com.kt.uam.model.WindDirection;
import com.kt.uam.repository.WindDirectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class WindDirectionService {

    @Autowired
    private WindDirectionRepository windDirectionRepository;

    @PostConstruct
    public void initializeWindDirections() {
        if (windDirectionRepository.count() == 0) {
            List<WindDirection> directions = Arrays.asList(
                    createWindDirection("N", "북풍"),
                    createWindDirection("NE", "북동풍"),
                    createWindDirection("E", "동풍"),
                    createWindDirection("SE", "남동풍"),
                    createWindDirection("S", "남풍"),
                    createWindDirection("SW", "남서풍"),
                    createWindDirection("W", "서풍"),
                    createWindDirection("NW", "북서풍"));
            windDirectionRepository.saveAll(directions);
        }
    }

    private WindDirection createWindDirection(String code, String description) {
        WindDirection windDirection = new WindDirection();
        windDirection.setCode(code);
        windDirection.setDescription(description);
        return windDirection;
    }

    public WindDirection getWindDirectionFromDegree(Double degree) {
        String code;
        if ((degree >= 337.5 && degree <= 360) || (degree >= 0 && degree < 22.5)) {
            code = "N";
        } else if (degree >= 22.5 && degree < 67.5) {
            code = "NE";
        } else if (degree >= 67.5 && degree < 112.5) {
            code = "E";
        } else if (degree >= 112.5 && degree < 157.5) {
            code = "SE";
        } else if (degree >= 157.5 && degree < 202.5) {
            code = "S";
        } else if (degree >= 202.5 && degree < 247.5) {
            code = "SW";
        } else if (degree >= 247.5 && degree < 292.5) {
            code = "W";
        } else {
            code = "NW";
        }
        return windDirectionRepository.findByCode(code);
    }
}