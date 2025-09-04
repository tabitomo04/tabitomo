package com.koreatravel.tabitomo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.koreatravel.tabitomo.domain.entity.PlaceEntity;

public interface PlaceRepository extends JpaRepository<PlaceEntity, String> {

}