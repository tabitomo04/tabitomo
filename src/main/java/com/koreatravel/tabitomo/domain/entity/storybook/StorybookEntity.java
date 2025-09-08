package com.koreatravel.tabitomo.domain.entity.storybook;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Getter
@Setter
@Table(name = "storybook")
@ToString
public class StorybookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_num", nullable = false)
    private Integer bookNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email", referencedColumnName = "email", nullable = false, insertable = false, updatable = false)
    private MemberEntity member;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Builder.Default
    @Column(name = "likes", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer likes = 0;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "subtitle", nullable = false, length = 100)
    private String subtitle;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Builder pattern implementation
    public static StorybookEntityBuilder builder() {
        return new StorybookEntityBuilder();
    }
}
