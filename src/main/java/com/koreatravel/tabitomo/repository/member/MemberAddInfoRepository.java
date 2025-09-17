package com.koreatravel.tabitomo.repository.member;

import com.koreatravel.tabitomo.domain.entity.member.MemberAddInfoEntity;
import com.koreatravel.tabitomo.domain.entity.member.MemberAddInfoId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MemberAddInfoRepository extends JpaRepository<MemberAddInfoEntity, MemberAddInfoId> {
    List<MemberAddInfoEntity> findByMemberId(UUID memberId);
    boolean existsByMemberId(UUID memberId);
}
