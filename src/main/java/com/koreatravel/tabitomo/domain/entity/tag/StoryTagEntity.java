package com.koreatravel.tabitomo.domain.entity.tag;

import com.koreatravel.tabitomo.domain.entity.storybook.StorybookEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "story_tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoryTagEntity {
    
    @EmbeddedId
    private StoryTagId id;
    
    @MapsId("bookNum")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booknum", nullable = false)
    private StorybookEntity storybook;
    
    @MapsId("tagId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private TagMasterEntity tag;
    
    // Helper method to set both the relationship and the ID
    public void setStorybook(StorybookEntity storybook) {
        // Prevent endless loop
        if (this.storybook != null && this.storybook.equals(storybook)) {
            return;
        }
        
        // Set new storybook
        StorybookEntity oldStorybook = this.storybook;
        this.storybook = storybook;
        
        // Update ID
        if (this.id == null) {
            this.id = new StoryTagId();
        }
        this.id.setBookNum(storybook != null ? storybook.getBooknum() : null);
        
        // Remove from old storybook's tags
        if (oldStorybook != null) {
            oldStorybook.getTags().remove(this);
        }
        
        // Add to new storybook's tags
        if (storybook != null && !storybook.getTags().contains(this)) {
            storybook.getTags().add(this);
        }
    }
    
    // Helper method to set both the relationship and the ID
    public void setTag(TagMasterEntity tag) {
        // Prevent endless loop
        if (this.tag != null && this.tag.equals(tag)) {
            return;
        }
        
        // Set new tag
        this.tag = tag;
        
        // Update ID
        if (this.id == null) {
            this.id = new StoryTagId();
        }
        this.id.setTagId(tag != null ? tag.getTagId() : null);
    }
    
    // Helper method to get bookNum
    public Integer getBookNum() {
        return id != null ? id.getBookNum() : null;
    }
    
    // Helper method to get tagId
    public Integer getTagId() {
        return id != null ? id.getTagId() : null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoryTagEntity that = (StoryTagEntity) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
