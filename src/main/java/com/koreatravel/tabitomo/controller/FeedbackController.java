package com.koreatravel.tabitomo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koreatravel.tabitomo.repository.chat.FeedbackRepository;
import com.koreatravel.tabitomo.domain.dto.chat.FeedbackRequestDTO;
import com.koreatravel.tabitomo.domain.entity.chat.FeedbackEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/feedback")
    public ResponseEntity<?> receiveFeedback(@RequestBody FeedbackRequestDTO feedbackRequest) {
        // 기존의 lastFeedbackId 대신 전체 채팅 기록을 받아서 저장합니다.
        // 클라이언트에서 feedbackSubmitted 플래그를 통해 중복 제출을 막으므로, 서버에서는 단순 저장 로직만 구현합니다.

        FeedbackEntity feedback = new FeedbackEntity();
        feedback.setRating(feedbackRequest.getRating());
        feedback.setFeedbackText(feedbackRequest.getFeedbackText());

        try {
            // chatHistory(List)를 JSON 문자열로 변환하여 저장
            String chatHistoryJson = objectMapper.writeValueAsString(feedbackRequest.getChatHistory());
            feedback.setChatHistory(chatHistoryJson);
            feedback.setTimestamp(LocalDateTime.now());

            feedbackRepository.save(feedback);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // DB 저장 실패 시 500 에러 반환
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("피드백 저장 실패");
        }
    }
}
