package com.koreatravel.tabitomo.repository.chat;

import com.koreatravel.tabitomo.domain.entity.chat.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {

    // JpaRepository already provides a save() method, so no new method is needed here.
    // You can simply call feedbackRepository.save(feedbackObject) in your service.
}