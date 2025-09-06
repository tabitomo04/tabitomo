package com.koreatravel.tabitomo.id;

import java.io.Serializable;
import java.util.Objects;
/*
    데이터베이스 중 ID가 2개 이상인 경우 새로 만들어서 해야 함,
    AddInfo 테이블의 infohighnum과 infolownum을 합쳐서 ID로 사용
 */
public class AddInfoId implements Serializable {
    private int infohighnum;
    private int infolownum;

    public AddInfoId() {
    }

    public AddInfoId(int infohighnum, int infolownum) {
        this.infohighnum = infohighnum;
        this.infolownum = infolownum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddInfoId that = (AddInfoId) o;
        return infohighnum == that.infohighnum && 
               infolownum == that.infolownum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(infohighnum, infolownum);
    }
    
    // Getters and setters
    public int getInfohighnum() {
        return infohighnum;
    }

    public void setInfohighnum(int infohighnum) {
        this.infohighnum = infohighnum;
    }

    public int getInfolownum() {
        return infolownum;
    }

    public void setInfolownum(int infolownum) {
        this.infolownum = infolownum;
    }
}