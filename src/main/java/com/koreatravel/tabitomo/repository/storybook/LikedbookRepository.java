package com.koreatravel.tabitomo.repository.storybook;

import com.koreatravel.tabitomo.domain.entity.storybook.LikedbookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikedbookRepository extends JpaRepository<LikedbookEntity, Integer> {

    boolean existsByBooknumAndEmail(Integer booknum, String email);

    void deleteByBooknumAndEmail(Integer booknum, String email);

}
