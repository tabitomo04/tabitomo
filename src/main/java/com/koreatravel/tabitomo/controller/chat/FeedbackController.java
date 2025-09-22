package com.koreatravel.tabitomo.controller.chat;

import com.koreatravel.tabitomo.domain.dto.chat.FeedbackRequest;
import com.koreatravel.tabitomo.domain.entity.chat.Feedback;
import com.koreatravel.tabitomo.service.chat.FeedbackService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class FeedbackController {

    private final FeedbackService feedbackService;

    // 생성자 주입
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/feedback")
    public ResponseEntity<?> receiveFeedback(@RequestBody FeedbackRequest feedbackRequest, @RequestParam(required = false) Long feedbackId) {
        try {
            // Service 메서드를 호출하여 피드백 저장 또는 업데이트
            Feedback savedFeedback = feedbackService.saveOrUpdateFeedback(feedbackRequest, feedbackId);
            // 저장된 피드백 객체를 반환하여 클라이언트가 ID를 관리할 수 있도록 함
            return ResponseEntity.ok().body(savedFeedback);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("채팅 기록을 JSON으로 변환하는 데 실패했습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("피드백 저장/수정 중 오류가 발생했습니다.");
        }
    }
}