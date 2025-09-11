package com.koreatravel.tabitomo.repository.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import com.koreatravel.tabitomo.domain.entity.trip.FavoritePlaceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface FavoritePlaceRepository extends JpaRepository<FavoritePlaceEntity, Integer> {
    boolean existsByMemberIdAndPlaceId(UUID memberId, String placeId);
    
    Page<FavoritePlaceEntity> findByMemberId(UUID memberId, Pageable pageable);

    Optional<FavoritePlaceEntity> findByMemberIdAndPlaceId(UUID memberId, String placeId);

    @Modifying
    @Query("DELETE FROM FavoritePlaceEntity f WHERE f.memberId = :memberId AND f.placeId = :placeId")
    void deleteByMemberIdAndPlaceId(
        @Param("memberId") UUID memberId, 
        @Param("placeId") String placeId
    );
}