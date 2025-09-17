package com.koreatravel.tabitomo.domain.entity.tag;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tag_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagMasterEntity {
    
    @Id
    @Column(name = "tag_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tagId;
    
    @Column(name = "tag_name", nullable = false, length = 50)
    private String tagName;
}
