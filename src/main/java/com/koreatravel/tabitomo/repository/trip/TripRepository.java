package com.koreatravel.tabitomo.repository.trip;

import com.koreatravel.tabitomo.domain.entity.trip.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {

    @Query("SELECT t FROM Trip t WHERE t.member.id = :memberId ORDER BY t.id DESC")
    List<Trip> findByMemberId(@Param("memberId") UUID memberId);
    
    @Query("SELECT t FROM Trip t WHERE t.id = :tripId AND t.member.id = :memberId")
    Optional<Trip> findByIdAndMemberId(@Param("tripId") UUID tripId, @Param("memberId") UUID memberId);

    @Query("SELECT t FROM Trip t JOIN FETCH t.member WHERE t.id = :tripId")
    Optional<Trip> findByIdWithMember(@Param("tripId") UUID tripId);

}
