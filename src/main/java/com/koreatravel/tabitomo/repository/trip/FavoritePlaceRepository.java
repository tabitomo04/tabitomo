package com.koreatravel.tabitomo.repository.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import com.koreatravel.tabitomo.domain.entity.trip.FavoritePlaceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoritePlaceRepository extends JpaRepository<FavoritePlaceEntity, Integer> {
    boolean existsByMemberEmailAndPlaceId(String memberEmail, String placeId);
    
    Page<FavoritePlaceEntity> findByMemberEmail(String memberEmail, Pageable pageable);

    @Modifying
    @Query("DELETE FROM FavoritePlaceEntity f WHERE f.memberEmail = :memberEmail AND f.placeId = :placeId")
    void deleteByMemberEmailAndPlaceId(
        @Param("memberEmail") String memberEmail, 
        @Param("placeId") String placeId
    );
}