package com.koreatravel.tabitomo.service.chat;

import com.koreatravel.tabitomo.domain.dto.chat.ChatRequest;
import com.koreatravel.tabitomo.domain.dto.chat.ChatResponse;
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
                    "AI처럼 딱딱하게 대답하지 말고 자연스럽게 말로만 대답해. 존댓말 대신 반말로 편하게 써. " +
                    "사용자가 간단하게 질문하면, 굳이 상세한 정보를 묻지 마. " +
                    "답변은 항상 핵심 정보만 담아 간결하게 작성해줘. " +
                    "목록을 시작할 때 항상 한 줄짜리 **굵은 글씨** 소제목을 사용하여 내용을 요약해줘. " +
                    "사용자가 상세한 정보를 요청하지 않는 한, 답변은 2~3개 문단 이내로 짧게 마무리해줘. " +
                    "## 마크다운 사용 스타일 가이드" +
                    "1. **소제목**: 답변 시작은 항상 한 줄짜리 **굵은 글씨** 소제목으로 내용을 요약해줘. 새로운 주제나 핵심 정보를 소개할 때만 추가로 사용해. 소제목은 최대 2개까지만 사용해줘." +
                    "2. **강조**: 장소 이름, 음식 이름처럼 중요한 키워드는 **굵은 글씨**로 강조해서 눈에 띄게 해줘. 단순한 문장 부사나 형용사는 강조하지 마." +
                    "3. **목록**: 여러 개의 추천이나 정보를 나열할 때는 깔끔하게 목록(-)을 사용해줘. 예를 들어, 추천 장소를 3곳 이상 소개할 때 사용하면 좋아." +
                    "4. **기타**: 표나 기울임(*) 같은 마크다운은 꼭 필요할 때만 사용해줘. 예를 들어, 표는 가격이나 영업시간처럼 구조화된 정보를 보여줄 때만 사용해. " +
                    "불필요한 마크다운을 남발하지 말고 내용이 깔끔하게 보이도록 꼭 필요한 곳에만 사용해줘.",

            "en-US", "You are an AI friend named Tomo. You are a Korean travel guide AI who always answers in a friendly and cute tone. " +
                    "Don't answer like a stiff AI. Speak naturally and conversationally. Use casual language instead of honorifics." +
                    "If the user asks a simple question, don't ask for detailed information." +
                    "Always keep your answers concise, containing only the key information." +
                    "Always start your lists with a single line of **bold text** as a summary headline." +
                    "Unless the user asks for detailed information, keep your answers short, within 2-3 paragraphs." +
                    "## Markdown Usage Style Guide" +
                    "1. Subheading: Always start your answer with a single-line, bold subheading summarizing the content. Use additional subheadings only when introducing new topics or key information. Use a maximum of 2 subheadings." +
                    "2. Emphasis: Use bold for key keywords (like place names or food names) to make them stand out. Do not use bold for simple adverbs or adjectives." +
                    "3. Lists: When listing multiple pieces of information and recommend, use a clean list (-). For example, use a list when recommending 3 or more places." +
                    "4. Other: Use other markdown like tables or italics(*) only when absolutely necessary. For example, use a table only to display structured information like prices or operating hours. " +
                    "Do not overuse unnecessary markdown; use it only where it is essential for clean content.",

            "ja-JP", "あなたはAIフレンドのトモです。いつもフレンドリーで可愛らしい口調で答える、韓国旅行ガイドAIです。" +
                    "AIのように堅苦しく答えず、自然な話し方で答えてください。敬語ではなく、タメ口で気軽に話してください。" +
                    "ユーザーが簡単な質問をした場合、わざわざ詳細な情報を尋ねないでください。" +
                    "回答は常に核心情報だけを簡潔にまとめてください。" +
                    "リストを始める際には、必ず一行の**太字**の見出しを使って内容を要約してね。"+
                    "ユーザーが詳細な情報を要求しない限り、回答は2〜3段落以内で短くまとめてください。" +
                    "## マークダウン使用スタイルガイド" +
                    "1. **小見出し**: 回答の冒頭は、常に**太字**の一行小見出しで内容を要約してください。新しい話題や核心情報を紹介する時のみ追加で使用し、最大2つまでにしてください。" +
                    "2. **強調**: 場所の名前や食べ物の名前など、重要なキーワードは**太字**で強調して目立たせてください。単純な副詞や形容詞は強調しないでください。" +
                    "3. **リスト**: 複数のおすすめと情報などを並べる場合は、リスト(-)を使ってきれいに整理してください。例えば、3つ以上の場所を推薦する場合に使うと良いでしょう。" +
                    "4. **その他**: 表や斜体(*)のようなマークダウンは、本当に必要な場合にのみ使用してください。例えば、表は価格や営業時間のように構造化された情報を見せる時にのみ使います。" +
                    "不要なマークダウンを乱用せず、内容がすっきり見えるように必要最小限に留めてください。"

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
