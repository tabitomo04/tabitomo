package com.koreatravel.tabitomo.domain.dto.storybook;

import java.time.LocalDateTime;

/**
 * Projection interface for storybook list view
 */
public interface StorybookListDTO {
    /**
     * @return the unique identifier of the storybook
     */
    Integer getBooknum();
    
    /**
     * @return the title of the storybook
     */
    String getTitle();
    
    /**
     * @return the subtitle of the storybook
     */
    String getSubtitle();
    
    /**
     * @return the number of likes the storybook has received
     */
    Integer getLikes();
    
    /**
     * @return the creation date of the storybook
     */
    LocalDateTime getCreateDate();
    
    /**
     * @return the URL of the storybook's thumbnail image
     */
    String getThumbnail();
}
