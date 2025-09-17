package com.koreatravel.tabitomo.repository;

import com.koreatravel.tabitomo.domain.entity.tag.StoryTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorytagRepository extends JpaRepository<StoryTagEntity,Integer> {
    void deleteByStorybook_Booknum(Integer booknum);
}
