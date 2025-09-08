package com.koreatravel.tabitomo.domain.entity.trip;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "schedule")
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id", columnDefinition = "BIGINT NOT NULL AUTO_INCREMENT")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private PlaceEntity place;

    @Column(name = "day_no", columnDefinition = "INT")
    private Integer dayNo;

    @Column(name = "start_time", columnDefinition = "TIME(6)")
    private LocalTime startTime;

    @Column(name = "end_time", columnDefinition = "TIME(6)")
    private LocalTime endTime;

    @Column(name = "memo", columnDefinition = "VARCHAR(255)")
    private String memo;

    @Builder
    private ScheduleEntity(TripEntity trip, PlaceEntity place, Integer dayNo, LocalTime startTime, LocalTime endTime, String memo) {
        this.trip = trip;
        this.place = place;
        this.dayNo = dayNo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.memo = memo != null ? memo : "";
    }
    
    public void updateMemo(String memo) {
        this.memo = memo != null ? memo : "";
    }

    public void setTrip(TripEntity trip) {
        this.trip = trip;
    }
}
