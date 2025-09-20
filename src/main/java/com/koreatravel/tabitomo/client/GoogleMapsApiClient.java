package com.koreatravel.tabitomo.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleMapsApiClient {

    @Value("${google.maps.api.key}")
    private String googleMapsApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String GOOGLE_GEOCODING_API_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    public Optional<Coordinates> geocodeAddress(String address) {
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(GOOGLE_GEOCODING_API_URL)
                    .queryParam("address", address)
                    .queryParam("key", googleMapsApiKey)
                    .queryParam("language", "ko");

            String url = uriBuilder.toUriString();
            log.info("Requesting Google Geocoding API: {}", url);

            String response = restTemplate.getForObject(url, String.class);
            if (response == null) {
                log.warn("Google Geocoding API returned null response for address: {}", address);
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response);
            String status = root.path("status").asText();

            if (!"OK".equals(status)) {
                log.warn("Google Geocoding API returned status: {} for address: {}", status, address);
                return Optional.empty();
            }

            JsonNode location = root.path("results").get(0).path("geometry").path("location");
            double lat = location.path("lat").asDouble();
            double lng = location.path("lng").asDouble();

            return Optional.of(new Coordinates(lat, lng));

        } catch (Exception e) {
            log.error("Error calling Google Geocoding API for address: {}", address, e);
            return Optional.empty();
        }
    }

    public static class Coordinates {
        private final double latitude;
        private final double longitude;

        public Coordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }
}
