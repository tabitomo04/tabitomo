package com.koreatravel.tabitomo.repository.chat;

import com.koreatravel.tabitomo.domain.entity.chat.MainCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MainCategoryRepository extends JpaRepository<MainCategory, Integer> {
}
