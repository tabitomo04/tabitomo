package com.koreatravel.tabitomo.domain.entity;

import com.koreatravel.tabitomo.id.AddInfoId;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity 
@IdClass(AddInfoId.class)
@Table(name = "AddInfo")
public class AddInfoEntity {
    @Id
    private int infohighnum;

    @Id
    private int infolownum;

    private String content;
}
