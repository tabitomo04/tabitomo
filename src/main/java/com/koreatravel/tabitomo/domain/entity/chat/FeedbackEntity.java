package com.koreatravel.tabitomo.domain.entity.chat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Getter
@Setter
public class FeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id", nullable = false)
    private Integer feedbackId;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1-5점

    @Lob
    @Column(name = "feedback_text")
    private String feedbackText;

    @Lob
    @Column(name = "chat_history")
    private String chatHistory;

    @Column(name = "timestamp", nullable = false, updatable = false, insertable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}
