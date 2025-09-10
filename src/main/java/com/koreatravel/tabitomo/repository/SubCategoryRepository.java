package com.koreatravel.tabitomo.repository;

import com.koreatravel.tabitomo.entity.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Integer> {
    List<SubCategory> findByMainCategoryId(Integer mainCategoryId);
}

