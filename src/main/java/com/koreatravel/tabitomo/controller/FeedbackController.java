package com.koreatravel.tabitomo.controller;

import com.koreatravel.tabitomo.dto.FeedbackRequest;
import com.koreatravel.tabitomo.service.FeedbackService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class FeedbackController {

    private final FeedbackService feedbackService; // ⬅️ FeedbackRepository 대신 FeedbackService 주입

    // 생성자 주입
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/feedback")
    public ResponseEntity<String> receiveFeedback(@RequestBody FeedbackRequest feedbackRequest) {
        try {
            feedbackService.saveFeedback(feedbackRequest); // ⬅️ Service 메서드 호출
            return ResponseEntity.ok().body("피드백이 성공적으로 저장되었습니다.");
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("채팅 기록을 JSON으로 변환하는 데 실패했습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("피드백 저장 중 오류가 발생했습니다.");
        }
    }
}