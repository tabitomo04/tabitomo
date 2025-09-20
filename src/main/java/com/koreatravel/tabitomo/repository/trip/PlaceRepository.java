package com.koreatravel.tabitomo.repository.trip;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.koreatravel.tabitomo.domain.entity.trip.Place;

import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    Optional<Place> findFirstByName(String name);

    @Query(value = "SELECT p.place_id, p.name, p.category_code, p.address, p.city, p.region, p.latitude, p.longitude, p.image_url, p.description, p.price_range FROM place p WHERE " +
           "(:region IS NULL OR :region = 'all' OR p.region = :region) AND " +
           "(:categoryCode IS NULL OR :categoryCode = 'all' OR p.category_code = :categoryCode) AND " +
           "(:keyword IS NULL OR :keyword = '' OR p.name LIKE CONCAT('%', :keyword, '%') OR p.address LIKE CONCAT('%', :keyword, '%') OR p.description LIKE CONCAT('%', :keyword, '%'))",
           countQuery = "SELECT count(*) FROM place p WHERE " +
           "(:region IS NULL OR :region = 'all' OR p.region = :region) AND " +
           "(:categoryCode IS NULL OR :categoryCode = 'all' OR p.category_code = :categoryCode) AND " +
           "(:keyword IS NULL OR :keyword = '' OR p.name LIKE CONCAT('%', :keyword, '%') OR p.address LIKE CONCAT('%', :keyword, '%') OR p.description LIKE CONCAT('%', :keyword, '%'))",
           nativeQuery = true)
    Page<Place> findByFilters(@Param("region") String region,
                              @Param("categoryCode") String categoryCode,
                              @Param("keyword") String keyword,
                              Pageable pageable);
}
