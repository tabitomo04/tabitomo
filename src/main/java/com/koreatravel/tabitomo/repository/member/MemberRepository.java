package com.koreatravel.tabitomo.repository.member;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.member.MemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, MemberId> {
    Optional<MemberEntity> findByEmail(String email);
    
    @Override
    Optional<MemberEntity> findById(MemberId id);
    
    default Optional<MemberEntity> findById(Long id) {
        return findById(new MemberId(id));
    }
    
    Optional<MemberEntity> findByNickname(String nickname);
}
