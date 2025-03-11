package com.kt.uam.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.kt.uam.model.Weather;
import com.kt.uam.model.WeatherNew;
import com.kt.uam.repository.WeatherNewRepository;
import com.kt.uam.repository.WeatherRepository;
import com.kt.uam.repository.LocationRepository;
import com.kt.uam.model.Location;
import com.kt.uam.repository.WindDirectionRepository;
import com.kt.uam.repository.WindSpeedRepository;
import com.kt.uam.repository.SkyConditionRepository;
import com.kt.uam.model.WindDirection;
import com.kt.uam.model.WindSpeed;
import com.kt.uam.model.SkyCondition;

@Service
@Transactional(readOnly = true)
public class WeatherNewService {

    @Autowired
    private WeatherRepository weatherRepository;

    @Autowired
    private WeatherNewRepository weatherNewRepository;

    @Autowired
    private WindDirectionRepository windDirectionRepository;

    @Autowired
    private WindSpeedRepository windSpeedRepository;

    @Autowired
    private SkyConditionRepository skyConditionRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Transactional
    public void convertAndSaveWeatherData() {
        // 먼저 WeatherNew 테이블을 비움
        weatherNewRepository.deleteAll();

        // 기존 데이터를 새로운 트랜잭션에서 조회
        List<Weather> allWeather = new ArrayList<>(weatherRepository.findAll());
        System.out.println("기존 Weather 데이터 개수: " + allWeather.size());

        // 기존 날씨 데이터를 baseDate, baseTime, fcstDate, fcstTime으로 그룹화
        Map<String, List<Weather>> groupedWeather = allWeather.stream()
                .filter(w -> w.getLocationId() != null)
                .collect(Collectors.groupingBy(
                        w -> String.format("%s_%s_%d",
                                w.getFcstDate(),
                                w.getFcstTime(),
                                w.getLocationId())));

        System.out.println("그룹화된 데이터 개수: " + groupedWeather.size());

        List<WeatherNew> newWeatherList = new ArrayList<>();

        for (List<Weather> weatherGroup : groupedWeather.values()) {
            if (!weatherGroup.isEmpty()) {
                try {
                    WeatherNew weatherNew = new WeatherNew();
                    Weather firstWeather = weatherGroup.get(0);

                    // Location 엔티티 조회
                    Location location = locationRepository.findById(firstWeather.getLocationId())
                            .orElseThrow(() -> new RuntimeException(
                                    "Location not found for id: " + firstWeather.getLocationId()));

                    System.out.println("처리 중인 데이터 - 날짜: " + firstWeather.getFcstDate()
                            + ", 시간: " + firstWeather.getFcstTime());

                    // 공통 정보 설정
                    weatherNew.setBaseDate(firstWeather.getBaseDate());
                    weatherNew.setBaseTime(firstWeather.getBaseTime());
                    weatherNew.setFcstDate(firstWeather.getFcstDate());
                    weatherNew.setFcstTime(firstWeather.getFcstTime());
                    weatherNew.setLocation(location);
                    weatherNew.setNx(firstWeather.getNx());
                    weatherNew.setNy(firstWeather.getNy());

                    // 각 카테고리별 데이터 설정
                    for (Weather w : weatherGroup) {
                        try {
                            System.out.println("카테고리: " + w.getCategory() + ", 값: " + w.getFcstValue());
                            switch (w.getCategory()) {
                                case "TMP":
                                    weatherNew.setTemperature(parseDoubleOrNull(w.getFcstValue()));
                                    break;
                                case "UUU":
                                    weatherNew.setEastWestWind(parseDoubleOrNull(w.getFcstValue()));
                                    break;
                                case "VVV":
                                    weatherNew.setNorthSouthWind(parseDoubleOrNull(w.getFcstValue()));
                                    break;
                                case "VEC":
                                    try {
                                        Double degree = Double.parseDouble(w.getFcstValue());
                                        String directionCode;
                                        if ((degree >= 337.5 && degree <= 360) || (degree >= 0 && degree < 22.5)) {
                                            directionCode = "N";
                                        } else if (degree >= 22.5 && degree < 67.5) {
                                            directionCode = "NE";
                                        } else if (degree >= 67.5 && degree < 112.5) {
                                            directionCode = "E";
                                        } else if (degree >= 112.5 && degree < 157.5) {
                                            directionCode = "SE";
                                        } else if (degree >= 157.5 && degree < 202.5) {
                                            directionCode = "S";
                                        } else if (degree >= 202.5 && degree < 247.5) {
                                            directionCode = "SW";
                                        } else if (degree >= 247.5 && degree < 292.5) {
                                            directionCode = "W";
                                        } else {
                                            directionCode = "NW";
                                        }
                                        WindDirection windDirection = windDirectionRepository.findById(directionCode)
                                                .orElseThrow(() -> new RuntimeException(
                                                        "WindDirection not found for code: " + directionCode));
                                        weatherNew.setWindDirection(windDirection);
                                    } catch (NumberFormatException e) {
                                        System.err.println("Invalid wind direction value: " + w.getFcstValue());
                                    }
                                    break;
                                case "WSD":
                                    try {
                                        Double speed = Double.parseDouble(w.getFcstValue());
                                        String speedCode;
                                        if (speed < 4.0) {
                                            speedCode = "WEAK";
                                        } else if (speed < 9.0) {
                                            speedCode = "MODERATE";
                                        } else {
                                            speedCode = "STRONG";
                                        }
                                        WindSpeed windSpeed = windSpeedRepository.findById(speedCode)
                                                .orElseThrow(() -> new RuntimeException(
                                                        "WindSpeed not found for code: " + speedCode));
                                        weatherNew.setWindSpeed(windSpeed);
                                    } catch (NumberFormatException e) {
                                        System.err.println("Invalid wind speed value: " + w.getFcstValue());
                                    }
                                    break;
                                case "SKY":
                                    SkyCondition skyCondition = skyConditionRepository.findById(w.getFcstValue())
                                            .orElseThrow(() -> new RuntimeException(
                                                    "SkyCondition not found for code: " + w.getFcstValue()));
                                    weatherNew.setSkyCondition(skyCondition);
                                    break;
                                case "PTY":
                                    weatherNew.setPrecipitationType(w.getFcstValue());
                                    break;
                                case "REH":
                                    weatherNew.setHumidity(parseIntegerOrNull(w.getFcstValue()));
                                    break;
                                case "POP":
                                    weatherNew.setPrecipitationProb(parseIntegerOrNull(w.getFcstValue()));
                                    break;
                                case "PCP":
                                    weatherNew.setPrecipitationAmount(w.getFcstValue());
                                    break;
                                case "SNO":
                                    weatherNew.setSnowAmount(w.getFcstValue());
                                    break;
                            }
                        } catch (Exception e) {
                            System.err.println("값 변환 중 오류 발생: " + w.getCategory() + " = " + w.getFcstValue());
                            e.printStackTrace();
                        }
                    }
                    newWeatherList.add(weatherNew);
                } catch (Exception e) {
                    System.err.println("데이터 처리 중 오류 발생");
                    e.printStackTrace();
                }
            }
        }

        System.out.println("변환된 데이터 개수: " + newWeatherList.size());

        try {
            // 배치 크기를 설정하여 저장
            int batchSize = 100;
            for (int i = 0; i < newWeatherList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, newWeatherList.size());
                List<WeatherNew> batch = newWeatherList.subList(i, end);
                weatherNewRepository.saveAll(batch);
            }
            System.out.println("데이터 저장 완료");
        } catch (Exception e) {
            System.err.println("데이터 저장 중 오류 발생");
            e.printStackTrace();
        }
    }

    @Scheduled(fixedRate = 300000) // 5분마다 실행
    @Transactional
    public void scheduleWeatherDataConversion() {
        convertAndSaveWeatherData();
    }

    private Double parseDoubleOrNull(String value) {
        try {
            return value != null ? Double.parseDouble(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseIntegerOrNull(String value) {
        try {
            return value != null ? Integer.parseInt(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}