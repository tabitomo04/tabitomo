package com.koreantravel.tabitomo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.koreantravel.tabitomo.domain.entity.Schedule;
import com.koreantravel.tabitomo.domain.entity.Trip;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("SELECT s FROM Schedule s JOIN FETCH s.place WHERE s.trip.id = :tripId ORDER BY s.day, s.startTime")
    List<Schedule> findByTripIdWithPlace(@Param("tripId") Long tripId);

    void deleteByTrip(Trip trip);
}
