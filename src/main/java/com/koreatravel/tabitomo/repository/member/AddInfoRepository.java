package com.koreatravel.tabitomo.repository.member;

import org.springframework.data.jpa.repository.JpaRepository;

import com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity;
import com.koreatravel.tabitomo.id.AddInfoId;

public interface AddInfoRepository extends JpaRepository<AddInfoEntity, AddInfoId> {
    
}
