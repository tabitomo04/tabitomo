package com.koreatravel.tabitomo.repository;

import com.koreatravel.tabitomo.domain.entity.StorytagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorytagRepository extends JpaRepository<StorytagEntity,Integer> {
    void deleteByStorybook_Booknum(Integer booknum);
}
