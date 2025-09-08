package com.koreatravel.tabitomo.id;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserSelectedInfoId implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Column(name = "info_high_num")
    private Integer infoHighNum;
    
    @Column(name = "info_low_num")
    private Integer infoLowNum;
    
    @Column(name = "email")
    private String email;
}