package com.koreatravel.tabitomo.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Feedback")
@Getter
@Setter
public class FeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Integer feedbackId;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1-5점

    @Column(name = "feedback_text", columnDefinition = "TEXT")
    private String feedbackText;

    @Column(name = "chat_history", columnDefinition = "TEXT")
    private String chatHistory;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}
