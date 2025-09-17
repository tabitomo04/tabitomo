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
import java.util.ArrayList;
import java.util.List;
import com.koreatravel.tabitomo.domain.entity.tag.TagMasterEntity;
import com.koreatravel.tabitomo.domain.entity.tag.StoryTagEntity;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "storybook")
@EntityListeners(AuditingEntityListener.class)
public class StorybookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer booknum;

    @Column(name = "title")
    private String title;

    @Column(name = "subtitle")
    private String subtitle;

    @Column(columnDefinition = "TEXT") // HTML 내용을 저장하기 위해 TEXT 타입으로 설정
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;


    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime createDate; // 생성 시간


    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime updateDate;

    @Column(name = "likes", columnDefinition = "integer default 0")
    private int likes;

    @OneToMany(mappedBy = "storybook", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StoryTagEntity> tags = new ArrayList<>();
    
    // Helper method to add a tag
    public void addTag(TagMasterEntity tag) {
        StoryTagEntity storyTag = new StoryTagEntity();
        storyTag.setStorybook(this);
        storyTag.setTag(tag);
        tags.add(storyTag);
    }
    
    // Helper method to remove a tag
    public void removeTag(TagMasterEntity tag) {
        tags.removeIf(storyTag -> storyTag.getTag().equals(tag));
    }
}
