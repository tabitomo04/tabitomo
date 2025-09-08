package com.koreatravel.tabitomo.domain.entity.chat;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_qa")
public class ChatQAEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qa_id", columnDefinition = "INT NOT NULL AUTO_INCREMENT")
    private Integer qaId;

    @Column(name = "category_id", nullable = false, columnDefinition = "INT")
    private Integer categoryId;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;
    
    // Builder pattern implementation
    public static ChatQAEntityBuilder builder() {
        return new ChatQAEntityBuilder();
    }
}
