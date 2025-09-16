package com.koreatravel.tabitomo.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleGeocodingClient {

    @Value("${google.maps.api.key:}") // Add your key to application.properties
    private String googleApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String GEOCODING_API_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    public JsonNode geocode(String address) {
        if (googleApiKey == null || googleApiKey.isEmpty()) {
            log.warn("Google Maps API key is not configured. Skipping geocoding.");
            return null;
        }

        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(GEOCODING_API_URL)
                .queryParam("address", address)
                .queryParam("key", googleApiKey)
                .queryParam("language", "ko");

            String url = builder.toUriString();
            log.info("Calling Google Geocoding API: {}", url);

            String response = restTemplate.getForObject(url, String.class);
            if (response == null) {
                return null;
            }

            JsonNode root = objectMapper.readTree(response);
            if (root.path("status").asText().equals("OK")) {
                return root.path("results");
            }
        } catch (Exception e) {
            log.error("Error calling Google Geocoding API for address: {}", address, e);
        }
        return null;
    }
}
