package com.koreantravel.tabitomo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.koreantravel.tabitomo.domain.entity.TripPlan;

import java.util.List;
import java.util.Optional;

public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {

    @Query("SELECT tp FROM TripPlan tp WHERE tp.member.email = :email")
    List<TripPlan> findByMemberEmail(@Param("email") String email);

    @Query("SELECT tp FROM TripPlan tp JOIN FETCH tp.member WHERE tp.id = :planId")
    Optional<TripPlan> findByIdWithMember(@Param("planId") Long planId);
}
