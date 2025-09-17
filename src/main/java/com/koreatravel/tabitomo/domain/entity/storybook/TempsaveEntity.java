package com.koreatravel.tabitomo.domain.entity.storybook;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "temp_story")
@EntityListeners(AuditingEntityListener.class)
public class TempsaveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tempId;

    @Column(name = "title")
    private String title;

    @Column(name = "subtitle")
    private String subtitle;

    @Column(columnDefinition = "TEXT") // HTML 내용을 저장하기 위해 TEXT 타입으로 설정
    private String content;

    @Column(name = "tags", length = 255)
    private String tags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime createDate; // 생성 시간

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime updateDate;

}
