package com.koreatravel.tabitomo.domain.entity.storybook;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "likedbook")
@EntityListeners(AuditingEntityListener.class)
public class LikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer likeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booknum", referencedColumnName = "book_num", insertable = false, updatable = false)
    private StorybookEntity storybook;
    
    @Column(name = "booknum", nullable = false)
    private Integer booknum;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime createDate;
}
