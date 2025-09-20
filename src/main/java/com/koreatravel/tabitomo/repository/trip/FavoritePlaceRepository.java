package com.koreatravel.tabitomo.repository.trip;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.trip.FavoritePlace;
import com.koreatravel.tabitomo.domain.entity.trip.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoritePlaceRepository extends JpaRepository<FavoritePlace, Long> {

    boolean existsByMemberAndPlace(MemberEntity member, Place place);

    Optional<FavoritePlace> findByMemberAndPlace_Id(MemberEntity member, Long placeId);

    List<FavoritePlace> findByMember(MemberEntity member);

    Page<FavoritePlace> findByMember(MemberEntity member, Pageable pageable);

    boolean existsByMemberAndPlace_Id(MemberEntity member, Long placeId);

}
