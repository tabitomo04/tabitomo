package com.koreatravel.tabitomo.repository.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.trip.TripEntity;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<TripEntity, Long> {
    List<TripEntity> findByMember(MemberEntity member);
    
    List<TripEntity> findByMemberEmail(String email);
    
    @Query("SELECT t FROM TripEntity t WHERE t.member.id = :memberId ORDER BY t.id DESC")
    List<TripEntity> findByMemberId(@Param("memberId") Long memberId);
    
    @Query("SELECT t FROM TripEntity t JOIN FETCH t.member WHERE t.id = :tripId")
    Optional<TripEntity> findByIdWithMember(@Param("tripId") Long tripId);
}
