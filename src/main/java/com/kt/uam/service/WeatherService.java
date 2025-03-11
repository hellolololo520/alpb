package com.kt.uam.service;

import com.kt.uam.model.Weather;
import com.kt.uam.repository.WeatherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import com.kt.uam.model.Location;
import com.kt.uam.repository.LocationRepository;
import com.kt.uam.service.LocationService;
import com.kt.uam.service.SkyConditionService;
import com.kt.uam.service.WindSpeedService;
import com.kt.uam.service.WindDirectionService;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.net.URLDecoder;

@Service
public class WeatherService {

    @Autowired
    private WeatherRepository weatherRepository;

    @Autowired
    private LocationService locationService; // LocationService 주입

    @Autowired
    private SkyConditionService skyConditionService; // 추가

    @Autowired
    private WindSpeedService windSpeedService; // 추가

    @Autowired
    private WindDirectionService windDirectionService; // 추가

    private static final String SERVICE_KEY = "s+fQ9LDUrt9xJ9LSIp0R4+gJBR7eOiUpRHNKXMb6gaV844FL4oI+OYVOY+MC2Bff+Iq9bQWFeWrktswAfBtkyg==";

    @Transactional
    public List<Weather> fetchAndSaveWeatherData() {
        try {
            weatherRepository.deleteAll();
            System.out.println("기존 날씨 데이터 삭제 완료");

            List<Weather> allWeatherList = new ArrayList<>();
            List<Location> locations = locationService.getAllLocations();

            for (Location location : locations) {
                String weatherData = fetchWeatherData(location.getNx(), location.getNy());
                List<Weather> weatherList = parseWeatherData(weatherData, location);
                allWeatherList.addAll(weatherList);
                System.out.println(location.getLocationName() + " 날씨 데이터 파싱 완료: " + weatherList.size() + "개");
            }

            List<Weather> savedList = weatherRepository.saveAll(allWeatherList);
            System.out.println("전체 날씨 데이터 저장 완료: " + savedList.size() + "개");

            return savedList;
        } catch (Exception e) {
            System.out.println("Error in fetchAndSaveWeatherData: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String[] getBaseDateAndTime() {
        LocalDateTime now = LocalDateTime.now();

        // 발표 시각들 (3시간 간격)
        int[] baseHours = { 2, 5, 8, 11, 14, 17, 20, 23 };

        // 현재 시각에서 1시간을 뺌 (데이터 업데이트 시간 고려)
        LocalDateTime oneHourBefore = now.minusHours(1);
        int currentHour = oneHourBefore.getHour();

        // 현재 시각보다 이전의 가장 가까운 발표 시각 찾기
        int baseHour = 2; // 기본값
        for (int hour : baseHours) {
            if (currentHour >= hour) {
                baseHour = hour;
            } else {
                break;
            }
        }

        // 발표 시각이 현재 날짜의 02시보다 이전인 경우, 전날 23시 데이터를 사용
        LocalDateTime baseDateTime;
        if (currentHour < 2) {
            baseDateTime = oneHourBefore.minusDays(1);
            baseHour = 23;
        } else {
            baseDateTime = oneHourBefore;
        }

        String baseDate = baseDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = String.format("%02d00", baseHour);

        System.out.println("현재 시각: " + now);
        System.out.println("조회 기준 시각: " + baseDate + " " + baseTime);

        return new String[] { baseDate, baseTime };
    }

    private String fetchWeatherData(int nx, int ny) throws Exception {
        String[] baseDateAndTime = getBaseDateAndTime();
        String base_date = baseDateAndTime[0];
        String base_time = baseDateAndTime[1];

        StringBuilder urlBuilder = new StringBuilder(
                "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst");

        String serviceKey = "s%2BfQ9LDUrt9xJ9LSIp0R4%2BgJBR7eOiUpRHNKXMb6gaV844FL4oI%2BOYVOY%2BMC2Bff%2BIq9bQWFeWrktswAfBtkyg%3D%3D";
        urlBuilder.append("?serviceKey=" + serviceKey);
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("372", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode("JSON", "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(base_date, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(base_time, "UTF-8"));
        urlBuilder
                .append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(nx), "UTF-8"));
        urlBuilder
                .append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(ny), "UTF-8"));

        URL url = new URL(urlBuilder.toString());
        System.out.println("Request URL: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        System.out.println("Response code: " + conn.getResponseCode());

        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
            String errorResponse = rd.lines().collect(Collectors.joining());
            System.out.println("Error Response: " + errorResponse);
            throw new RuntimeException("API 호출 실패: " + conn.getResponseCode());
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        String response = sb.toString();
        System.out.println("API Response: " + response);
        return response;
    }

    private List<Weather> parseWeatherData(String jsonData, Location location) {
        List<Weather> weatherList = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonData);
            JsonNode items = rootNode.path("response")
                    .path("body")
                    .path("items")
                    .path("item");

            if (items.isArray()) {
                for (JsonNode item : items) {
                    String category = item.path("category").asText();
                    if ("WAV".equals(category)) {
                        continue;
                    }

                    Weather weather = new Weather();
                    weather.setBaseDate(item.path("baseDate").asText());
                    weather.setBaseTime(item.path("baseTime").asText());
                    weather.setCategory(category);
                    weather.setFcstDate(item.path("fcstDate").asText());
                    weather.setFcstTime(item.path("fcstTime").asText());
                    weather.setLocationId(location.getId()); // location 대신 locationId 설정

                    String value = item.path("fcstValue").asText();
                    if ("PCP".equals(category) || "SNO".equals(category)) {
                        try {
                            value = new String(value.getBytes("UTF-8"), "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    weather.setFcstValue(value);
                    weather.setNx(item.path("nx").asInt());
                    weather.setNy(item.path("ny").asInt());

                    // 각 카테고리별 코드 설정
                    if ("VEC".equals(category)) {
                        try {
                            Double degree = Double.parseDouble(value);
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
                            weather.setWindDirectionCode(directionCode);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid wind direction value: " + value);
                        }
                    } else if ("WSD".equals(category)) {
                        weather.setWindSpeedCode(value);
                    } else if ("SKY".equals(category)) {
                        weather.setSkyConditionCode(value);
                    }

                    weatherList.add(weather);
                }
            }
        } catch (Exception e) {
            System.out.println("Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
        }
        return weatherList;
    }

    private String getTextContent(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }

    public List<Map<String, Object>> getWeatherTable() {
        List<Weather> allWeather = weatherRepository.findAll();
        Map<String, Map<String, Map<String, String>>> groupedData = new TreeMap<>();

        // 날짜와 시간별로 데이터 그룹화
        for (Weather weather : allWeather) {
            String fcstDate = weather.getFcstDate();
            String fcstTime = weather.getFcstTime();
            String category = weather.getCategory();
            String value = weather.getFcstValue();

            groupedData.computeIfAbsent(fcstDate, k -> new TreeMap<>())
                    .computeIfAbsent(fcstTime, k -> new HashMap<>())
                    .put(category, value);
        }

        List<Map<String, Object>> tableData = new ArrayList<>();

        // 그룹화된 데이터를 원하는 형식으로 변환
        for (Map.Entry<String, Map<String, Map<String, String>>> dateEntry : groupedData.entrySet()) {
            String fcstDate = dateEntry.getKey();
            for (Map.Entry<String, Map<String, String>> timeEntry : dateEntry.getValue().entrySet()) {
                String fcstTime = timeEntry.getKey();
                Map<String, String> categories = timeEntry.getValue();

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("Forecast_date", fcstDate);
                row.put("Forecast_hour", fcstTime);
                row.put("Temperature", categories.getOrDefault("TMP", "NaN"));
                row.put("EastWestWind", categories.getOrDefault("UUU", "NaN"));
                row.put("NorthSouthWind", categories.getOrDefault("VVV", "NaN"));
                row.put("WindDirection", categories.getOrDefault("VEC", "NaN"));
                row.put("WindSpeed", categories.getOrDefault("WSD", "NaN"));
                row.put("SkyCondition", categories.getOrDefault("SKY", "NaN"));
                row.put("PrecipitationType", categories.getOrDefault("PTY", "NaN"));
                row.put("Humidity", categories.getOrDefault("REH", "NaN"));
                row.put("PrecipitationProb", categories.getOrDefault("POP", "NaN"));
                row.put("PrecipitationAmount", categories.getOrDefault("PCP", "NaN"));
                row.put("SnowAmount", categories.getOrDefault("SNO", "NaN"));

                tableData.add(row);
            }
        }

        return tableData;
    }

    @Transactional
    public void fetchWeatherDataForLocation(Location location) {
        // 외부 API에서 해당 위치의 날씨 데이터를 가져와서 Weather 테이블에 저장하는 로직
        // ...
    }
}