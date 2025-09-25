package com.koreatravel.tabitomo.service.trip;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import com.koreatravel.tabitomo.client.GoogleMapsApiClient;
import com.koreatravel.tabitomo.client.KakaoLocalApiClient;
import com.koreatravel.tabitomo.domain.dto.trip.ScheduleInfo;
import com.koreatravel.tabitomo.domain.dto.trip.TourRecommendation;
import com.koreatravel.tabitomo.domain.entity.trip.Place;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeminiAIService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GeminiAIService.class);

    @Value("${google.gemini.api-key}")
    private String geminiApiKey;

    @Value("${pixabay.api.key}")
    private String pixabayApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final KakaoLocalApiClient kakaoLocalApiClient;
    private final GoogleMapsApiClient googleMapsApiClient; // Inject Google Maps API client
    private final ResourceLoader resourceLoader;

    private List<Map<String, Object>> accommodationData;

    private static final Map<String, String> DESTINATION_MAP = Map.ofEntries(
        Map.entry("경상남도", "경남"),
        Map.entry("경상북도", "경북"),
        Map.entry("충청남도", "충남"),
        Map.entry("충청북도", "충북"),
        Map.entry("전라남도", "전남"),
        Map.entry("전북특별자치도", "전북"),
        Map.entry("강원특별자치도", "강원"),
        Map.entry("제주특별자치도", "제주"),
        Map.entry("서울특별시", "서울"),
        Map.entry("부산광역시", "부산"),
        Map.entry("대구광역시", "대구"),
        Map.entry("인천광역시", "인천"),
        Map.entry("광주광역시", "광주"),
        Map.entry("대전광역시", "대전"),
        Map.entry("울산광역시", "울산"),
        Map.entry("세종특별자치시", "세종"),
        Map.entry("경기도", "경기")
    );

    public GeminiAIService(RestTemplate restTemplate, ObjectMapper objectMapper, KakaoLocalApiClient kakaoLocalApiClient, GoogleMapsApiClient googleMapsApiClient, ResourceLoader resourceLoader) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.kakaoLocalApiClient = kakaoLocalApiClient;
        this.googleMapsApiClient = googleMapsApiClient;
        this.resourceLoader = resourceLoader;
        loadData();
    }

    private void loadData() {
        try {
            Resource resource = resourceLoader.getResource("classpath:static/data/전국 숙박시설 가격,부대시설 정보.json");
            InputStream inputStream = resource.getInputStream();
            String jsonData = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            accommodationData = objectMapper.readValue(jsonData, new TypeReference<List<Map<String, Object>>>() {});
            logger.info("Successfully loaded {} accommodation records.", accommodationData.size());
        } catch (IOException e) {
            logger.error("Failed to load accommodation data", e);
            accommodationData = Collections.emptyList();
        }
    }

    public TourRecommendation getRecommendation(String destination, String duration, String theme, String priceRange, Integer accommodationBudget, List<String> facilities) {
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent?key=" + geminiApiKey;

        StringBuilder promptBuilder = new StringBuilder();
        String durationText = duration;
        try {
            int nights = Integer.parseInt(duration);
            int days = nights + 1;
            durationText = String.format("%d박 %d일", nights, days);
        } catch (Exception e) {
            // Ignore
        }
        promptBuilder.append(String.format(
    "사용자가 요청한 여행지, 기간, 테마에 맞춰서 상세한 여행 코스와 숙소를 추천해줘. 숙소 추천은 필수야. 각 장소에 대한 설명을 포함하고, 반드시 지도에서 검색 가능한 실제 장소 이름과 대한민국 행정안전부에서 제공하는 공식 도로명 주소 형식의 정확한 전체 주소를 사용해줘. 주소는 절대 꾸며내지 말고, 검증된 실제 주소여야만 해. '(가상)'이라는 단어는 이름에 넣지마.\n" +
    "여행지: %s\n" +
    "기간: %s\n" +
    "테마: %s\n",
    destination, durationText, theme
));

        promptBuilder.append(String.format("모든 추천 결과(명소, 숙소)는 반드시 '%s' 지역 내에서만 추천해야 합니다. 주소에 '%s'가 포함되지 않는 장소는 절대 추천하지 마세요.\n", destination, destination));

        List<Map<String, Object>> filteredAccommodations = accommodationData.stream()
            .filter(Objects::nonNull)
            .filter(acc -> Optional.ofNullable(acc.get("지역")).map(Object::toString).map(destination::contains).orElse(false))
            .filter(acc -> {
                if (accommodationBudget == null || accommodationBudget <= 0) return true;
                return Optional.ofNullable(acc.get("가격")).map(Object::toString).map(s -> s.replaceAll("[^\\d]", "")).filter(s -> !s.isEmpty()).map(Integer::parseInt).map(price -> price <= accommodationBudget).orElse(false);
            })
            .filter(acc -> {
                if (facilities == null || facilities.isEmpty()) return true;
                return Optional.ofNullable(acc.get("부대시설")).map(Object::toString).map(facilityInfo -> facilities.stream().allMatch(facilityInfo::contains)).orElse(false);
            })
            .collect(Collectors.toList());

        if (filteredAccommodations.isEmpty()) {
            promptBuilder.append(String.format("숙소는 여행 기간 전체를 대표하는 단 하나를 반드시 추천해야 해. 사용자가 제시한 조건에 맞는 숙소를 찾지 못했더라도, 반드시 %s 내에서 아래 조건을 최대한 만족하는 다른 숙소를 찾아서 추천해줘.\n", destination));
            if (accommodationBudget != null && accommodationBudget > 0) {
                promptBuilder.append(String.format("- 숙소 예산: %d원 이하\n", accommodationBudget));
            }
            if (facilities != null && !facilities.isEmpty()) {
                promptBuilder.append(String.format("- 희망 부대시설: %s\n", String.join(", ", facilities)));
            }
        } else {
            promptBuilder.append("숙소는 반드시 다음 목록 안에서만 하나를 골라서 추천해줘야 해. 다른 숙소는 절대 추천하면 안돼. 목록에 있는 정보(특히 가격과 주소)를 최대한 활용해서 추천해줘.\n");
            promptBuilder.append("--- 숙소 목록 시작 ---\n");
            for (Map<String, Object> acc : filteredAccommodations) {
                promptBuilder.append(String.format("- 상호명: %s, 주소: %s, 가격: %s, 부대시설: %s\n", acc.get("상호명"), acc.get("주소"), acc.get("가격"), acc.get("부대시설")));
            }
            promptBuilder.append("--- 숙소 목록 끝 ---\n");
        }

        promptBuilder.append(
            "\n응답은 반드시 JSON 형식으로 해줘. `accommodation` 필드는 필수 항목이므로 절대 생략하면 안돼. `address` 필드도 항상 실제적이고 검증된 주소로 포함해줘.\n" +
            "예시:\n" +
            "{\n" +
            "  \"title\": \"추천 여행 코스 제목\",\n" +
            "  \"itinerary\": [\n" +
            "    {\"day\": 1, \"time\": \"09:00-11:00\", \"category\": \"명소\", \"place_name\": \"해운대해수욕장\", \"description\": \"설명1\", \"address\": \"부산광역시 해운대구 우동\"}\n" +
            "  ],\n" +
            "  \"accommodation\": {\n" +
            "    \"category\": \"숙소\", \"place_name\": \"토요코인 부산역1\", \"description\": \"숙소 설명1\", \"price_range\": \"100000-150000\", \"address\": \"부산광역시 동구 중앙대로 225\"\n" +
            "  }\n" +
            "}\n"
        );

        try {
            Map<String, Object> part = Map.of("text", promptBuilder.toString());
            Map<String, Object> content = Map.of("parts", List.of(part));
            Map<String, Object> requestBodyMap = Map.of("contents", List.of(content));
            String requestBody = objectMapper.writeValueAsString(requestBodyMap);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject(apiUrl, entity, String.class);
            logger.info("Gemini API 원본 응답: {}", response);
            return parseResponse(response, destination);

        } catch (Exception e) {
            logger.error("Gemini API 호출 중 에러 발생: {}", e.getMessage(), e);
            return new TourRecommendation("추천을 받지 못했습니다.", Collections.emptyList(), null);
        }
    }

    private TourRecommendation parseResponse(String jsonResponse, String destination) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            String textContent = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            String cleanJson = textContent.trim().replace("```json", "").replace("```", "").trim().replaceAll(",\\s*([}\\]])", "$1");
            int startIndex = cleanJson.indexOf('{');
            int endIndex = cleanJson.lastIndexOf('}');
            if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
                logger.error("응답에서 유효한 JSON 부분을 찾을 수 없습니다: {}", textContent);
                return new TourRecommendation("응답 파싱 오류", Collections.emptyList(), null);
            }
            String jsonContent = cleanJson.substring(startIndex, endIndex + 1);
            JsonNode recommendationNode = objectMapper.readTree(jsonContent);
            String title = recommendationNode.path("title").asText();

            List<ScheduleInfo> itinerary = new ArrayList<>();
            JsonNode itineraryNode = recommendationNode.path("itinerary");
            if (itineraryNode.isArray()) {
                for (JsonNode itemNode : itineraryNode) {
                    Place place = createAndEnrichPlace(itemNode, destination, false);
                    if (place != null) {
                        ScheduleInfo scheduleInfo = new ScheduleInfo();
                        scheduleInfo.setDay(itemNode.path("day").asInt());
                        String timeRange = itemNode.path("time").asText();
                        String[] times = timeRange.split("-|~");
                        scheduleInfo.setStartTime(times.length > 0 ? times[0].trim() : timeRange);
                        scheduleInfo.setEndTime(times.length > 1 ? times[1].trim() : "");
                        scheduleInfo.setMemo(itemNode.path("description").asText());
                        scheduleInfo.setPlace(place);
                        itinerary.add(scheduleInfo);
                    }
                }
            }

            Place accommodation = null;
            JsonNode accommodationNode = recommendationNode.path("accommodation");

            JsonNode targetAccommodationNode = null;
            if (accommodationNode.isObject() && accommodationNode.size() > 0) {
                targetAccommodationNode = accommodationNode;
            } else if (accommodationNode.isArray() && accommodationNode.size() > 0) {
                logger.warn("AI returned an array for accommodation, using the first element.");
                targetAccommodationNode = accommodationNode.get(0);
            }

            if (targetAccommodationNode != null) {
                accommodation = createAndEnrichPlace(targetAccommodationNode, destination, true);
            }

            return new TourRecommendation(title, itinerary, accommodation);

        } catch (Exception e) {
            logger.error("응답 처리 에러: {}", e.getMessage(), e);
            return new TourRecommendation("알 수 없는 오류", Collections.emptyList(), null);
        }
    }

    private boolean isAddressInDestination(String address, String destination) {
        if (address == null || destination == null) {
            return false;
        }
        String shortName = DESTINATION_MAP.getOrDefault(destination, destination);
        return address.startsWith(destination) || address.startsWith(shortName);
    }

    private Place createAndEnrichPlace(JsonNode itemNode, String destination, boolean isAccommodation) {
        String placeName = itemNode.path("place_name").asText(null);
        if (placeName == null || placeName.trim().isEmpty()) {
            logger.warn("JSON item is missing or has empty 'place_name'. Skipping.");
            return null;
        }
        String cleanedPlaceName = placeName.replace("(가상)", "").trim();
        String addressFromAI = itemNode.path("address").asText(null);

        try {
            // 1. Try searching by keyword first (Kakao)
            String searchResponse = kakaoLocalApiClient.searchKeyword(cleanedPlaceName, 1, 1);
            JsonNode searchRoot = objectMapper.readTree(searchResponse);
            JsonNode documents = searchRoot.path("documents");

            // 2. If keyword search fails, try searching by address (Kakao)
            if ((!documents.isArray() || documents.size() == 0) && addressFromAI != null && !addressFromAI.trim().isEmpty()) {
                logger.warn("Kakao keyword search for '{}' failed. Retrying with address: {}", cleanedPlaceName, addressFromAI);
                searchResponse = kakaoLocalApiClient.searchAddress(addressFromAI, 1, 1);
                searchRoot = objectMapper.readTree(searchResponse);
                documents = searchRoot.path("documents");
            }

            if (documents.isArray() && documents.size() > 0) {
                JsonNode firstResult = documents.get(0);
                String address = firstResult.path("address_name").asText("");
                if (address.isEmpty() && firstResult.has("road_address")) {
                    address = firstResult.path("road_address").path("address_name").asText();
                }

                if (!isAddressInDestination(address, destination)) {
                    logger.warn("Place '{}' with address '{}' is outside the destination '{}'. Skipping for itinerary, but including for accommodation.", cleanedPlaceName, address, destination);
                    if (!isAccommodation) return null;
                }

                return Place.builder()
                    .name(cleanedPlaceName)
                    .categoryCode(itemNode.path("category").asText())
                    .description(itemNode.path("description").asText())
                    .priceRange(itemNode.path("price_range").asText())
                    .address(address)
                    .latitude(firstResult.path("y").asDouble())
                    .longitude(firstResult.path("x").asDouble())
                    .imageUrl(getPixabayImageUrl(cleanedPlaceName))
                    .build();
            }
        } catch (Exception e) {
            logger.error("Kakao API data enrichment failed for place '{}': {}", cleanedPlaceName, e.getMessage());
        }

        // 3. If all Kakao searches fail, try Google Geocoding API
        if (addressFromAI != null && !addressFromAI.trim().isEmpty()) {
            logger.warn("All Kakao searches failed for '{}'. Retrying with Google Geocoding API.", cleanedPlaceName);
            Optional<GoogleMapsApiClient.Coordinates> coords = googleMapsApiClient.geocodeAddress(addressFromAI);
            if (coords.isPresent()) {
                return Place.builder()
                    .name(cleanedPlaceName)
                    .categoryCode(itemNode.path("category").asText())
                    .description(itemNode.path("description").asText())
                    .priceRange(itemNode.path("price_range").asText())
                    .address(addressFromAI)
                    .latitude(coords.get().getLatitude())
                    .longitude(coords.get().getLongitude())
                    .imageUrl(getPixabayImageUrl(cleanedPlaceName))
                    .build();
            }
        }

        logger.warn("Could not enrich place data for '{}' after all attempts.", cleanedPlaceName);

        if (isAccommodation) {
            logger.warn("Creating partial Place object for accommodation: {}. Address from AI: {}", cleanedPlaceName, addressFromAI);
            return Place.builder()
                .name(cleanedPlaceName)
                .categoryCode(itemNode.path("category").asText())
                .description(itemNode.path("description").asText())
                .priceRange(itemNode.path("price_range").asText())
                .address(addressFromAI) // At least save the address from AI
                .imageUrl(getPixabayImageUrl(cleanedPlaceName))
                .build();
        }

        logger.warn("It will not be included.");
        return null;
    }

    private String getPixabayImageUrl(String query) {
        try {
            String pixabayUrl = String.format(
                "https://pixabay.com/api/?key=%s&q=%s&image_type=photo&per_page=3",
                pixabayApiKey, query
            );
            JsonNode response = restTemplate.getForObject(pixabayUrl, JsonNode.class);
            if (response != null && response.path("hits").isArray() && response.path("hits").size() > 0) {
                return response.path("hits").get(0).path("webformatURL").asText("");
            }
        } catch (Exception e) {
            logger.error("Pixabay API 호출 중 에러 발생: {}", e.getMessage(), e);
        }
        return "";
    }
}
