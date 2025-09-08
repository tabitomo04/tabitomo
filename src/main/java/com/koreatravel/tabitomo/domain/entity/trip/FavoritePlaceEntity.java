package com.koreatravel.tabitomo.domain.entity.trip;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;

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

    @Column(name = "member_email", nullable = false, length = 100)
    private String memberEmail;

    @Column(name = "place_id", nullable = false, length = 50)
    private String placeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_email", referencedColumnName = "email", insertable = false, updatable = false)
    private MemberEntity member;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
