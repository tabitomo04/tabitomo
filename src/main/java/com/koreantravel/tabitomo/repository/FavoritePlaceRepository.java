package com.koreantravel.tabitomo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.koreantravel.tabitomo.domain.entity.FavoritePlace;

import java.util.List;
import java.util.Optional;

public interface FavoritePlaceRepository extends JpaRepository<FavoritePlace, Long> {

    Optional<FavoritePlace> findByMemberEmailAndPlaceId(String email, Long placeId);

    List<FavoritePlace> findByMemberEmail(String email);

    Page<FavoritePlace> findByMemberEmail(String email, Pageable pageable);

    boolean existsByMemberEmailAndPlaceId(String email, Long placeId);
}
