package com.koreatravel.tabitomo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.koreatravel.tabitomo.domain.entity.UserSelectedInfoEntity;
import com.koreatravel.tabitomo.id.UserSelectedInfoId;

public interface UserSelectedInfoRepository extends JpaRepository<UserSelectedInfoEntity, UserSelectedInfoId> {
    
}
