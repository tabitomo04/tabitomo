package com.koreatravel.tabitomo.repository.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import com.koreatravel.tabitomo.domain.entity.trip.PlaceEntity;

import java.util.Optional;

public interface PlaceRepository extends JpaRepository<PlaceEntity, String> {
    Optional<PlaceEntity> findByName(String name);
}