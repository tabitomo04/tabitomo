package com.koreatravel.tabitomo.client;

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

    @Value("${app.kakao.local.api.key}")
    private String kakaoLocalApiKey;

    private final RestTemplate restTemplate;

    private static final String KAKAO_LOCAL_API_BASE_URL = "https://dapi.kakao.com/v2/local/search/keyword.json";

    public String searchKeyword(String query, int page, int size) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoLocalApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = String.format("%s?query=%s&page=%d&size=%d", KAKAO_LOCAL_API_BASE_URL, query, page, size);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        } catch (Exception e) {
            log.error("Error calling Kakao Local API for query: {}", query, e);
            return null;
        }
    }
}
