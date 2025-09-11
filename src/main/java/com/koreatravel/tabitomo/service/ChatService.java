package com.koreatravel.tabitomo.service;

import com.koreatravel.tabitomo.dto.ChatRequest;
import com.koreatravel.tabitomo.dto.ChatResponse;
import com.koreatravel.tabitomo.entity.ChatKeyword;
import com.koreatravel.tabitomo.entity.ChatQA;
import com.koreatravel.tabitomo.entity.ForbiddenWord;
import com.koreatravel.tabitomo.entity.MainCategory;
import com.koreatravel.tabitomo.entity.SubCategory;
import com.koreatravel.tabitomo.repository.*;
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
    private MainCategoryRepository mainCategoryRepo;
    @Autowired
    private SubCategoryRepository subCategoryRepo;
    @Autowired
    private ForbiddenWordRepository forbiddenWordRepo;

    // 언어 코드를 변환하는 헬퍼 메서드
    private String processLanguageCode(String lang) {
        if (lang != null) {
            switch (lang.toLowerCase()) {
                case "en":
                    return "en-US";
                case "ja":
                    return "ja-JP";
                default:
                    return lang;
            }
        }
        return "ko-KR";
    }

    // 메인 카테고리 목록을 가져옵니다.
    public List<MainCategory> getCategories() {
        return mainCategoryRepo.findAll();
    }

    // 특정 메인 카테고리의 서브 카테고리 목록을 가져옵니다.
    public List<SubCategory> getSubCategoriesByMainCategory(Integer mainCategoryId) {
        return subCategoryRepo.findByMainCategoryId(mainCategoryId);
    }

    // 특정 서브 카테고리의 Q&A 목록을 가져옵니다.
    public List<ChatQA> getQAsByCategory(Integer subCategoryId) {
        return qaRepo.findBySubCategoryId(subCategoryId);
    }

    // 기존 getAnswerByKeyword 메서드 확장 (점수 기반)
    public String getAnswerByKeywordWithScore(String input, int threshold) {
        String processedInput = processSynonyms(input);
        List<ChatKeyword> keywords = keywordRepo.findByKeywordInInput(processedInput);
        Map<Integer, Integer> qaScores = new HashMap<>();

        for (ChatKeyword keyword : keywords) {
            int currentScore = qaScores.getOrDefault(keyword.getQaId(), 0);
            qaScores.put(keyword.getQaId(), currentScore + keyword.getWeight());
        }

        Optional<Integer> bestQaId = qaScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .filter(entry -> entry.getValue() >= threshold)
                .map(Map.Entry::getKey);

        if (bestQaId.isPresent()) {
            return qaRepo.findById(bestQaId.get()).map(ChatQA::getAnswer).orElse(null);
        }

        return null;
    }

    // 이전에 삭제했던 메서드를 다시 추가합니다.
    public String getAnswerByKeyword(String input) {
        String processedInput = processSynonyms(input);
        List<ChatKeyword> keywords = keywordRepo.findByKeywordInInput(processedInput);
        if (!keywords.isEmpty()) {
            return keywords.get(0).getChatQA().getAnswer();
        }
        return "죄송합니다. 정확한 답변을 찾지 못했습니다.";
    }

    // 질문 ID를 기반으로 답변을 반환합니다.
    public String getAnswerByQaId(Integer qaId) {
        return qaRepo.findById(qaId).map(ChatQA::getAnswer).orElse("답변이 없습니다.");
    }

    // 1. 키워드 점수 합산 방식으로 DB에서 ChatQA 객체 검색
    public Optional<ChatQA> getChatQAByKeywordWithScore(String input, int threshold) {
        String processedInput = processSynonyms(input);
        List<ChatKeyword> keywords = keywordRepo.findByKeywordInInput(processedInput);
        Map<Integer, Integer> qaScores = new HashMap<>();

        for (ChatKeyword keyword : keywords) {
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
    public String refineAnswerWithAI(String question, String dbAnswer, String language) {
        try {
            String prompt = "다음 답변을 친근하고 유머러스한 AI 친구 '토모'의 말투로 자연스럽게 다듬고 확장해줘. "
                    + "너무 길지 않게 간결하게 해줘.\n\n"
                    + "질문: " + question + "\n"
                    + "답변: " + dbAnswer;

            ChatRequest singleReq = new ChatRequest();
            singleReq.setMessage(prompt);
            singleReq.setLanguage(processLanguageCode(language)); // 언어 코드 변환
            return openAiService.getChatResponse(singleReq).getReply();
        } catch (Exception e) {
            System.err.println("AI 답변 보정 중 오류 발생: " + e.getMessage());
            return dbAnswer;
        }
    }

    // ⭐️ 새로 추가된 핵심 메서드: 전체 채팅 기록을 기반으로 답변을 처리합니다.
    public ChatResponse handleChatRequest(ChatRequest request) {
        // 1. 먼저 DB에서 키워드 기반 답변을 찾습니다. (임계값 3으로 설정)
        Optional<ChatQA> dbAnswer = getChatQAByKeywordWithScore(request.getMessage(), 3);

        if (dbAnswer.isPresent()) {
            // 2. DB에서 답변을 찾았다면, AI를 사용해 답변을 다듬고 반환합니다.
            String refinedAnswer = refineAnswerWithAI(dbAnswer.get().getQuestion(), dbAnswer.get().getAnswer(), request.getLanguage()); // 언어 정보 전달
            ChatResponse response = new ChatResponse();
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
                ChatRequest aiRequest = new ChatRequest();
                aiRequest.setMessage(fullPrompt);
                aiRequest.setLanguage(processLanguageCode(request.getLanguage())); // 언어 코드 변환

                return openAiService.getChatResponse(aiRequest);

            } catch (Exception e) {
                System.err.println("AI 답변 생성 중 오류 발생: " + e.getMessage());
                ChatResponse errorResponse = new ChatResponse();
                errorResponse.setReply("죄송합니다. AI 답변 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
                errorResponse.setAnswerSource("SYSTEM");
                return errorResponse;
            }
        }
    }

    // AI에 일반 답변을 요청하는 메서드 (Controller에서 호출)
    public ChatResponse getChatResponseFromAI(ChatRequest req) {
        try {
            // 여기에도 언어 코드 변환 로직 추가
            req.setLanguage(processLanguageCode(req.getLanguage()));
            return openAiService.getChatResponse(req);
        } catch (Exception e) {
            System.err.println("AI 답변 생성 중 오류 발생: " + e.getMessage());
            ChatResponse errorResponse = new ChatResponse();
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
                .map(synonym -> synonym.getChatKeyword().getKeyword());

        return processedKeyword.orElse(input);
    }

    public boolean containsForbiddenWord(String input) {
        List<ForbiddenWord> forbiddenWords = forbiddenWordRepo.findAll();
        String lowerCaseInput = input.toLowerCase();
        for (ForbiddenWord word : forbiddenWords) {
            if (lowerCaseInput.contains(word.getWord().toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
