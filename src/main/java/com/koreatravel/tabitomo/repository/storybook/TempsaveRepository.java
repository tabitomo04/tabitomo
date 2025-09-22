package com.koreatravel.tabitomo.repository.storybook;

import com.koreatravel.tabitomo.domain.entity.storybook.TempsaveEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TempsaveRepository extends JpaRepository<TempsaveEntity, Integer> {
    List<TempsaveEntity> findByMember_Email(String email);
}
