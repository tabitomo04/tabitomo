package com.koreatravel.tabitomo.repository.member;

import com.koreatravel.tabitomo.domain.entity.member.MemberAddInfoEntity;
import com.koreatravel.tabitomo.id.MemberAddInfoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.koreatravel.tabitomo.domain.entity.member.AddInfoEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberAddInfoRepository extends JpaRepository<MemberAddInfoEntity, MemberAddInfoId> {
    
    /**
     * Find all member add info by member ID
     */
    @Query("SELECT m FROM MemberAddInfoEntity m WHERE m.id.memberId = :memberId")
    List<MemberAddInfoEntity> findByMemberId(@Param("memberId") UUID memberId);
    
    /**
     * Delete all member add info by member ID
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM MemberAddInfoEntity m WHERE m.id.memberId = :memberId")
    void deleteByMemberId(@Param("memberId") UUID memberId);
    
    /**
     * Find all member add info by member ID and info high number
     */
    @Query("""
        SELECT m FROM MemberAddInfoEntity m 
        WHERE m.id.memberId = :memberId 
        AND m.id.infoHighNum = :infoHighNum
    """)
    List<MemberAddInfoEntity> findByMemberIdAndInfoHighNum(
        @Param("memberId") UUID memberId, 
        @Param("infoHighNum") Integer infoHighNum
    );
    
    /**
     * Find all member add info by member ID, info high number, and info low number
     * 
     * @param memberId The ID of the member
     * @param infoHighNum The high number of the information to search for
     * @param infoLowNum The low number of the information to search for
     * @return A list of matching MemberAddInfoEntity objects
     */
    @Query("""
        SELECT m FROM MemberAddInfoEntity m 
        WHERE m.id.memberId = :memberId 
        AND m.id.infoHighNum = :infoHighNum 
        AND m.id.infoLowNum = :infoLowNum
    """)
    List<MemberAddInfoEntity> findByMemberIdAndInfoHighNumAndInfoLowNum(
        @Param("memberId") UUID memberId,
        @Param("infoHighNum") Integer infoHighNum,
        @Param("infoLowNum") Integer infoLowNum
    );
    
    /**
     * Find all member add info by member ID and info high number
     */
    default List<MemberAddInfoEntity> findByMemberIdAndAddInfoInfoHighNum(UUID memberId, Integer infoHighNum) {
        return findByMemberIdAndInfoHighNum(memberId, infoHighNum);
    }
    
    /**
     * Find all member add info by member ID, info high number, and info low number
     */
    default List<MemberAddInfoEntity> findByMemberIdAndAddInfoInfoHighNumAndAddInfoInfoLowNum(
        UUID memberId, Integer infoHighNum, Integer infoLowNum) {
        return findByMemberIdAndInfoHighNumAndInfoLowNum(memberId, infoHighNum, infoLowNum);
    }
}
