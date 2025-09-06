package com.koreatravel.tabitomo.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "schedule")
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private PlaceEntity place;

    @Column(name = "day_no")
    private Integer dayNo;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(length = 255)
    private String memo;

    @Builder
    public ScheduleEntity(TripEntity trip, PlaceEntity place, Integer dayNo, LocalTime startTime, LocalTime endTime, String memo) {
        this.trip = trip;
        this.place = place;
        this.dayNo = dayNo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.memo = memo;
    }

    public void setTrip(TripEntity trip) {
        this.trip = trip;
    }
}
