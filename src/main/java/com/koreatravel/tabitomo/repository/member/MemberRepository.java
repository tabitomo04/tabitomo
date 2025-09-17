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
    
    /**
     * 이메일로 회원 존재 여부 확인
     * @param email 확인할 이메일
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByEmail(String email);
    
    /**
     * 닉네임으로 회원 존재 여부 확인
     * @param nickname 확인할 닉네임
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByNickname(String nickname);
}
