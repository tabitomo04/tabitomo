package com.koreatravel.tabitomo.domain.entity.member;

import com.koreatravel.tabitomo.id.UserSelectedInfoId;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int infohighnum;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int infolownum;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String email;
}
