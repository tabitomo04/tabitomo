package com.koreatravel.tabitomo.service.chat;

import com.koreatravel.tabitomo.domain.dto.chat.FeedbackRequest;
import com.koreatravel.tabitomo.domain.entity.chat.Feedback;
import com.koreatravel.tabitomo.domain.entity.chat.LearningData;
import com.koreatravel.tabitomo.repository.chat.FeedbackRepository;
import com.koreatravel.tabitomo.repository.chat.LearningDataRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final ObjectMapper objectMapper;

    // 생성자 주입
    public FeedbackService(FeedbackRepository feedbackRepository, ObjectMapper objectMapper) {
        this.feedbackRepository = feedbackRepository;
        this.objectMapper = objectMapper;
    }

    // 피드백을 저장하거나 업데이트하는 메서드
    public Feedback saveOrUpdateFeedback(FeedbackRequest feedbackRequest, Long feedbackId) throws JsonProcessingException {
        Feedback feedback;
        if (feedbackId != null) {
            // 기존 피드백을 찾아서 업데이트. Long 타입인 feedbackId를 Integer로 변환합니다.
            feedback = feedbackRepository.findById(feedbackId.intValue()).orElse(new Feedback());
        } else {
            // 새로운 피드백 저장
            feedback = new Feedback();
        }

        feedback.setRating(feedbackRequest.getRating());
        feedback.setFeedbackText(feedbackRequest.getFeedbackText());
        feedback.setTimestamp(LocalDateTime.now());
        // 추가된 코드: FeedbackRequest에서 isHelpful 값을 읽어와 Feedback 엔티티에 설정합니다.
        // DTO의 Integer(0 또는 1) 값을 Entity의 Boolean으로 변환합니다.
        feedback.setIsHelpful(feedbackRequest.getIsHelpful() != null && feedbackRequest.getIsHelpful() == 1);
        String chatHistoryJson = objectMapper.writeValueAsString(feedbackRequest.getChatHistory());
        feedback.setChatHistory(chatHistoryJson);

        // 변경된 피드백을 저장하고, 저장된 객체를 반환
        return feedbackRepository.save(feedback);
    }
}