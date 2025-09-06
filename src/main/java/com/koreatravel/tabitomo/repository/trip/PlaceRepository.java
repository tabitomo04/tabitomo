package com.koreatravel.tabitomo.repository.trip;

import org.springframework.data.jpa.repository.JpaRepository;

import com.koreatravel.tabitomo.domain.entity.trip.PlaceEntity;

public interface PlaceRepository extends JpaRepository<PlaceEntity, Long> {

}