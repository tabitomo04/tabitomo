package com.koreatravel.tabitomo.repository.trip;

import org.springframework.data.jpa.repository.JpaRepository;

import com.koreatravel.tabitomo.domain.entity.trip.FavoritePlaceEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoritePlaceRepository extends JpaRepository<FavoritePlaceEntity, Integer> {
    boolean existsByEmailAndPlaceId(String email, Long placeId);
    Page<FavoritePlaceEntity> findByEmail(String email, Pageable pageable);

    @Modifying
    @Query("DELETE FROM FavoritePlaceEntity f WHERE f.email = :email AND f.placeId = :placeId")
    void deleteByEmailAndPlaceId(@Param("email") String email, @Param("placeId") Long placeId);
    
    
}
