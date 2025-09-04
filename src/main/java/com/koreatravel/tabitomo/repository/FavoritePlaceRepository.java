package com.koreatravel.tabitomo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.koreatravel.tabitomo.domain.entity.FavoritePlaceEntity;

public interface FavoritePlaceRepository extends JpaRepository<FavoritePlaceEntity, Integer> {
    boolean existsByEmailAndPlaceId(String email, String placeId);
}
