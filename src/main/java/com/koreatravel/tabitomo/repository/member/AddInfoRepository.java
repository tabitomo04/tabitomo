package com.koreatravel.tabitomo.repository.member;

import com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity;
import com.koreatravel.tabitomo.domain.entity.member.AddInfoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddInfoRepository extends JpaRepository<AddInfoEntity, AddInfoId> {
    List<AddInfoEntity> findByInfoHighNum(int infoHighNum);
}
