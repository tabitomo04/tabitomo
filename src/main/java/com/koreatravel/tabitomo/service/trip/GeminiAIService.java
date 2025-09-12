package com.koreatravel.tabitomo.service.trip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.fasterxml.jackson.databind.type.CollectionType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.koreatravel.tabitomo.client.KakaoLocalApiClient;
import com.koreatravel.tabitomo.domain.dto.trip.TourRecommendationDTO;
import com.koreatravel.tabitomo.domain.dto.trip.TourRecommendationDTO.ItineraryItem;
import com.koreatravel.tabitomo.domain.dto.trip.AccommodationDTO;

@Service
public class GeminiAIService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GeminiAIService.class);

    @Value("${spring.ai.vertex.ai.gemini.api-endpoint}")
    private String geminiApiEndpoint;
    
    @Value("${spring.ai.vertex.ai.project-id}")
    private String projectId;
    
    @Value("${spring.ai.vertex.ai.location}")
    private String location;
    
    @Value("${app.google.maps.api.key}")
    private String googleApiKey;
    
    @Value("${app.pixabay.api.key}")
    private String pixabayApiKey;
    
    @Value("${app.google.cloud.translation.api-key}")
    private String translationApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final KakaoLocalApiClient kakaoLocalApiClient;
    private final ResourceLoader resourceLoader;

    private List<Map<String, String>> touristSpotData;
    private List<Map<String, Object>> accommodationData;

    public GeminiAIService(RestTemplate restTemplate, ObjectMapper objectMapper, KakaoLocalApiClient kakaoLocalApiClient, ResourceLoader resourceLoader) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.kakaoLocalApiClient = kakaoLocalApiClient;
        this.resourceLoader = resourceLoader;
        loadData();
    }

    private void loadData() {
        try {
            // 관광명소 CSV 로드
            Resource csvResource = resourceLoader.getResource("classpath:static/data/국내 지역별 관광명소 데이터.csv");
            try (CSVReader reader = new CSVReader(new InputStreamReader(csvResource.getInputStream(), "UTF-8"))) {
                List<String[]> allRows = reader.readAll();
                if (!allRows.isEmpty()) {
                    String[] headers = allRows.get(0);
                    touristSpotData = allRows.subList(1, allRows.size()).stream()
                        .map(row -> {
                            Map<String, String> map = new HashMap<>();
                            for (int i = 0; i < headers.length; i++) {
                                if (i < row.length) {
                                    map.put(headers[i], row[i]);
                                }
                            }
                            return map;
                        })
                        .collect(Collectors.toList());
                    logger.info("관광명소 데이터 로드 완료. {}개 항목.", touristSpotData.size());
                }
            }

            // 숙박시설 JSON 로드
            Resource jsonResource = resourceLoader.getResource("classpath:static/data/전국 숙박시설 가격,부대시설 정보.json");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(jsonResource.getInputStream(), "UTF-8"))) {
                String jsonString = reader.lines().collect(Collectors.joining("\n"));
                accommodationData = objectMapper.readValue(jsonString, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>(){});
                logger.info("숙박시설 데이터 로드 완료. {}개 항목.", accommodationData.size());
            }

        } catch (IOException | CsvException e) {
            logger.error("데이터 로드 중 에러 발생: {}", e.getMessage(), e);
            touristSpotData = Collections.emptyList();
            accommodationData = Collections.emptyList();
        }
    }

    public TourRecommendationDTO getRecommendation(String destination, String duration, String theme, String budget, Integer accommodationBudget, List<String> facilities) {
        return getRecommendationDTO(destination, duration, theme, budget);
    }

    private TourRecommendationDTO getRecommendationDTO(String destination, String duration, String theme, String budget) {
        // Construct the Vertex AI Gemini API endpoint
        String apiUrl = String.format("https://%s/v1/projects/%s/locations/%s/publishers/google/models/gemini-pro:generateContent",
            geminiApiEndpoint, projectId, location);
            
        // Create headers with authentication
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Set authentication
        headers.setBearerAuth(translationApiKey);  // Using the same API key for authentication

        StringBuilder promptBuilder = new StringBuilder();
        // Convert duration to natural language if it's a number
        String durationText = duration;
        try {
            int nights = Integer.parseInt(duration);
            int days = nights + 1;
            durationText = String.format("%d박 %d일", nights, days);
        } catch (NumberFormatException e) {
            // If it's already in natural language, use it as is
            logger.debug("Using provided duration text: {}", duration);
        }
        promptBuilder.append(String.format(
            "사용자가 요청한 여행지, 기간, 테마에 맞춰서 상세한 여행 코스와 숙소를 추천해줘. 각 장소에 대한 설명을 포함하고, 검색 가능한 실제 장소 이름을 사용해줘. '(가상)'이라는 단어는 이름에 넣지마. 위도와 경도는 내가 직접 찾을거야. 이미지 URL은 제공하지마.\n" +
            "여행지: %s\n" +
            "기간: %s\n" +
            "테마: %s\n" +
            "특히, 각 날짜별로 4~6개 이상의 다양한 장소(명소, 식당, 카페, 체험 등)를 추천해줘. 하루에 가능한 한 다양한 카테고리로 일정을 구성해줘.\n",
            destination, durationText, theme
        ));
        // 반드시 destination 내에서만 추천하도록 명시
        promptBuilder.append(String.format("모든 추천 결과(숙소, 명소)는 반드시 '%s' 지역 내에서만 추천해줘. 다른 지역은 절대 포함하지 마.\n", destination));
        // 여행 기간 안내 추가
        promptBuilder.append(String.format("여행 기간이 %s이므로, 일정의 day 값을 1부터 %s까지 나눠서 반환해줘.\n", durationText, durationText.replaceAll("[^0-9]", "").isEmpty() ? "여행일수" : String.valueOf(durationText.replaceAll("[^0-9]", ""))));

        // 가격대 정보 추가
        if (budget != null && !budget.isEmpty()) {
            promptBuilder.append(String.format("숙소 가격대는 %s입니다. 이 가격대에 맞는 숙소를 추천해줘.\n", budget));
        }

        // 관광명소 데이터 필터링 및 프롬프트에 추가
        if (touristSpotData != null && !touristSpotData.isEmpty()) {
            List<Map<String, String>> filteredSpots = touristSpotData.stream()
                .filter(spot -> spot.getOrDefault("지역", "").contains(destination))
                .collect(Collectors.toList());
            if (!filteredSpots.isEmpty()) {
                promptBuilder.append("\n다음은 참고할 수 있는 해당 지역의 관광명소 데이터입니다:\n");
                filteredSpots.stream().limit(5).forEach(spot ->
                    promptBuilder.append(String.format("- %s (주소: %s, 분류: %s)\n",
                        spot.getOrDefault("관광지명", ""),
                        spot.getOrDefault("소재지도로명주소", ""),
                        spot.getOrDefault("관광지구분", "")
                    ))
                );
                promptBuilder.append("이 데이터를 활용하여 여행 코스를 구성하는 데 참고해줘.\n");
            }
        }

        // 숙박시설 데이터 필터링 및 프롬프트에 추가
        if (accommodationData != null && !accommodationData.isEmpty()) {
            List<Map<String, Object>> filteredAccommodations = accommodationData.stream()
                .filter(acc -> acc.getOrDefault("소재지도로명주소", "").toString().contains(destination))
                .filter(acc -> {
                    if (budget == null || budget.isEmpty()) return true;
                    try {
                        String priceStr = acc.getOrDefault("객실평균가격", "0").toString().replaceAll("[^\\d.]", "");
                        double avgPrice = Double.parseDouble(priceStr);
                        String[] range = budget.split("-");
                        double minPrice = Double.parseDouble(range[0]);
                        double maxPrice = Double.parseDouble(range[1]);
                        return avgPrice >= minPrice && avgPrice <= maxPrice;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

            if (!filteredAccommodations.isEmpty()) {
                promptBuilder.append("\n다음은 참고할 수 있는 해당 지역의 숙박시설 데이터입니다:\n");
                filteredAccommodations.stream().limit(3).forEach(acc ->
                    promptBuilder.append(String.format("- %s (주소: %s, 가격: %s원, 부대시설: %s)\n",
                        acc.getOrDefault("숙박업소명", ""),
                        acc.getOrDefault("소재지도로명주소", ""),
                        acc.getOrDefault("객실평균가격", "정보 없음"),
                        acc.getOrDefault("부대시설", "정보 없음")
                    ))
                );
                promptBuilder.append("이 데이터를 활용하여 숙소를 추천하는 데 참고해줘.\n");
            }
        }

        promptBuilder.append(
            "\n응답은 반드시 JSON 형식으로 해줘. 여행 기간이 여러 날일 경우, 각 일정의 day 값을 1, 2, 3...으로 나눠서 반환해줘.\n" +
            "예시:\n" +
            "{\n" +
            "  \"title\": \"추천 여행 코스 제목\",\n" +
            "  \"itinerary\": [\n" +
            "    {\"day\": 1, \"time\": \"09:00-11:00\", \"category\": \"명소\", \"place_name\": \"장소 이름1\", \"description\": \"설명1\", \"image_url\": \"\"},\n" +
            "    {\"day\": 2, \"time\": \"09:00-11:00\", \"category\": \"명소\", \"place_name\": \"장소 이름2\", \"description\": \"설명2\", \"image_url\": \"\"},\n" +
            "    {\"day\": 3, \"time\": \"09:00-11:00\", \"category\": \"명소\", \"place_name\": \"장소 이름3\", \"description\": \"설명3\", \"image_url\": \"\"}\n" +
            "  ],\n" +
            "  \"accommodations\": [\n" +
            "    {\"day\": 1, \"time\": \"체크인\", \"category\": \"숙소\", \"place_name\": \"숙소 이름1\", \"description\": \"숙소 설명1\", \"image_url\": \"\", \"price_range\": \"저렴\"},\n" +
            "    {\"day\": 2, \"time\": \"체크인\", \"category\": \"숙소\", \"place_name\": \"숙소 이름2\", \"description\": \"숙소 설명2\", \"image_url\": \"\", \"price_range\": \"저렴\"}\n" +
            "  ]\n" +
            "}\n"
        );

        try {
            // Create the request body for Vertex AI
            Map<String, Object> requestBody = new HashMap<>();
            
            // Create the content structure expected by Vertex AI
            Map<String, Object> content = new HashMap<>();
            content.put("role", "user");
            content.put("parts", Collections.singletonList(Collections.singletonMap("text", promptBuilder.toString())));
            
            requestBody.put("contents", Collections.singletonList(content));
            
            // Add generation config
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.2);
            generationConfig.put("topP", 0.8);
            generationConfig.put("topK", 40);
            generationConfig.put("maxOutputTokens", 2048);
            
            requestBody.put("generationConfig", generationConfig);

            // Make the API request
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

            logger.info("Gemini API 원본 응답: {}", response.getBody()); // 원본 응답 로깅

            return parseResponse(response.getBody());

        } catch (Exception e) {
            logger.error("Gemini API 호출 중 에러 발생: {}", e.getMessage(), e);
            return new TourRecommendationDTO("추천을 받지 못했습니다.", Collections.emptyList());
        }
    }

    private TourRecommendationDTO parseResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            String textContent = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            logger.info("Gemini 응답 텍스트 내용: {}", textContent); // 텍스트 내용 로깅

            // Markdown JSON 코드 블록 제거 및 공백 제거
            String cleanJson = textContent.trim().replace("```json", "").replace("```", "").trim();
            // trailing comma 제거 (마지막 필드 뒤 쉼표)
            cleanJson = cleanJson.replaceAll(",\\s*([}\\]])", "$1");
            logger.info("클린 JSON (마크다운 및 trailing comma 제거): {}", cleanJson); // 클린 JSON 로깅

            // 유효한 JSON 부분만 추출
            int startIndex = cleanJson.indexOf('{');
            int endIndex = cleanJson.lastIndexOf('}');

            if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
                logger.error("응답에서 유효한 JSON 부분을 찾을 수 없습니다: {}", textContent);
                return new TourRecommendationDTO("응답 파싱 오류", Collections.emptyList());
            }

            String jsonContent = cleanJson.substring(startIndex, endIndex + 1);
            logger.info("추출된 JSON 내용: {}", jsonContent); // 추출된 JSON 내용 로깅

            // 추출한 JSON 문자열 파싱
            JsonNode recommendationNode = objectMapper.readTree(jsonContent);
            String title = recommendationNode.path("title").asText();

            JsonNode itineraryNode = recommendationNode.path("itinerary");
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, ItineraryItem.class);
            List<ItineraryItem> itinerary = objectMapper.convertValue(itineraryNode, listType);

            // Kakao Local API 및 Geocoding을 통해 정보 보강
            for (ItineraryItem item : itinerary) {
                updateItemWithApiData(item);
            }

            // Create a new TourRecommendationDTO with the title and itinerary
            TourRecommendationDTO dto = new TourRecommendationDTO();
            dto.setTitle(title);
            dto.setItinerary(itinerary);
            
            // If there are accommodations, use the first one as the main accommodation
            if (recommendationNode.has("accommodation")) {
                try {
                    JsonNode accNode = recommendationNode.get("accommodation");
                    AccommodationDTO accommodation = new AccommodationDTO();
                    accommodation.setPlaceName(accNode.path("place").asText());
                    accommodation.setDescription(accNode.path("description").asText(""));
                    accommodation.setLatitude(accNode.path("latitude").asDouble(0.0));
                    accommodation.setLongitude(accNode.path("longitude").asDouble(0.0));
                    // Set default price range if available
                    accommodation.setPriceRange("N/A");
                    dto.setAccommodation(accommodation);
                } catch (Exception e) {
                    logger.error("Error setting accommodation: {}", e.getMessage(), e);
                }
            }
            
            return dto;
        } catch (JsonProcessingException e) {
            logger.error("JSON 파싱 에러: {}", e.getMessage(), e);
            return new TourRecommendationDTO("응답 파싱 오류", Collections.emptyList());
        } catch (Exception e) {
            logger.error("응답 처리 에러: {}", e.getMessage(), e);
            return new TourRecommendationDTO("알 수 없는 오류", Collections.emptyList());
        }
    }

    private void updateItemWithApiData(TourRecommendationDTO.ItineraryItem item) {
        // Try to find additional data from our datasets
        String placeName = item.getPlace();
        if (placeName == null || placeName.trim().isEmpty()) {
            return;
        }
        String cleanedPlaceName = item.getPlaceName().replace("(가상)", "").trim();
        item.setPlaceName(cleanedPlaceName);

        try {
            // 1. Kakao Local API로 키워드 검색
            String searchResponse = kakaoLocalApiClient.searchKeyword(cleanedPlaceName, 1, 1);
            JsonNode searchRoot = objectMapper.readTree(searchResponse);
            JsonNode documents = searchRoot.path("documents");

            if (documents.isArray() && documents.size() > 0) {
                JsonNode firstResult = documents.get(0);
                item.setLatitude(firstResult.path("y").asDouble()); // 위도
                item.setLongitude(firstResult.path("x").asDouble()); // 경도
                item.setRestDate("정보 없음"); // 카카오 로컬 API에서 휴무일 정보는 직접 제공하지 않음
                item.setUseTime("정보 없음"); // 카카오 로컬 API에서 이용시간 정보는 직접 제공하지 않음
                item.setDescription(firstResult.path("address_name").asText("")); // 주소를 설명으로 사용

                // Kakao API에서 정보를 찾았으므로 Pixabay 이미지 검색 시도
                String imageUrl = getPixabayImageUrl(cleanedPlaceName);
                item.setImageUrl(imageUrl);
                return; // Kakao API에서 정보를 찾았으므로 종료
            }
        } catch (Exception e) {
            logger.error("Kakao Local API 처리 중 에러 발생: {}", e.getMessage(), e);
        }

        // 2. Kakao Local API에서 정보를 못 찾으면 Geocoding API로 좌표 검색
        updateCoordinatesFromGeocoding(item, cleanedPlaceName);
        // Geocoding API로 좌표를 찾은 후에도 이미지 검색 시도
        String imageUrl = getPixabayImageUrl(cleanedPlaceName);
        item.setImageUrl(imageUrl); // 이미지 URL은 항상 설정
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
            return ""; // 이미지를 찾지 못하면 빈 문자열 반환
        }
        return ""; // 이미지를 찾지 못하면 빈 문자열 반환
    }

    private void updateCoordinatesFromGeocoding(ItineraryItem item, String placeNameForGeocoding) {
        try {
            String url = String.format(
                "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s",
                placeNameForGeocoding, googleApiKey
            );
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            if (response != null && response.path("status").asText().equals("OK")) {
                JsonNode location = response.path("results").get(0).path("geometry").path("location");
                item.setLatitude(location.path("lat").asDouble());
                item.setLongitude(location.path("lng").asDouble());
            }
        } catch (Exception e) {
            logger.error("Geocoding API 호출 중 에러 발생: {}", e.getMessage(), e);
        }
    }
}
