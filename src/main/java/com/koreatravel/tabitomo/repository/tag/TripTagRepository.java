package com.koreatravel.tabitomo.repository.tag;

import com.koreatravel.tabitomo.domain.entity.tag.TripTagEntity;
import com.koreatravel.tabitomo.domain.entity.tag.TripTagId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripTagRepository extends JpaRepository<TripTagEntity, TripTagId> {
    
}
