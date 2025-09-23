package com.koreatravel.tabitomo.domain.entity.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Getter
@Setter
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Integer feedbackId;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Column(name = "nickname", length = 30)
    private String nickname;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "feedback_text", columnDefinition = "TEXT")
    private String feedbackText;

    @Column(name = "is_helpful", nullable = false)
    private Boolean isHelpful;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}
