package com.koreatravel.tabitomo.id;

import java.io.Serializable;
import java.util.Objects;

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