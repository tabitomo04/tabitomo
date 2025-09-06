package com.koreatravel.tabitomo.repository.member;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, String> {
    
    Optional<MemberEntity> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    boolean existsByNickname(String nickname);
    
    @Query("SELECT m FROM MemberEntity m WHERE m.email = :email AND m.isActive = true")
    Optional<MemberEntity> findActiveByEmail(@Param("email") String email);
    
    @Query("SELECT m FROM MemberEntity m WHERE m.nickname = :nickname")
    Optional<MemberEntity> findByNickname(@Param("nickname") String nickname);
    
    @Query("UPDATE MemberEntity m SET m.isActive = false WHERE m.email = :email")
    void deactivateByEmail(@Param("email") String email);
}
