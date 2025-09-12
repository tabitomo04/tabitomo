package com.koreatravel.tabitomo.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite key class for MemberAddInfoEntity
 * Represents the composite primary key for the member_add_info table.
 */
@Embeddable
public class MemberAddInfoId implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Column(name = "member_id", columnDefinition = "BINARY(16)")
    private UUID memberId;
    
    @Column(name = "info_high_num")
    private Integer infoHighNum;
    
    @Column(name = "info_low_num")
    private Integer infoLowNum;
    
    public MemberAddInfoId() {
        // Default constructor required by JPA
    }
    
    public MemberAddInfoId(UUID memberId, Integer infoHighNum, Integer infoLowNum) {
        this.memberId = memberId;
        this.infoHighNum = infoHighNum;
        this.infoLowNum = infoLowNum;
    }
    
    // Getters and setters
    public UUID getMemberId() {
        return memberId;
    }
    
    public void setMemberId(UUID memberId) {
        this.memberId = memberId;
    }
    
    public Integer getInfoHighNum() {
        return infoHighNum;
    }
    
    public void setInfoHighNum(Integer infoHighNum) {
        this.infoHighNum = infoHighNum;
    }
    
    public Integer getInfoLowNum() {
        return infoLowNum;
    }
    
    public void setInfoLowNum(Integer infoLowNum) {
        this.infoLowNum = infoLowNum;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberAddInfoId that = (MemberAddInfoId) o;
        return Objects.equals(memberId, that.memberId) &&
               Objects.equals(infoHighNum, that.infoHighNum) &&
               Objects.equals(infoLowNum, that.infoLowNum);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(memberId, infoHighNum, infoLowNum);
    }
    
    @Override
    public String toString() {
        return "MemberAddInfoId{" +
               "memberId=" + memberId +
               ", infoHighNum=" + infoHighNum +
               ", infoLowNum=" + infoLowNum +
               '}';
    }
    }
