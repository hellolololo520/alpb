package com.kt.uam.service;

import com.kt.uam.model.Location;
import com.kt.uam.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    @PostConstruct
    public void initializeLocations() {
        // 이미 데이터가 있는지 확인
        if (locationRepository.count() == 0) {
            List<Location> locations = Arrays.asList(
                    createLocation(56, 129, "고양킨텍스"),
                    createLocation(57, 127, "김포공항"),
                    createLocation(59, 126, "여의도"),
                    createLocation(62, 126, "잠실한강공원"),
                    createLocation(62, 124, "수서역"),
                    createLocation(55, 125, "드론시험인증센터"),
                    createLocation(56, 127, "계양신도시"));

            locationRepository.saveAll(locations);
        }
    }

    private Location createLocation(int nx, int ny, String name) {
        Location location = new Location();
        location.setNx(nx);
        location.setNy(ny);
        location.setLocationName(name);
        return location;
    }

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public Location getLocationByName(String name) {
        return locationRepository.findByLocationName(name);
    }
}