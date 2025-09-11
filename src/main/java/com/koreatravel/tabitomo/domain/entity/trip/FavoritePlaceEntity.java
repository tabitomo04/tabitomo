package com.koreatravel.tabitomo.domain.entity.trip;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
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

    // 멤버 ID (member 테이블과의 조인을 위해 사용)
    @Column(name = "member_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID memberId;
    
    // 이메일 필드 (조회용)
    @Column(name = "email", insertable = false, updatable = false, length = 255)
    private String email;

    // 장소 ID (place 테이블과의 관계를 나타냄)
    @Column(name = "place_id", nullable = false, length = 50, insertable = true, updatable = false)
    private String placeId;

    // 회원 엔티티와의 연관 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "id", insertable = false, updatable = false)
    private MemberEntity member;

    // 생성 일시
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        // Set email from member if available
        if (this.member != null && this.email == null) {
            this.email = this.member.getEmail();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Ensure email is in sync with member if member is loaded
        if (this.member != null && this.email == null) {
            this.email = this.member.getEmail();
        }
    }
}
