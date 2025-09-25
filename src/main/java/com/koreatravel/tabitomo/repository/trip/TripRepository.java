package com.koreatravel.tabitomo.repository.trip;


import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.koreatravel.tabitomo.domain.entity.trip.Trip;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    @Query("SELECT t FROM Trip t WHERE t.member.email = :email ORDER BY t.id DESC")
    List<Trip> findByMemberEmail(@Param("email") String email);

    @Query("SELECT t FROM Trip t WHERE t.member.email = :email ORDER BY t.id DESC")
    Page<Trip> findByMemberEmail(@Param("email") String email, Pageable pageable);

    @Query("SELECT t FROM Trip t JOIN FETCH t.member WHERE t.id = :tripId")
    Optional<Trip> findByIdWithMember(@Param("tripId") Long tripId);

    List<Trip> findAllByVisibility(String visibility);

    Page<Trip> findAllByVisibility(String visibility, Pageable pageable);

    Page<Trip> findAllByVisibilityAndTitleContainingIgnoreCase(String visibility, String title, Pageable pageable);

    Page<Trip> findAllByVisibilityAndMemberNicknameContainingIgnoreCase(String visibility, String nickname, Pageable pageable);

    List<Trip> findByMemberNicknameAndVisibility(String nickname, String visibility);

    Page<Trip> findByMemberNicknameAndVisibility(String nickname, String visibility, Pageable pageable);

    @Query("SELECT t FROM Trip t JOIN FETCH t.accommodation a WHERE t.member = :member ORDER BY t.createdAt DESC LIMIT 1")
    Optional<Trip> findTopByMemberWithAccommodation(@Param("member") MemberEntity member);
}
