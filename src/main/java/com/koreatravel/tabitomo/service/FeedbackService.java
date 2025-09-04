package com.koreatravel.tabitomo.service;

import com.koreatravel.tabitomo.entity.Feedback;
import com.koreatravel.tabitomo.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    // 저장된 Feedback 엔티티를 반환하도록 수정
    public Feedback saveFeedback(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }

    // Feedback 엔티티의 ID 타입(Integer)과 일치하도록 수정
    public void updateFeedbackRatingAndText(Integer feedbackId, Integer rating, String feedbackText) {
        Optional<Feedback> optionalFeedback = feedbackRepository.findById(feedbackId);
        if (optionalFeedback.isPresent()) {
            Feedback feedback = optionalFeedback.get();
            feedback.setRating(rating);
            feedback.setFeedbackText(feedbackText);
            feedbackRepository.save(feedback);
        }
    }
}
