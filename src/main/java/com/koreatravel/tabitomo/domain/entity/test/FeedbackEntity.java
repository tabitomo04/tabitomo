package com.koreatravel.tabitomo.domain.entity.test;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "feedback")
public class FeedbackEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;
    
    @Column(nullable = false)
    private Integer rating;
    
    @Column(name = "feedback_text", columnDefinition = "TEXT")
    private String feedbackText;
    
    @Column(name = "chat_history", columnDefinition = "TEXT")
    private String chatHistory;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private MemberEntity user;
    
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    @Column(name = "model_version", length = 50)
    private String modelVersion;
    
    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LearningDataEntity> learningData = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
    
    public void addLearningData(LearningDataEntity data) {
        learningData.add(data);
        data.setFeedback(this);
    }
    
    public void removeLearningData(LearningDataEntity data) {
        learningData.remove(data);
        data.setFeedback(null);
    }
}
