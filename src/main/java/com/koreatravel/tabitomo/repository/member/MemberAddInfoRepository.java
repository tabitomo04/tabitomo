package com.koreatravel.tabitomo.repository.member;

import com.koreatravel.tabitomo.domain.entity.member.MemberAddInfoEntity;
import com.koreatravel.tabitomo.domain.entity.member.MemberAddInfoId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberAddInfoRepository extends JpaRepository<MemberAddInfoEntity, MemberAddInfoId> {
    List<MemberAddInfoEntity> findByMemberId(Long memberId);
    boolean existsByMemberId(Long memberId);
}
