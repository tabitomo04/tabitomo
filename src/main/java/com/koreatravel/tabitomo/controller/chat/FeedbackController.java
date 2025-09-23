package com.koreatravel.tabitomo.controller.chat;

import com.koreatravel.tabitomo.config.security.UserDetailsImpl;
import com.koreatravel.tabitomo.domain.dto.chat.FeedbackRequest;
import com.koreatravel.tabitomo.domain.entity.chat.Feedback;
import com.koreatravel.tabitomo.service.chat.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<?> receiveFeedback(@RequestBody FeedbackRequest feedbackRequest, @RequestParam(required = false) Long feedbackId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            // 로그인한 사용자의 이메일과 닉네임을 FeedbackRequest에 추가
            if (userDetails != null) {
                // UserDetailsImpl 객체에서 직접 이메일과 닉네임을 가져와 설정합니다.
                feedbackRequest.setEmail(userDetails.getEmail());
                feedbackRequest.setNickname(userDetails.getNickname());
            }

            // Service 메서드를 호출하여 피드백 저장 또는 업데이트
            Feedback savedFeedback = feedbackService.saveOrUpdateFeedback(feedbackRequest, feedbackId);
            // 저장된 피드백 객체를 반환하여 클라이언트가 ID를 관리할 수 있도록 함
            return ResponseEntity.ok().body(savedFeedback);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("피드백 저장/수정 중 오류가 발생했습니다.");
        }
    }
}
