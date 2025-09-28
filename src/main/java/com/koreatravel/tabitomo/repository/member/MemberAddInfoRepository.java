package com.koreatravel.tabitomo.repository.member;

import com.koreatravel.tabitomo.domain.entity.member.MemberAddInfoEntity;
import com.koreatravel.tabitomo.domain.entity.member.MemberAddInfoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface MemberAddInfoRepository extends JpaRepository<MemberAddInfoEntity, MemberAddInfoId> {
    List<MemberAddInfoEntity> findByMemberId(UUID memberId);
    boolean existsByMemberId(UUID memberId);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM MemberAddInfoEntity m WHERE m.memberId = :memberId AND m.id.infoHighNum = :infoHighNum")
    void deleteByMemberIdAndInfoHighNum(@Param("memberId") UUID memberId, @Param("infoHighNum") int infoHighNum);
    
    @Query("SELECT COUNT(m) > 0 FROM MemberAddInfoEntity m WHERE m.memberId = :memberId AND m.id.infoHighNum = :infoHighNum AND m.id.infoLowNum = :infoLowNum")
    boolean existsByMemberIdAndInfoHighNumAndInfoLowNum(
            @Param("memberId") UUID memberId, 
            @Param("infoHighNum") int infoHighNum, 
            @Param("infoLowNum") int infoLowNum
    );
    
    @Query("SELECT COUNT(m) FROM MemberAddInfoEntity m WHERE m.memberId = :memberId")
    long countByMemberId(@Param("memberId") UUID memberId);
}
