package com.koreatravel.tabitomo.repository.storybook;

import com.koreatravel.tabitomo.domain.entity.storybook.LikeEntity;
import com.koreatravel.tabitomo.domain.entity.storybook.StorybookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Repository interface for LikeEntity persistence operations.
 * Handles CRUD operations for user likes on storybooks.
 */
public interface LikedbookRepository extends JpaRepository<LikeEntity, Long> {
    
    /**
     * Find a like by member email and storybook number.
     * @param email The email of the member who liked the storybook
     * @param bookNum The ID of the storybook that was liked
     * @return Optional containing the LikeEntity if found
     */
    @Query("SELECT l FROM LikeEntity l WHERE l.email = :email AND l.booknum = :bookNum")
    Optional<LikeEntity> findByEmailAndBookNum(@Param("email") String email, @Param("bookNum") Integer bookNum);
    
    /**
     * Check if a member has liked a specific storybook.
     * @param email The email of the member
     * @param bookNum The ID of the storybook
     * @return true if the member has liked the storybook, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END " +
           "FROM LikeEntity l WHERE l.email = :email AND l.booknum = :bookNum")
    boolean existsByEmailAndBookNum(@Param("email") String email, @Param("bookNum") Integer bookNum);
    
    /**
     * Count the number of likes for a specific storybook.
     * @param storybook The storybook to count likes for
     * @return The number of likes
     */
    long countByStorybook(StorybookEntity storybook);
    
    /**
     * Delete a like by email and book number.
     * @param email The email of the member who liked the storybook
     * @param bookNum The ID of the storybook that was liked
     * @return Number of records deleted
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM LikeEntity l WHERE l.email = :email AND l.booknum = :bookNum")
    int deleteByEmailAndBookNum(@Param("email") String email, @Param("bookNum") Integer bookNum);
    
    /**
     * Delete all likes for a specific storybook.
     * @param bookNum The storybook number to delete likes for
     * @return Number of records deleted
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM LikeEntity l WHERE l.booknum = :bookNum")
    int deleteByBookNum(@Param("bookNum") Integer bookNum);
    
    /**
     * Delete all likes by a specific member's email.
     * @param email The email of the member whose likes should be deleted
     * @return Number of records deleted
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM LikeEntity l WHERE l.email = :email")
    int deleteByEmail(@Param("email") String email);
}
