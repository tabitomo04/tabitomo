package com.koreatravel.tabitomo.service;

import com.koreatravel.tabitomo.domain.dto.ChatRequestDTO;
import com.koreatravel.tabitomo.domain.dto.ChatResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    // Gemini API URL (Google Generative Language API)
    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";


    public ChatResponseDTO getChatResponse(ChatRequestDTO req) {
        // Gemini 요청 바디에 시스템 지시문(systemInstruction) 추가
        Map<String, Object> requestBody = Map.of(
                "systemInstruction", Map.of(
                        "parts", List.of(
                                Map.of("text", "너는 토모라는 AI 친구야. 항상 친근하고 귀여운 말투로 대답하는 한국 여행 가이드 AI야. 유머도 약간 섞어. 너무 길게 말하지 말고 간결하게 대답해줘. AI처럼 대답하지 말고 부가적인 표시 없이 말로만 대답해야 해. 존댓말 대신 반말로 편하게 써.")
                        )
                ),
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", req.getMessage())
                        ))
                )
        );

        try {
            // WebClient를 이용한 Gemini API 호출 및 지수 백오프 재시도 로직 적용
            Map response = webClient.post()
                    .uri(GEMINI_URL + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse -> {
                        if (clientResponse.statusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                            return Mono.error(new RuntimeException("API 요청 오류: 할당량 초과. 무료 요청 횟수를 모두 사용했습니다."));
                        }
                        return clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException(
                                        "API 요청 오류: " + clientResponse.statusCode() + " - " + errorBody
                                )));
                    })
                    .bodyToMono(Map.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                            .filter(e -> e.getMessage().contains("429 Too Many Requests"))
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                    new RuntimeException("API 요청 실패: 최대 재시도 횟수 초과", retrySignal.failure())))
                    .block();

            String reply = extractReply(response);
            if (reply == null || reply.isEmpty()) {
                reply = "AI로부터 유효한 답변을 받지 못했습니다. 다시 시도해 주세요.";
            }

            ChatResponseDTO chatResponse = new ChatResponseDTO();
            chatResponse.setReply(reply);
            return chatResponse;

        } catch (Exception e) {
            // 모든 예외를 여기서 잡아서 명확한 오류 메시지 반환
            System.err.println("API 호출 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            ChatResponseDTO errorResponse = new ChatResponseDTO();
            errorResponse.setReply("API 호출 중 오류가 발생했습니다. 키 설정을 확인하거나 잠시 후 다시 시도해 주세요.");
            errorResponse.setAnswerSource("SYSTEM");
            return errorResponse;
        }
    }

    // Gemini 응답을 안전하게 파싱하는 메소드
    private String extractReply(Map response) {
        if (response == null) {
            return "응답이 없습니다.";
        }

        try {
            Object candidatesObj = response.get("candidates");
            if (candidatesObj instanceof List<?> candidates && !candidates.isEmpty()) {
                Object firstCandidate = candidates.get(0);
                if (firstCandidate instanceof Map<?, ?> candidateMap) {
                    Object contentObj = candidateMap.get("content");
                    if (contentObj instanceof Map<?, ?> contentMap) {
                        Object partsObj = contentMap.get("parts");
                        if (partsObj instanceof List<?> parts && !parts.isEmpty()) {
                            Object firstPart = parts.get(0);
                            if (firstPart instanceof Map<?, ?> partMap) {
                                Object text = partMap.get("text");
                                if (text instanceof String) {
                                    return (String) text;
                                } else {
                                    return "API 응답에서 'text' 필드가 유효하지 않습니다.";
                                }
                            }
                        }
                    } else {
                        return "API 응답에서 'content' 필드가 유효하지 않습니다.";
                    }
                }
            } else {
                return "API 응답에서 'candidates' 필드가 유효하지 않습니다. (내용이 비어있을 수 있습니다.)";
            }
        } catch (Exception e) {
            System.err.println("응답 파싱 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return "응답 파싱 중 알 수 없는 오류가 발생했습니다.";
        }
        return "답변을 가져오지 못했습니다. 예상치 못한 응답 구조입니다.";
    }
}
