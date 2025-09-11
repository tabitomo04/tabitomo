package com.koreatravel.tabitomo.repository.trip;

import com.koreatravel.tabitomo.domain.entity.trip.ScheduleEntity;
import com.koreatravel.tabitomo.domain.entity.trip.TripEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {
    /**
     * Find all schedules for a trip
     */
    List<ScheduleEntity> findByTrip(TripEntity trip);
    
    /**
     * Find all schedules for a trip with place details, ordered by day and start time
     */
    @Query("SELECT s FROM ScheduleEntity s JOIN FETCH s.place WHERE s.trip.id = :tripId ORDER BY s.day, s.startTime")
    List<ScheduleEntity> findByTripIdWithPlace(@Param("tripId") Long tripId);
    
    /**
     * Find all schedules for a trip ID
     */
    @Query("SELECT s FROM ScheduleEntity s WHERE s.trip.id = :tripId")
    List<ScheduleEntity> findByTripId(@Param("tripId") Long tripId);
    
    /**
     * Delete all schedules for a trip
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ScheduleEntity s WHERE s.trip = :trip")
    void deleteByTrip(@Param("trip") TripEntity trip);
}
