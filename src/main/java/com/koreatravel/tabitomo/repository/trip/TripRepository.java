package com.koreatravel.tabitomo.repository.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.koreatravel.tabitomo.domain.entity.trip.Trip;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    @Query("SELECT t FROM Trip t WHERE t.member.id = :memberId ORDER BY t.id DESC")
    List<Trip> findByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT t FROM Trip t JOIN FETCH t.member WHERE t.id = :tripId")
    Optional<Trip> findByIdWithMember(@Param("tripId") Long tripId);
}
