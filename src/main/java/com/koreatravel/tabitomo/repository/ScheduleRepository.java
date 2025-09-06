package com.koreatravel.tabitomo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.koreatravel.tabitomo.domain.entity.ScheduleEntity;
import com.koreatravel.tabitomo.domain.entity.TripEntity;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {
    List<ScheduleEntity> findByTrip(TripEntity trip);
}
