package com.koreatravel.tabitomo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.koreatravel.tabitomo.domain.entity.FavoritePlaceEntity;

public interface FavoritePlaceRepository extends JpaRepository<FavoritePlaceEntity, Integer> {
    boolean existsByEmailAndPlaceId(String email, String placeId);
    Page<FavoritePlaceEntity> findByEmail(String email, Pageable pageable);

    default void deleteByEmailAndPlaceId(String email, String placeId) {
        deleteByEmailAndPlaceId(email, placeId);
    }
    
    
}
