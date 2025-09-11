package com.koreatravel.tabitomo.repository;

import com.koreatravel.tabitomo.domain.entity.LikedbookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikedbookRepository extends JpaRepository<LikedbookEntity, Integer> {

    boolean existsByBooknumAndEmail(Integer booknum, String email);

    void deleteByBooknumAndEmail(Integer booknum, String email);

}
