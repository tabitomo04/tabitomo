package com.koreatravel.tabitomo.repository.storybook;

import com.koreatravel.tabitomo.domain.entity.storybook.MediaEntity;
import com.koreatravel.tabitomo.domain.entity.storybook.MediaEntity.MediaStatus;
import com.koreatravel.tabitomo.domain.entity.storybook.StorybookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for MediaEntity persistence operations.
 * Handles CRUD operations for media files associated with storybooks.
 */
public interface MediaRepository extends JpaRepository<MediaEntity, Integer> {
    
    /**
     * Delete all media entries for a specific storybook.
     * @param bookNum The ID of the storybook
     */
    void deleteByBookNum(Integer bookNum);
    
    /**
     * Delete media entries for a specific storybook with a given status.
     * @param bookNum The ID of the storybook
     * @param status The status of the media to delete
     */
    void deleteByBookNumAndStatus(Integer bookNum, MediaStatus status);
    
    /**
     * Find all media entries for a specific storybook with a given status.
     * @param bookNum The ID of the storybook
     * @param status The status of the media to find
     * @return List of matching media entities
     */
    List<MediaEntity> findByBookNumAndStatus(Integer bookNum, MediaStatus status);
    
    /**
     * Find a media entity by its URL.
     * @param mediaUrl The URL of the media
     * @return Optional containing the media entity if found
     */
    Optional<MediaEntity> findByMediaUrl(String mediaUrl);
    
    /**
     * Find all media entries for a specific storybook.
     * @param storybook The storybook entity
     * @return List of media entities for the storybook
     */
    List<MediaEntity> findByStorybook(StorybookEntity storybook);
    
    /**
     * Find all media entries for a specific storybook with a given media type.
     * @param storybook The storybook entity
     * @param mediaType The type of media to find (e.g., "image", "video")
     * @return List of matching media entities
     */
    List<MediaEntity> findByStorybookAndMediaType(StorybookEntity storybook, String mediaType);
    
    /**
     * Update the status of all media entries for a specific storybook.
     * @param storybook The storybook entity
     * @param oldStatus The current status of the media
     * @param newStatus The new status to set
     * @return Number of records updated
     */
    @Transactional
    @Modifying
    @Query("UPDATE MediaEntity m SET m.status = :newStatus WHERE m.storybook = :storybook AND m.status = :oldStatus")
    int updateStatusByStorybookAndStatus(
        @Param("storybook") StorybookEntity storybook,
        @Param("oldStatus") MediaStatus oldStatus,
        @Param("newStatus") MediaStatus newStatus
    );
    
    /**
     * Count the number of media entries for a specific storybook and status.
     * @param storybook The storybook entity
     * @param status The status to filter by
     * @return Count of matching media entries
     */
    long countByStorybookAndStatus(StorybookEntity storybook, MediaStatus status);
}
