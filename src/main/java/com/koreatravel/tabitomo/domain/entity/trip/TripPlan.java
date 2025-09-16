package com.koreatravel.tabitomo.domain.entity.trip;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TripPlan {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_plan_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(nullable = false)
    private String planName;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String planDetails;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public TripPlan(MemberEntity member, String planName, String planDetails) {
        this.member = member;
        this.planName = planName;
        this.planDetails = planDetails;
    }
}
