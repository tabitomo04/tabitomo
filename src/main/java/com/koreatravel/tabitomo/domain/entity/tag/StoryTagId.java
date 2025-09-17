package com.koreatravel.tabitomo.domain.entity.tag;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class StoryTagId implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Column(name = "booknum")
    private Integer bookNum;
    
    @Column(name = "tag_id")
    private Integer tagId;
    
    public StoryTagId() {
    }
    
    public StoryTagId(Integer bookNum, Integer tagId) {
        this.bookNum = bookNum;
        this.tagId = tagId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoryTagId that = (StoryTagId) o;
        return Objects.equals(bookNum, that.bookNum) &&
               Objects.equals(tagId, that.tagId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(bookNum, tagId);
    }
    
    public Integer getBookNum() {
        return bookNum;
    }
    
    public void setBookNum(Integer bookNum) {
        this.bookNum = bookNum;
    }
    
    public Integer getTagId() {
        return tagId;
    }
    
    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }
}
