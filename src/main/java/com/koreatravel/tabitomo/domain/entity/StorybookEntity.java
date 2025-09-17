package com.koreatravel.tabitomo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Builder
@Table(name = "storybook")
@EntityListeners(AuditingEntityListener.class)
public class StorybookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer booknum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email", referencedColumnName = "email")
    private MemberEntity member;

    @Column(name = "title")
    private String title;

    @Column(name = "subtitle")
    private String subtitle;

    @Column(columnDefinition = "TEXT") // HTML 내용을 저장하기 위해 TEXT 타입으로 설정
    private String content;


    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime createDate; // 생성 시간


    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime updateDate;

    @Column(name = "likes", columnDefinition = "integer default 0")
    private int likes;

    @OneToMany(mappedBy = "storybook", cascade = CascadeType.ALL)
    private List<StorytagEntity> tags;
}
