package com.koreatravel.tabitomo.entity;

// LearningData.java

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "learning_data")
public class LearningData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dataId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String prompt;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String expectedResponse;

    @Column(name = "feedback_id")
    private Integer sourceFeedbackId;

    private int importance;

    private LocalDateTime createdAt;

    // Getter and Setter
    // ...
}