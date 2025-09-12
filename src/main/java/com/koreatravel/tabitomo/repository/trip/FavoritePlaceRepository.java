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
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FavoritePlaceEntity f WHERE f.member.id = :memberId AND f.placeId = :placeId")
    boolean existsByMemberIdAndPlaceId(@Param("memberId") UUID memberId, @Param("placeId") String placeId);
    
    @Query("SELECT f FROM FavoritePlaceEntity f WHERE f.member.id = :memberId")
    Page<FavoritePlaceEntity> findByMemberId(@Param("memberId") UUID memberId, Pageable pageable);

    @Query("SELECT f FROM FavoritePlaceEntity f WHERE f.member.id = :memberId AND f.placeId = :placeId")
    Optional<FavoritePlaceEntity> findByMemberIdAndPlaceId(
        @Param("memberId") UUID memberId, 
        @Param("placeId") String placeId
    );

    @Modifying
    @Query("DELETE FROM FavoritePlaceEntity f WHERE f.member.id = :memberId AND f.placeId = :placeId")
    int deleteByMemberIdAndPlaceId(
        @Param("memberId") UUID memberId, 
        @Param("placeId") String placeId
    );
    
    // For compatibility with existing code
    default void removeByMemberIdAndPlaceId(UUID memberId, String placeId) {
        deleteByMemberIdAndPlaceId(memberId, placeId);
    }
}