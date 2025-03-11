package com.kt.uam.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.uam.model.LocationCandidate;
import com.kt.uam.repository.LocationCandidateRepository;
import com.kt.uam.model.Location;
import com.kt.uam.repository.LocationRepository;
import com.kt.uam.repository.WeatherNewRepository;
import com.kt.uam.model.Weather;
import com.kt.uam.repository.WeatherRepository;
import com.kt.uam.service.WeatherNewService;

import jakarta.annotation.PostConstruct;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationCandidateService {
    private final LocationCandidateRepository locationCandidateRepository;
    private final LocationRepository locationRepository;
    private final WeatherNewRepository weatherNewRepository;
    private final WeatherRepository weatherRepository;
    private final WeatherNewService weatherNewService;

    @PostConstruct
    public void initializeLocationCandidates() {
        if (locationCandidateRepository.count() == 0) {
            List<LocationCandidate> candidates = Arrays.asList(
                    createLocationCandidate("종로구", 60, 127),
                    createLocationCandidate("중구", 60, 126),
                    createLocationCandidate("용산구", 59, 127),
                    createLocationCandidate("성동구", 61, 127),
                    createLocationCandidate("광진구", 62, 127),
                    createLocationCandidate("동대문구", 61, 127),
                    createLocationCandidate("중랑구", 62, 127),
                    createLocationCandidate("성북구", 61, 127),
                    createLocationCandidate("강북구", 60, 128),
                    createLocationCandidate("도봉구", 61, 128),
                    createLocationCandidate("노원구", 62, 129),
                    createLocationCandidate("은평구", 59, 128),
                    createLocationCandidate("서대문구", 59, 127),
                    createLocationCandidate("마포구", 59, 127),
                    createLocationCandidate("양천구", 58, 126),
                    createLocationCandidate("강서구", 58, 126),
                    createLocationCandidate("구로구", 58, 125),
                    createLocationCandidate("금천구", 59, 124),
                    createLocationCandidate("영등포구", 59, 127),
                    createLocationCandidate("동작구", 60, 125),
                    createLocationCandidate("관악구", 59, 125),
                    createLocationCandidate("서초구", 60, 125),
                    createLocationCandidate("강남구", 62, 125),
                    createLocationCandidate("송파구", 62, 125),
                    createLocationCandidate("강동구", 63, 126),
                    createLocationCandidate("KT판교빌딩", 60, 124));
            locationCandidateRepository.saveAll(candidates);
        }
    }

    private LocationCandidate createLocationCandidate(String name, int nx, int ny) {
        LocationCandidate candidate = new LocationCandidate();
        candidate.setLocationName(name);
        candidate.setNx(nx);
        candidate.setNy(ny);
        return candidate;
    }

    @Transactional
    public void activateLocation(Long id) {
        LocationCandidate candidate = locationCandidateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 위치를 찾을 수 없습니다: " + id));

        if (!locationRepository.existsByNxAndNy(candidate.getNx(), candidate.getNy())) {
            Location location = new Location();
            location.setLocationName(candidate.getLocationName());
            location.setNx(candidate.getNx());
            location.setNy(candidate.getNy());
            locationRepository.save(location);
        }

        candidate.setActive(true);
    }

    @Transactional
    public void activateLocationWithWeather(Long id) {
        LocationCandidate candidate = locationCandidateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 위치를 찾을 수 없습니다: " + id));

        // Location 테이블에 추가
        Location location;
        if (!locationRepository.existsByNxAndNy(candidate.getNx(), candidate.getNy())) {
            location = new Location();
            location.setLocationName(candidate.getLocationName());
            location.setNx(candidate.getNx());
            location.setNy(candidate.getNy());
            location = locationRepository.save(location);

            // Weather 테이블에서 해당 좌표의 데이터 찾기
            List<Weather> existingWeatherData = weatherRepository.findByNxAndNy(candidate.getNx(), candidate.getNy());

            // 해당 좌표의 데이터가 없으면, 비슷한 좌표의 데이터를 복사
            if (existingWeatherData.isEmpty()) {
                // 예: 가장 가까운 위치의 데이터 찾기
                List<Weather> nearbyWeatherData = weatherRepository.findAll().stream()
                        .filter(w -> w.getLocationId() != null)
                        .collect(Collectors.toList());

                if (!nearbyWeatherData.isEmpty()) {
                    for (Weather weather : nearbyWeatherData) {
                        Weather newWeather = new Weather();
                        newWeather.setBaseDate(weather.getBaseDate());
                        newWeather.setBaseTime(weather.getBaseTime());
                        newWeather.setFcstDate(weather.getFcstDate());
                        newWeather.setFcstTime(weather.getFcstTime());
                        newWeather.setCategory(weather.getCategory());
                        newWeather.setFcstValue(weather.getFcstValue());
                        newWeather.setNx(candidate.getNx());
                        newWeather.setNy(candidate.getNy());
                        newWeather.setLocationId(location.getId());

                        weatherRepository.save(newWeather);
                    }
                }
            } else {
                // 이미 해당 좌표의 데이터가 있으면, locationId만 업데이트
                for (Weather weather : existingWeatherData) {
                    weather.setLocationId(location.getId());
                    weatherRepository.save(weather);
                }
            }
        }

        // 날씨 데이터 변환
        weatherNewService.convertAndSaveWeatherData();

        candidate.setActive(true);
    }

    @Transactional
    public void deactivateLocation(Long id) {
        LocationCandidate candidate = locationCandidateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 위치를 찾을 수 없습니다: " + id));

        // 먼저 weather_new 테이블에서 해당 location과 관련된 데이터를 삭제
        weatherNewRepository.deleteByLocationId(candidate.getNx(), candidate.getNy());

        // 그 다음 location 테이블에서 삭제
        locationRepository.deleteByNxAndNy(candidate.getNx(), candidate.getNy());

        candidate.setActive(false);
    }

    public List<LocationCandidate> getActiveLocations() {
        return locationCandidateRepository.findByIsActiveTrue();
    }

    public List<LocationCandidate> getAllLocations() {
        return locationCandidateRepository.findAll();
    }

    @Transactional
    public LocationCandidate addLocationCandidate(LocationCandidate locationCandidate) {
        // 이미 존재하는 위치인지 확인
        List<LocationCandidate> existingCandidates = locationCandidateRepository.findAll();
        for (LocationCandidate existing : existingCandidates) {
            if (existing.getNx() == locationCandidate.getNx() &&
                    existing.getNy() == locationCandidate.getNy()) {
                throw new IllegalArgumentException("이미 동일한 좌표의 위치가 존재합니다.");
            }
            if (existing.getLocationName().equals(locationCandidate.getLocationName())) {
                throw new IllegalArgumentException("이미 동일한 이름의 위치가 존재합니다.");
            }
        }

        // 새 위치 저장
        LocationCandidate savedCandidate = locationCandidateRepository.save(locationCandidate);

        // 위치가 활성화 상태라면 Location 테이블에도 추가
        if (locationCandidate.isActive()) {
            activateLocationWithWeather(savedCandidate.getId());
        }

        return savedCandidate;
    }
}