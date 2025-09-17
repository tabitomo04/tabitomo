package com.koreantravel.tabitomo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoLocalApiClient {

    @Value("${kakao.local.api.key}")
    private String kakaoLocalApiKey;

    private final RestTemplate restTemplate;

    private static final String KAKAO_KEYWORD_SEARCH_URL = "https://dapi.kakao.com/v2/local/search/keyword.json";
    private static final String KAKAO_ADDRESS_SEARCH_URL = "https://dapi.kakao.com/v2/local/search/address.json";

    public String searchKeyword(String query, int page, int size) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoLocalApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = String.format("%s?query=%s&page=%d&size=%d", KAKAO_KEYWORD_SEARCH_URL, query, page, size);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        } catch (Exception e) {
            log.error("Error calling Kakao Local API for keyword query: {}", query, e);
            return null;
        }
    }

    public String searchAddress(String address, int page, int size) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoLocalApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = String.format("%s?query=%s&page=%d&size=%d", KAKAO_ADDRESS_SEARCH_URL, address, page, size);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        } catch (Exception e) {
            log.error("Error calling Kakao Local API for address query: {}", address, e);
            return null;
        }
    }
}
