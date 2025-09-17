package com.koreatravel.tabitomo.domain.entity.tag;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoryTagId implements Serializable {
    @Column(name = "booknum")
    private Integer bookNum;
    
    @Column(name = "tag_id")
    private Integer tagId;
}
