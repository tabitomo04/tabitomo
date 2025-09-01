package com.koreatravel.tabitomo.domain.entity;

import com.koreatravel.tabitomo.id.UserSelectedInfoId;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "UserSelectedInfo")
@IdClass(UserSelectedInfoId.class)
public class UserSelectedInfoEntity {
    @Id
    private int infohighnum;
    
    @Id
    private int infolownum;
    
    @Id
    private String email;
}
