package com.koreatravel.tabitomo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.koreatravel.tabitomo.domain.entity.ScheduleEntity;
import com.koreatravel.tabitomo.domain.entity.TripEntity;
import com.koreatravel.tabitomo.repository.ScheduleRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;

    public ScheduleEntity saveSchedule(ScheduleEntity schedule) {
        return scheduleRepository.save(schedule);
    }

    public List<ScheduleEntity> getSchedulesByTrip(TripEntity trip) {
        return scheduleRepository.findByTrip(trip);
    }
}
