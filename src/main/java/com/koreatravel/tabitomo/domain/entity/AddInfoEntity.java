package com.koreatravel.tabitomo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "AddInfo")
public class AddInfoEntity {
    @Id
    private String highnum;

    @Id
    private String lownum;

    @Column
    private String content;
}
