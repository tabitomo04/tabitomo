package com.koreatravel.tabitomo.repository;

import com.koreatravel.tabitomo.domain.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByEmail(String Email);
    /**
     * 닉네임이 존재하는지 확인하는 메서드
     * @param nickname 확인할 닉네임
     * @return 닉네임이 존재하면 true, 아니면 false
     */
    boolean existsByNickname(String nickname);
}

