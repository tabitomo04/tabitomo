package com.koreantravel.tabitomo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.koreantravel.tabitomo.domain.entity.Place;

import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    Optional<Place> findFirstByName(String name);

    @Query("SELECT p FROM Place p WHERE " +
           "(:region IS NULL OR :region = 'all' OR p.region = :region) AND " +
           "(:categoryCode IS NULL OR :categoryCode = 'all' OR p.categoryCode = :categoryCode) AND " +
           "(:keyword IS NULL OR :keyword = '' OR p.name LIKE %:keyword% OR p.address LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Place> findByFilters(@Param("region") String region,
                              @Param("categoryCode") String categoryCode,
                              @Param("keyword") String keyword,
                              Pageable pageable);
}
