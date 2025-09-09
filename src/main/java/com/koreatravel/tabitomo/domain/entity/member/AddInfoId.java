package com.koreatravel.tabitomo.domain.entity.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class AddInfoId implements Serializable {
    @Column(name = "info_high_num", nullable = false)
    private Integer infoHighNum;
    
    @Column(name = "info_low_num", nullable = false)
    private Long infoLowNum;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddInfoId that = (AddInfoId) o;
        return Objects.equals(infoHighNum, that.infoHighNum) &&
               Objects.equals(infoLowNum, that.infoLowNum);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(infoHighNum, infoLowNum);
    }
}
