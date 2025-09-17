package com.koreatravel.tabitomo.domain.entity.storybook;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.member.MemberId;
import com.koreatravel.tabitomo.domain.entity.storybook.StorybookEntity;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "likedbook")
@EntityListeners(AuditingEntityListener.class)
public class LikedbookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer likeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booknum", nullable = false)
    private StorybookEntity storybook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(name = "created_at",columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime createDate;

    public void setMember(MemberId id) {
        MemberEntity member = new MemberEntity();
        member.setId(id);
        this.member = member;
    }
    
    // 편의를 위한 메서드 추가
    public void setMemberId(Long id) {
        this.setMember(new MemberId(id));
    }
}
