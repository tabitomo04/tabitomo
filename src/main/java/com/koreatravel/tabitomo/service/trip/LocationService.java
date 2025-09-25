package com.koreatravel.tabitomo.service.trip;

import com.koreatravel.tabitomo.domain.dto.trip.AddressComponent;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.koreatravel.tabitomo.domain.entity.trip.Place;
import com.koreatravel.tabitomo.repository.trip.PlaceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PlaceRepository placeRepository;

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @Value("${google.maps.api.key}")
    private String googleMapsApiKey;

    private static final String GOOGLE_PLACES_API_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json";

    private static final Map<String, String> REGION_MAP = new HashMap<>();
    static {
        REGION_MAP.put("서울", "서울특별시");
        REGION_MAP.put("서울특별시", "서울특별시");
        REGION_MAP.put("부산", "부산광역시");
        REGION_MAP.put("부산광역시", "부산광역시");
        REGION_MAP.put("대구", "대구광역시");
        REGION_MAP.put("대구광역시", "대구광역시");
        REGION_MAP.put("인천", "인천광역시");
        REGION_MAP.put("인천광역시", "인천광역시");
        REGION_MAP.put("광주", "광주광역시");
        REGION_MAP.put("광주광역시", "광주광역시");
        REGION_MAP.put("대전", "대전광역시");
        REGION_MAP.put("대전광역시", "대전광역시");
        REGION_MAP.put("울산", "울산광역시");
        REGION_MAP.put("울산광역시", "울산광역시");
        REGION_MAP.put("세종", "세종특별자치시");
        REGION_MAP.put("세종특별자치시", "세종특별자치시");
        REGION_MAP.put("경기", "경기도");
        REGION_MAP.put("경기도", "경기도");
        REGION_MAP.put("강원", "강원특별자치도");
        REGION_MAP.put("강원도", "강원특별자치도"); // 이전 명칭 처리
        REGION_MAP.put("강원특별자치도", "강원특별자치도");
        REGION_MAP.put("충북", "충청북도");
        REGION_MAP.put("충청북도", "충청북도");
        REGION_MAP.put("충남", "충청남도");
        REGION_MAP.put("충청남도", "충청남도");
        REGION_MAP.put("전북", "전북특별자치도");
        REGION_MAP.put("전라북도", "전북특별자치도"); // 이전 명칭 처리
        REGION_MAP.put("전북특별자치도", "전북특별자치도");
        REGION_MAP.put("전남", "전라남도");
        REGION_MAP.put("전라남도", "전라남도");
        REGION_MAP.put("경북", "경상북도");
        REGION_MAP.put("경상북도", "경상북도");
        REGION_MAP.put("경남", "경상남도");
        REGION_MAP.put("경상남도", "경상남도");
        REGION_MAP.put("제주", "제주특별자치도");
        REGION_MAP.put("제주도", "제주특별자치도"); // 이전 명칭 처리
        REGION_MAP.put("제주특별자치도", "제주특별자치도");
    }

    public AddressComponent parseAddress(String address) {
        if (address == null || address.isBlank()) {
            return new AddressComponent(null, null);
        }

        String[] parts = address.trim().split("\s+");
        if (parts.length == 0) {
            return new AddressComponent(null, null);
        }

        String regionPart = parts[0];
        String fullRegion = REGION_MAP.get(regionPart);

        if (fullRegion != null) {
            String city = (parts.length > 1) ? parts[1] : null;
            return new AddressComponent(fullRegion, city);
        } else {
            return new AddressComponent(null, null);
        }
    }

    public List<Map<String, Object>> searchGooglePlaces(String query) {
        URI uri = UriComponentsBuilder.fromHttpUrl(GOOGLE_PLACES_API_URL)
                .queryParam("query", query)
                .queryParam("key", googleMapsApiKey)
                .queryParam("language", "ko")
                .build()
                .toUri();

        try {
            String response = restTemplate.getForObject(uri, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.path("results");

            List<Map<String, Object>> places = new ArrayList<>();
            if (results.isArray()) {
                for (JsonNode result : results) {
                    Map<String, Object> place = new HashMap<>();
                    place.put("name", result.path("name").asText());
                    place.put("address", result.path("formatted_address").asText());
                    JsonNode location = result.path("geometry").path("location");
                    place.put("latitude", location.path("lat").asDouble());
                    place.put("longitude", location.path("lng").asDouble());
                    places.add(place);
                }
            }
            return places;
        } catch (Exception e) {
            log.error("Error searching Google Places for query: " + query, e);
            return Collections.emptyList();
        }
    }

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
        List<Place> places = placeRepository.findAll();
        return places.stream()
                .map(place -> {
                    Map<String, String> spot = new HashMap<>();
                    spot.put("name", place.getName());
                    spot.put("categoryCode", place.getCategoryCode());
                    spot.put("address", place.getAddress());
                    spot.put("city", place.getCity());
                    spot.put("region", place.getRegion());
                    if (place.getLatitude() != null) {
                        spot.put("latitude", String.valueOf(place.getLatitude()));
                    }
                    if (place.getLongitude() != null) {
                        spot.put("longitude", String.valueOf(place.getLongitude()));
                    }
                    spot.put("imageUrl", place.getImageUrl());
                    spot.put("description", place.getDescription());
                    spot.put("priceRange", place.getPriceRange());
                    return spot;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> findNearbyAccommodations(double lat, double lng, int radius) {
        return searchPlaces("", "AD5", lat, lng, radius);
    }
}
