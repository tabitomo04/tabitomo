package com.koreatravel.tabitomo.service.chat;

import com.koreatravel.tabitomo.domain.dto.chat.ChatRequestDTO;
import com.koreatravel.tabitomo.domain.dto.chat.ChatResponseDTO;
import com.koreatravel.tabitomo.repository.chat.SynonymRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// AnswerService.java
@Service
public class AnswerService {

    @Autowired
    private ChatService chatService; // 기존 DB 기반 답변 서비스

    @Autowired
    private OpenAiService openAiService; // AI API 호출 서비스

    @Autowired
    private SynonymRepository synonymRepo; // 동의어 처리를 위한 레포지토리

    private static final int THRESHOLD = 5; // 키워드 점수 임계값

    // 반환 타입을 String에서 ChatResponse로 변경
    public ChatResponseDTO getHybridAnswer(ChatRequestDTO chatRequest) {
        String userMessage = chatRequest.getMessage();
        // 1. 동의어 처리 및 키워드 추출
        String processedMessage = processSynonyms(userMessage);

        // 2. DB 기반 답변 시도 (다중 키워드 점수 합산)
        String dbAnswer = chatService.getAnswerByKeywordWithScore(processedMessage, THRESHOLD);

        // 3. 임계값 초과 시 DB 답변 반환 (ChatResponse 객체로 생성)
        if (dbAnswer != null) {
            ChatResponseDTO response = new ChatResponseDTO();
            response.setReply(dbAnswer);
            response.setAnswerSource("DB");
            return response;
        }

        // 4. 임계값 미만 시 AI API 호출
        ChatResponseDTO aiResponse = openAiService.getChatResponse(chatRequest);
        aiResponse.setAnswerSource("AI");
        return aiResponse;
    }

    // 동의어 처리 로직 (Synonym 테이블 활용)
    private String processSynonyms(String text) {
        // Synonym 테이블을 조회하여 동의어를 메인 키워드로 변환하는 로직 구현
        // 예: "회원가입" -> "가입"
        return text;
    }
}
