package com.koreatravel.tabitomo.service.chat;

import com.koreatravel.tabitomo.domain.dto.chat.FeedbackRequest;
import com.koreatravel.tabitomo.domain.entity.chat.Feedback;
import com.koreatravel.tabitomo.repository.chat.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    // 생성자 주입
    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    // 피드백을 저장하거나 업데이트하는 메서드
    public Feedback saveOrUpdateFeedback(FeedbackRequest feedbackRequest, Long feedbackId) {
        Feedback feedback;
        if (feedbackId != null) {
            // 기존 피드백을 찾아서 업데이트. Long 타입인 feedbackId를 Integer로 변환합니다.
            feedback = feedbackRepository.findById(feedbackId.intValue()).orElse(new Feedback());
        } else {
            // 새로운 피드백 저장
            feedback = new Feedback();
        }

        // DTO에서 받은 값을 엔티티에 설정
        // 컨트롤러에서 주입된 email과 nickname 값을 사용합니다.
        feedback.setEmail(feedbackRequest.getEmail());
        feedback.setNickname(feedbackRequest.getNickname());
        feedback.setRating(feedbackRequest.getRating());
        feedback.setFeedbackText(feedbackRequest.getFeedbackText());
        feedback.setTimestamp(LocalDateTime.now());

        // DTO의 Integer(0 또는 1) 값을 Entity의 Boolean으로 변환합니다.
        feedback.setIsHelpful(feedbackRequest.getIsHelpful() != null && feedbackRequest.getIsHelpful() == 1);

        // 변경된 피드백을 저장하고, 저장된 객체를 반환
        return feedbackRepository.save(feedback);
    }
}
