package com.koreatravel.tabitomo.repository.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.koreatravel.tabitomo.domain.entity.trip.TripPlan;

import java.util.List;
import java.util.Optional;

public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {

    @Query("SELECT tp FROM TripPlan tp WHERE tp.member.id = :memberId")
    List<TripPlan> findByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT tp FROM TripPlan tp JOIN FETCH tp.member WHERE tp.id = :planId")
    Optional<TripPlan> findByIdWithMember(@Param("planId") Long planId);
}
