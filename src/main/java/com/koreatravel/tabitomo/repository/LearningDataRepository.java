package com.koreatravel.tabitomo.repository;

import com.koreatravel.tabitomo.entity.LearningData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LearningDataRepository extends JpaRepository<LearningData, Long> {
}