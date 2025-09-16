package com.koreatravel.tabitomo.service;

import com.koreatravel.tabitomo.dto.chat.ChatRequest;
import com.koreatravel.tabitomo.dto.chat.ChatResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String apiKey;

    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";

    // ChatService에서 전달하는 형식에 맞춰 언어별 시스템 프롬프트 키를 수정했습니다.
    private static final Map<String, String> SYSTEM_PROMPTS = Map.of(
            "ko-KR", "너는 토모라는 AI 친구야. 항상 친근하고 귀여운 말투로 대답하는 한국 여행 가이드 AI야. " +
                    "AI처럼 대답하지 말고 부가적인 표시 없이 말로만 대답해야 해. 존댓말 대신 반말로 편하게 써. " +
                    "답변은 여러 문단으로 나누고, 중요한 내용 사이에는 줄바꿈 문자를 포함해줘.",
            "en-US", "You are an AI friend named Tomo, a friendly and cute Korean travel guide. " +
                    "Always respond in a friendly and casual tone. Don't respond like an AI; " +
                    "just speak naturally without any extra markings. Please use informal language instead of polite speech. " +
                    "Divide your answer into multiple paragraphs and include line breaks between important parts.",
            "ja-JP", "あなたはトモというAIの友達だよ。いつも親しみやすく可愛い口調で答える韓国旅行ガイドAIだよ。 " +
                    "AIのように答えず、付加的な表示なしで言葉だけで答えてね。敬語ではなく、タメ口で気軽に話して。 " +
                    "回答は複数の段落に分け、重要な内容の間には改行を含めてね。"
    );

    public ChatResponse getChatResponse(ChatRequest req) {
        // ChatService에서 전달하는 정확한 언어 코드를 사용하도록 수정했습니다.
        String language = Optional.ofNullable(req.getLanguage()).orElse("ko-KR");
        String systemPrompt = SYSTEM_PROMPTS.getOrDefault(language, SYSTEM_PROMPTS.get("ko-KR"));

        // API 요청 바디 구성
        Map<String, Object> requestBodyMap = new HashMap<>();

        // 1. 시스템 지침 추가 (Contents와 별개로 최상위 필드에 위치)
        requestBodyMap.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))));

        // 2. 메시지 히스토리를 포함한 전체 대화 내용 구성
        List<Map<String, Object>> contents = new ArrayList<>();

        // 2-1. 기존 채팅 기록 추가
        if (req.getChatHistory() != null) {
            for (Map<String, String> chat : req.getChatHistory()) {
                String role = "user".equals(chat.get("sender")) ? "user" : "model";
                contents.add(Map.of("role", role, "parts", List.of(Map.of("text", chat.get("message")))));
            }
        }

        // 2-2. 현재 사용자 메시지 추가
        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", req.getMessage()))));

        requestBodyMap.put("contents", contents);

        try {
            String response = webClient.post()
                    .uri(GEMINI_URL + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBodyMap)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), clientResponse ->
                            Mono.error(new RuntimeException("API Client Error: " + clientResponse.statusCode())))
                    .onStatus(status -> status.is5xxServerError(), clientResponse ->
                            Mono.error(new RuntimeException("API Server Error: " + clientResponse.statusCode())))
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)).jitter(0.5))
                    .block();

            JsonNode rootNode = objectMapper.readTree(response);
            String reply = extractReply(rootNode);

            if (reply == null || reply.isEmpty()) {
                reply = "AI로부터 유효한 답변을 받지 못했습니다. 다시 시도해 주세요.";
            }

            ChatResponse chatResponse = new ChatResponse();
            chatResponse.setReply(reply);
            return chatResponse;
        } catch (Exception e) {
            System.err.println("API 호출 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setReply("API 호출 중 오류가 발생했습니다. 키 설정을 확인하거나 잠시 후 다시 시도해 주세요.");
            errorResponse.setAnswerSource("SYSTEM");
            return errorResponse;
        }
    }

    // Gemini 응답을 안전하게 파싱하는 메소드
    private String extractReply(JsonNode response) {
        if (response == null) {
            return "응답이 없습니다.";
        }

        try {
            JsonNode candidates = response.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    JsonNode firstPart = parts.get(0);
                    JsonNode textNode = firstPart.path("text");
                    if (textNode.isTextual()) {
                        return textNode.asText();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("응답 파싱 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return "응답 파싱 중 알 수 없는 오류가 발생했습니다.";
        }
        return "답변을 가져오지 못했습니다. 예상치 못한 응답 구조입니다.";
    }
}
