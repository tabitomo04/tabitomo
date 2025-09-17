package com.koreatravel.tabitomo.repository.member;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<MemberEntity, UUID> {
    Optional<MemberEntity> findByEmail(String email);
    
    /**
     * 이메일로 회원 존재 여부 확인
     * @param email 확인할 이메일
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByEmail(String email);
    
    /**
     * 닉네임으로 회원 존재 여부 확인
     * @param nickname 확인할 닉네임
     * @return 존재 여부
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MemberEntity m WHERE m.nickname = :nickname")
    boolean existsByNickname(@Param("nickname") String nickname);
}
