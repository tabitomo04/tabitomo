package com.koreatravel.tabitomo.repository.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.koreatravel.tabitomo.domain.entity.trip.ScheduleEntity;
import com.koreatravel.tabitomo.domain.entity.trip.TripEntity;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {
    List<ScheduleEntity> findByTrip(TripEntity trip);
    
    @Query("SELECT s FROM ScheduleEntity s JOIN FETCH s.place WHERE s.trip.id = :tripId ORDER BY s.day, s.startTime")
    List<ScheduleEntity> findByTripIdWithPlace(@Param("tripId") Long tripId);
}
