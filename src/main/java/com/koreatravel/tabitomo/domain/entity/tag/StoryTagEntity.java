package com.koreatravel.tabitomo.domain.entity.tag;

import com.koreatravel.tabitomo.domain.entity.storybook.StorybookEntity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
class StoryTagId implements Serializable {
    @Column(name = "booknum")
    private Integer bookNum;
    
    @Column(name = "tag_id")
    private Integer tagId;
}

@Entity
@Table(name = "story_tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(StoryTagId.class)
public class StoryTagEntity {
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booknum", nullable = false, insertable = false, updatable = false)
    private StorybookEntity storybook;
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false, insertable = false, updatable = false)
    private TagMaster tag;
    
    // Composite key fields (must match the @IdClass fields)
    @Column(name = "booknum")
    private Integer bookNum;
    
    @Column(name = "tag_id")
    private Integer tagId;
    
    // Helper method to set both the relationship and the ID
    public void setStorybook(StorybookEntity storybook) {
        this.storybook = storybook;
        this.bookNum = storybook != null ? storybook.getBooknum() : null;
    }
    
    // Helper method to set both the relationship and the ID
    public void setTag(TagMaster tag) {
        this.tag = tag;
        this.tagId = tag != null ? tag.getTagId() : null;
    }
}
