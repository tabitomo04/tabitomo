package vio.tabitomo.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(nullable = false)
    private int day; // 1일차, 2일차...

    private String startTime;

    private String endTime;

    @Lob
    private String memo;

    @Builder
    public Schedule(Trip trip, Place place, int day, String startTime, String endTime, String memo) {
        this.trip = trip;
        this.place = place;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.memo = memo;
    }
}
