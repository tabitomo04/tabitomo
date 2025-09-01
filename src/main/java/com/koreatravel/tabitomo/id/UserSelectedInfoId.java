package com.koreatravel.tabitomo.id;

import java.io.Serializable;
import java.util.Objects;

public class UserSelectedInfoId implements Serializable {
    private int infohighnum;
    private int infolownum;
    private String email;

    public UserSelectedInfoId() {
    }

    public UserSelectedInfoId(int infohighnum, int infolownum, String email) {
        this.infohighnum = infohighnum;
        this.infolownum = infolownum;
        this.email = email;
    }

    // Getters and setters
    // Must implement equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSelectedInfoId that = (UserSelectedInfoId) o;
        return infohighnum == that.infohighnum && 
               infolownum == that.infolownum && 
               Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(infohighnum, infolownum, email);
    }
}