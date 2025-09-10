package com.koreatravel.tabitomo.domain.entity.trip;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;

/**
 * 즐겨찾기 장소 엔티티
 * 데이터베이스의 favorite_place 테이블과 매핑됩니다.
 */
@Entity
@Table(name = "favorite_place")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoritePlaceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    // 이메일 필드 (member_email과 동기화됨)
    @Column(name = "email", length = 255)
    private String email;

    // 멤버 이메일 (member 테이블과의 조인을 위해 사용)
    @Column(name = "member_email", nullable = false, length = 100, insertable = true, updatable = false)
    private String memberEmail;

    // 장소 ID (place 테이블과의 관계를 나타냄)
    @Column(name = "place_id", nullable = false, length = 50, insertable = true, updatable = false)
    private String placeId;

    // 회원 엔티티와의 연관 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_email", referencedColumnName = "email", insertable = false, updatable = false)
    private MemberEntity member;

    // 생성 일시
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        // email과 memberEmail을 동기화
        if (this.memberEmail != null && this.email == null) {
            this.email = this.memberEmail;
        } else if (this.email != null && this.memberEmail == null) {
            this.memberEmail = this.email;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // email과 memberEmail을 동기화
        if (this.memberEmail != null && this.email == null) {
            this.email = this.memberEmail;
        } else if (this.email != null && this.memberEmail == null) {
            this.memberEmail = this.email;
        }
    }
}
