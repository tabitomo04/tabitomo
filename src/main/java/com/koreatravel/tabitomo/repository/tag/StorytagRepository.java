package com.koreatravel.tabitomo.repository.tag;

import com.koreatravel.tabitomo.domain.entity.tag.StoryTagEntity;
import com.koreatravel.tabitomo.domain.entity.tag.StoryTagId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorytagRepository extends JpaRepository<StoryTagEntity, StoryTagId> {
    void deleteByStorybook_Booknum(Integer booknum);
}
