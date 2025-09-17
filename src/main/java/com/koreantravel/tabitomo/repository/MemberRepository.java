package com.koreantravel.tabitomo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.koreantravel.tabitomo.domain.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {
    Optional<Member> findByNickname(String nickname);
}
