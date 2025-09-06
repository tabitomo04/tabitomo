package com.koreatravel.tabitomo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.koreatravel.tabitomo.domain.entity.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.TripEntity;
import com.koreatravel.tabitomo.repository.TripRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {
    private final TripRepository tripRepository;

    public TripEntity saveTrip(TripEntity trip) {
        return tripRepository.save(trip);
    }

    public List<TripEntity> getTripsByMember(MemberEntity member) {
        return tripRepository.findByMember(member);
    }

    public List<TripEntity> getTripsByMemberEmail(String email) {
        return tripRepository.findByMemberEmail(email);
    }

    public TripEntity getTrip(Long tripId) {
        return tripRepository.findById(tripId).orElse(null);
    }
}
