package vio.tabitomo.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trip {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id")
    private Place accommodation;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Schedule> schedules = new ArrayList<>();

    @Column(nullable = false)
    private String title;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(length = 20)
    private String visibility; // e.g., "PUBLIC", "PRIVATE"

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    public Trip(Member member, Place accommodation, String title, LocalDate startDate, LocalDate endDate, String visibility) {
        this.member = member;
        this.accommodation = accommodation;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.visibility = visibility;
    }

    //== Business Methods ==//
    /**
     * 여행 제목을 수정합니다.
     */
    public void updateTitle(String title) {
        this.title = title;
    }
}
