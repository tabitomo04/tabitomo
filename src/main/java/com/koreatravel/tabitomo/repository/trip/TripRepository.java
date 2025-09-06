package com.koreatravel.tabitomo.repository.trip;

import org.springframework.data.jpa.repository.JpaRepository;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.trip.TripEntity;

import java.util.List;

public interface TripRepository extends JpaRepository<TripEntity, Long> {
    List<TripEntity> findByMember(MemberEntity member);
    List<TripEntity> findByMemberEmail(String email);
}
