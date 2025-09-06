package com.koreatravel.tabitomo.service.trip;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.trip.TripEntity;
import com.koreatravel.tabitomo.repository.trip.TripRepository;

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
