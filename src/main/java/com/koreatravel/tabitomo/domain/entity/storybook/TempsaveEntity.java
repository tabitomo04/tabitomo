package com.koreatravel.tabitomo.domain.entity.storybook;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Builder
@Table(name = "temp_story")
@EntityListeners(AuditingEntityListener.class)
public class TempsaveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "temp_id", columnDefinition = "INT NOT NULL AUTO_INCREMENT")
    private Integer tempId;

    @Column(name = "title")
    private String title;

    @Column(name = "subtitle")
    private String subtitle;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

}
