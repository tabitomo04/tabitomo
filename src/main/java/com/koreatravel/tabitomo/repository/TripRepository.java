package com.koreatravel.tabitomo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.koreatravel.tabitomo.domain.entity.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.TripEntity;

import java.util.List;

public interface TripRepository extends JpaRepository<TripEntity, Long> {
    List<TripEntity> findByMember(MemberEntity member);
    List<TripEntity> findByMemberEmail(String email);
}
