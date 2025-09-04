package com.koreatravel.tabitomo.repository;

import com.koreatravel.tabitomo.entity.ForbiddenWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForbiddenWordRepository extends JpaRepository<ForbiddenWord, Integer> {
    // Spring Data JPA가 자동으로 CRUD(생성, 조회, 수정, 삭제) 기능을 제공합니다.
    // 여기에 추가적인 메서드가 필요할 경우 정의할 수 있습니다.
}
