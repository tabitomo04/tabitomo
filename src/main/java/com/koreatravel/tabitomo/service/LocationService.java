package com.koreatravel.tabitomo.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {
    /**
     * 전국 숙박시설 가격,부대시설 정보.json을 기반으로 조건에 맞는 숙소 추천
     */
    public List<Map<String, Object>> recommendAccommodationsByJson(String destination, Integer budget, List<String> facilities) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            ClassPathResource resource = new ClassPathResource("static/data/전국 숙박시설 가격,부대시설 정보.json");
            List<?> all = objectMapper.readValue(resource.getInputStream(), List.class);
            for (Object obj : all) {
                Map<String, Object> hotel = (Map<String, Object>) obj;
                // 지역 필터
                String region = hotel.get("지역") != null ? hotel.get("지역").toString() : "";
                if (destination != null && !region.contains(destination)) continue;

                // 예산 필터
                Integer price = null;
                try {
                    price = Integer.parseInt(String.valueOf(hotel.getOrDefault("가격", "0")));
                } catch (Exception e) { price = null; }
                if (budget != null && price != null && price > budget) continue;

                // 부대시설 필터
                List<String> hotelFacilities;
                Object fObj = hotel.get("부대시설");
                if (fObj instanceof List<?>) {
                    hotelFacilities = ((List<?>) fObj).stream().map(f -> f.toString().trim()).toList();
                } else if (fObj instanceof String) {
                    hotelFacilities = Arrays.asList(((String) fObj).split(","));
                } else {
                    hotelFacilities = new ArrayList<>();
                }
                if (facilities != null && !facilities.isEmpty()) {
                    boolean allMatch = facilities.stream().allMatch(f -> hotelFacilities.stream().anyMatch(h -> h.trim().contains(f)));
                    if (!allMatch) continue;
                }

                result.add(hotel);
            }
        } catch (IOException e) {
            log.error("숙소 추천 json 파싱 오류", e);
        }
        return result;
    }

    private static final String KAKAO_GEOCODE_URL = "https://dapi.kakao.com/v2/local/search/address.json";
    private static final String KAKAO_KEYWORD_SEARCH_URL = "https://dapi.kakao.com/v2/local/search/keyword.json";
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    public Map<String, Double> getCoordinates(String address) {
        String url = UriComponentsBuilder.fromHttpUrl(KAKAO_GEOCODE_URL)
                .queryParam("query", address)
                .toUriString();

        try {
            var headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            
            var response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new org.springframework.http.HttpEntity<>(headers),
                    String.class
            );
            
            // Parse response and extract coordinates
            var root = objectMapper.readTree(response.getBody());
            var documents = root.path("documents");
            if (documents.isArray() && documents.size() > 0) {
                var location = documents.get(0);
                Map<String, Double> coords = new HashMap<>();
                coords.put("latitude", location.path("y").asDouble());
                coords.put("longitude", location.path("x").asDouble());
                return coords;
            }
        } catch (Exception e) {
            log.error("Error getting coordinates for address: " + address, e);
        }
        return null;
    }

    public List<Map<String, Object>> searchPlaces(String query, String category, double lat, double lng, int radius) {
        String url = UriComponentsBuilder.fromHttpUrl(KAKAO_KEYWORD_SEARCH_URL)
                .queryParam("query", query)
                .queryParam("category_group_code", category)
                .queryParam("x", lng)
                .queryParam("y", lat)
                .queryParam("radius", radius)
                .toUriString();

        try {
            var headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            
            var response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    new org.springframework.http.HttpEntity<>(headers),
                    String.class
            );
            
            // Parse and return places
            var root = objectMapper.readTree(response.getBody());
            var documents = root.path("documents");
            List<Map<String, Object>> places = new ArrayList<>();
            
            for (var doc : documents) {
                Map<String, Object> place = new HashMap<>();
                place.put("name", doc.path("place_name").asText());
                place.put("address", doc.path("address_name").asText());
                place.put("phone", doc.path("phone").asText());
                place.put("category", doc.path("category_name").asText());
                place.put("latitude", doc.path("y").asDouble());
                place.put("longitude", doc.path("x").asDouble());
                places.add(place);
            }
            return places;
        } catch (Exception e) {
            log.error("Error searching places", e);
            return Collections.emptyList();
        }
    }

    public List<Map<String, String>> loadTouristSpots() {
        List<Map<String, String>> spots = new ArrayList<>();
        String csvFile = "static/data/국내 지역별 관광명소 데이터.csv";
        
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(
                        new ClassPathResource(csvFile).getInputStream(),
                        "EUC-KR" // Adjust encoding if needed
                ))) {
            
            String[] headers = reader.readNext(); // Skip header
            String[] line;
            
            while ((line = reader.readNext()) != null) {
                Map<String, String> spot = new HashMap<>();
                for (int i = 0; i < headers.length && i < line.length; i++) {
                    spot.put(headers[i].trim(), line[i].trim());
                }
                spots.add(spot);
            }
        } catch (IOException | CsvValidationException e) {
            log.error("Error reading tourist spots CSV", e);
        }
        
        return spots;
    }

    public List<Map<String, Object>> findNearbyAccommodations(double lat, double lng, int radius) {
        return searchPlaces("", "AD5", lat, lng, radius);
    }
}
