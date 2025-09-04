package com.koreatravel.tabitomo.repository;

import com.koreatravel.tabitomo.domain.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, String> {
    Optional<MemberEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
