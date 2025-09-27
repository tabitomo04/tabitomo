package com.koreatravel.tabitomo.domain.entity.member;

import lombok.*;
import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "add_info")
@IdClass(AddInfoId.class)
public class AddInfoEntity {
    
    @Id
    @Column(name = "info_high_num", nullable = false)
    private Integer infoHighNum;
    
    @Id
    @Column(name = "info_low_num", nullable = false)
    private Integer infoLowNum;
    
    @Column(name = "info_name", nullable = false, length = 100)
    private String infoName;
    
    @Column(name = "content", length = 255)
    private String content;
}
