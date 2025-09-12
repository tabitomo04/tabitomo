package com.koreatravel.tabitomo.repository.storybook;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.storybook.StorybookEntity;
import com.koreatravel.tabitomo.domain.entity.storybook.TempsaveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TempsaveRepository extends JpaRepository<TempsaveEntity, Integer> {
    
    /**
     * Find a temporary save by its ID and member
     * @param tempId The ID of the temporary save
     * @param member The member who owns the temporary save
     * @return An Optional containing the TempsaveEntity if found
     */
    Optional<TempsaveEntity> findByTempIdAndMember(Integer tempId, MemberEntity member);
    
    /**
     * Find all temporary saves for a specific member
     * @param member The member who owns the temporary saves
     * @return List of TempsaveEntity for the member
     */
    List<TempsaveEntity> findByMemberOrderBySaveTimeDesc(MemberEntity member);
    
    /**
     * Delete a temporary save by its ID and member
     * @param tempId The ID of the temporary save to delete
     * @param member The member who owns the temporary save
     * @return Number of records deleted
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM TempsaveEntity t WHERE t.tempId = :tempId AND t.member = :member")
    int deleteByTempIdAndMember(@Param("tempId") Integer tempId, @Param("member") MemberEntity member);
    
    /**
     * Find a temporary save by storybook and member
     * @param storybook The storybook associated with the temporary save
     * @param member The member who owns the temporary save
     * @return An Optional containing the TempsaveEntity if found
     */
    Optional<TempsaveEntity> findByStorybookAndMember(StorybookEntity storybook, MemberEntity member);
    
    /**
     * Find all temporary saves for a specific storybook
     * @param storybook The storybook associated with the temporary saves
     * @return List of TempsaveEntity for the storybook
     */
    List<TempsaveEntity> findByStorybook(StorybookEntity storybook);
}
