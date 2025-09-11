package com.koreatravel.tabitomo.repository.storybook;

import com.koreatravel.tabitomo.domain.entity.storybook.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<LikeEntity, Integer> {
    boolean existsByBooknumAndEmail(Integer booknum, String email);
    void deleteByBooknumAndEmail(Integer booknum, String email);
    // 특정 회원이 좋아요한 스토리북 목록 조회 (필요 시 사용)
    // List<LikeEntity> findByMember(Member member);
}
