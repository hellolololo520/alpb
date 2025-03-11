package com.kt.uam.controller;

import com.kt.uam.model.LocationCandidate;
import com.kt.uam.service.LocationCandidateService;
import com.kt.uam.service.WeatherNewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location-candidates")
@RequiredArgsConstructor
public class LocationCandidateController {
    private final LocationCandidateService locationCandidateService;
    private final WeatherNewService weatherNewService;

    @GetMapping
    public ResponseEntity<List<LocationCandidate>> getAllCandidates() {
        return ResponseEntity.ok(locationCandidateService.getAllLocations());
    }

    @PostMapping
    public ResponseEntity<LocationCandidate> addLocationCandidate(@RequestBody LocationCandidate locationCandidate) {
        LocationCandidate savedCandidate = locationCandidateService.addLocationCandidate(locationCandidate);
        return ResponseEntity.ok(savedCandidate);
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateLocation(@PathVariable("id") Long id) {
        locationCandidateService.activateLocation(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateLocation(@PathVariable("id") Long id) {
        locationCandidateService.deactivateLocation(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/activate-with-weather")
    public ResponseEntity<Void> activateLocationWithWeather(@PathVariable("id") Long id) {
        locationCandidateService.activateLocationWithWeather(id);
        return ResponseEntity.ok().build();
    }
}