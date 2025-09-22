package com.koreatravel.tabitomo.repository.tag;

import com.koreatravel.tabitomo.domain.entity.storybook.StorybookEntity;
import com.koreatravel.tabitomo.domain.entity.tag.StoryTagEntity;
import com.koreatravel.tabitomo.domain.entity.tag.StoryTagId;
import com.koreatravel.tabitomo.domain.entity.tag.TagMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface StorytagRepository extends JpaRepository<StoryTagEntity, StoryTagId> {
    
    @Modifying
    @Transactional
    @Query("DELETE FROM StoryTagEntity st WHERE st.id.bookNum = :booknum")
    void deleteByBookNum(@Param("booknum") Integer booknum);
    
    // For backward compatibility
    default void deleteByStorybook_Booknum(Integer booknum) {
        deleteByBookNum(booknum);
    }

    boolean existsByStorybookAndTag(StorybookEntity entity, TagMasterEntity tagentity);
}
