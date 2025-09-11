package com.koreatravel.tabitomo.service.chat;

import com.koreatravel.tabitomo.domain.dto.chat.ChatRequestDTO;
import com.koreatravel.tabitomo.domain.dto.chat.ChatResponseDTO;
import com.koreatravel.tabitomo.domain.entity.chat.ChatCategoryEntity;
import com.koreatravel.tabitomo.domain.entity.chat.ChatKeywordEntity;
import com.koreatravel.tabitomo.domain.entity.chat.ChatQAEntity;
import com.koreatravel.tabitomo.domain.entity.chat.ForbiddenWordEntity;
import com.koreatravel.tabitomo.repository.chat.ChatCategoryRepository;
import com.koreatravel.tabitomo.repository.chat.ChatKeywordRepository;
import com.koreatravel.tabitomo.repository.chat.ChatQARepository;
import com.koreatravel.tabitomo.repository.chat.ForbiddenWordRepository;
import com.koreatravel.tabitomo.repository.chat.SynonymRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatQARepository qaRepo;
    @Autowired
    private ChatKeywordRepository keywordRepo;
    @Autowired
    private SynonymRepository synonymRepo;
    @Autowired
    private OpenAiService openAiService;
    @Autowired
    private ChatCategoryRepository categoryRepo;
    @Autowired
    private ForbiddenWordRepository forbiddenWordRepo;

    // 카테고리 목록을 가져옵니다.
    public List<ChatCategoryEntity> getCategories() {
        return categoryRepo.findAll();
    }

    // 특정 카테고리의 Q&A 목록을 가져옵니다.
    public List<ChatQAEntity> getQAsByCategory(Integer categoryId) {
        return qaRepo.findByCategoryId(categoryId);
    }

    // 언어 코드를 변환하는 헬퍼 메서드
    public String processLanguageCode(String lang) {
        if (lang != null) {
            switch (lang.toLowerCase()) {
                case "en":
                    return "en-US";
                case "ja":
                    return "ja-JP";
                default:
                    return "ko-KR";
            }
        }
        return "ko-KR";
    }
    
    // 키워드 기반 응답 생성 (점수 기반)
    public String getAnswerByKeywordWithScore(String input, int threshold) {
        // 1. 동의어 처리
        String processedInput = processSynonyms(input);
        
        // 2. 금지어 검사
        if (containsForbiddenWord(processedInput)) {
            return "죄송합니다. 부적절한 단어가 포함되어 있습니다.";
        }
        
        // 3. 키워드 기반 점수 계산
        List<ChatKeywordEntity> keywords = keywordRepo.findByKeywordInInput(processedInput);
        Map<Integer, Integer> qaScores = new HashMap<>();
        
        // 4. 각 키워드에 대한 가중치 합산
        for (ChatKeywordEntity keyword : keywords) {
            int currentScore = qaScores.getOrDefault(keyword.getQaId(), 0);
            qaScores.put(keyword.getQaId(), currentScore + keyword.getWeight());
        }
        
        // 5. 최고 점수 Q&A 찾기
        return qaScores.entrySet().stream()
                .filter(entry -> entry.getValue() >= threshold)
                .max(Map.Entry.comparingByValue())
                .map(entry -> {
                    Optional<ChatQAEntity> qaOpt = qaRepo.findById(entry.getKey());
                    return qaOpt.map(ChatQAEntity::getAnswer)
                             .orElse("죄송합니다. 적절한 답변을 찾을 수 없습니다.");
                })
                .orElse("죄송합니다. 질문을 이해하지 못했습니다. 더 자세히 설명해 주시겠어요?");
    }

    // 이전에 삭제했던 메서드를 다시 추가합니다.
    public String getAnswerByKeyword(String input) {
        String processedInput = processSynonyms(input);
        List<ChatKeywordEntity> keywords = keywordRepo.findByKeywordInInput(processedInput);
        if (!keywords.isEmpty()) {
            return keywords.get(0).getChatQA().getAnswer();
        }
        return "죄송합니다. 정확한 답변을 찾지 못했습니다.";
    }

    // 질문 ID를 기반으로 답변을 반환합니다.
    public String getAnswerByQaId(Integer qaId) {
        return qaRepo.findById(qaId).map(ChatQAEntity::getAnswer).orElse("답변이 없습니다.");
    }

    // 1. 키워드 점수 합산 방식으로 DB에서 ChatQA 객체 검색
    public Optional<ChatQAEntity> getChatQAByKeywordWithScore(String input, int threshold) {
        String processedInput = processSynonyms(input);
        List<ChatKeywordEntity> keywords = keywordRepo.findByKeywordInInput(processedInput);
        Map<Integer, Integer> qaScores = new HashMap<>();

        for (ChatKeywordEntity keyword : keywords) {
            int currentScore = qaScores.getOrDefault(keyword.getQaId(), 0);
            qaScores.put(keyword.getQaId(), currentScore + keyword.getWeight());
        }

        Optional<Integer> bestQaId = qaScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .filter(entry -> entry.getValue() >= threshold)
                .map(Map.Entry::getKey);

        if (bestQaId.isPresent()) {
            return qaRepo.findById(bestQaId.get());
        }

        return Optional.empty();
    }

    // AI를 사용하여 DB 답변을 보정/확장하는 새로운 메서드
    public String refineAnswerWithAI(String question, String dbAnswer) {
        try {
            String prompt = "다음 답변을 친근하고 유머러스한 AI 친구 '토모'의 말투로 자연스럽게 다듬고 확장해줘. "
                    + "너무 길지 않게 간결하게 해줘.\n\n"
                    + "질문: " + question + "\n"
                    + "답변: " + dbAnswer;

            // ChatRequest req = new ChatRequest();
            // req.setMessage(prompt);
            // 기존 getChatResponseFromAI를 직접 호출하지 않고 OpenAiService를 직접 호출하여 리팩토링합니다.
            // 이 방식이 더 깔끔하고 유연합니다.
            // OpenAiService에 적절한 메서드가 있다고 가정합니다.

            // OpenAiService에 맥락없는 질문을 던지는 메서드가 필요합니다.
            // 예를 들어 `getSingleResponse(String prompt)` 같은 메서드가 있으면 더 좋습니다.
            // 임시로 기존 getChatResponseFromAI를 사용하도록 변경합니다.
            ChatRequestDTO singleReq = new ChatRequestDTO();
            singleReq.setMessage(prompt);
            return openAiService.getChatResponse(singleReq).getReply();
        } catch (Exception e) {
            System.err.println("AI 답변 보정 중 오류 발생: " + e.getMessage());
            return dbAnswer;
        }
    }

    // ⭐️ 새로 추가된 핵심 메서드: 전체 채팅 기록을 기반으로 답변을 처리합니다.
    public ChatResponseDTO handleChatRequest(ChatRequestDTO request) {
        // 1. 먼저 DB에서 키워드 기반 답변을 찾습니다. (임계값 3으로 설정)
        Optional<ChatQAEntity> dbAnswer = getChatQAByKeywordWithScore(request.getMessage(), 3);

        if (dbAnswer.isPresent()) {
            // 2. DB에서 답변을 찾았다면, AI를 사용해 답변을 다듬고 반환합니다.
            String refinedAnswer = refineAnswerWithAI(dbAnswer.get().getQuestion(), dbAnswer.get().getAnswer());
            ChatResponseDTO response = new ChatResponseDTO();
            response.setReply(refinedAnswer);
            response.setAnswerSource("DB");
            return response;
        } else {
            // 3. DB에서 답변을 찾지 못했다면, 전체 채팅 기록을 AI에 보내 답변을 받습니다.
            try {
                // 이전 대화 기록을 AI 모델에 전달할 수 있는 형태로 변환합니다.
                List<Map<String, String>> chatHistory = request.getChatHistory();
                String fullPrompt = chatHistory.stream()
                        .map(chat -> {
                            String sender = chat.get("sender");
                            String message = chat.get("message");
                            if ("user".equals(sender)) {
                                return "사용자: " + message;
                            } else {
                                return "AI 챗봇: " + message;
                            }
                        })
                        .collect(Collectors.joining("\n"));

                // AI 모델에 최종 프롬프트를 전송합니다.
                ChatRequestDTO aiRequest = new ChatRequestDTO();
                aiRequest.setMessage(fullPrompt);

                // OpenAiService의 getChatResponseFromAI 메서드가 ChatRequest를 받도록 되어있으므로,
                // 이를 사용하여 대화 맥락을 전달합니다.
                return getChatResponseFromAI(aiRequest);

            } catch (Exception e) {
                System.err.println("AI 답변 생성 중 오류 발생: " + e.getMessage());
                ChatResponseDTO errorResponse = new ChatResponseDTO();
                errorResponse.setReply("죄송합니다. AI 답변 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
                errorResponse.setAnswerSource("SYSTEM");
                return errorResponse;
            }
        }
    }

    // AI에 일반 답변을 요청하는 메서드 (Controller에서 호출)
    public ChatResponseDTO getChatResponseFromAI(ChatRequestDTO req) {
        try {
            return openAiService.getChatResponse(req);
        } catch (Exception e) {
            System.err.println("AI 답변 생성 중 오류 발생: " + e.getMessage());
            ChatResponseDTO errorResponse = new ChatResponseDTO();
            errorResponse.setReply("죄송합니다. AI 답변 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
            errorResponse.setAnswerSource("SYSTEM");
            return errorResponse;
        }
    }

    // 동의어 처리 로직 (임의로 구현)
    private String processSynonyms(String input) {
        Optional<String> processedKeyword = synonymRepo.findAll().stream()
                .filter(synonym -> input.equals(synonym.getSynonymKeyword()) || input.contains(synonym.getSynonymKeyword()))
                .findFirst()
                .map(synonym -> synonym.getMainKeyword());

        return processedKeyword.orElse(input);
    }

    public boolean containsForbiddenWord(String input) {
        List<ForbiddenWordEntity> forbiddenWords = forbiddenWordRepo.findAll();
        String lowerCaseInput = input.toLowerCase();
        for (ForbiddenWordEntity word : forbiddenWords) {
            if (lowerCaseInput.contains(word.getWord().toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}