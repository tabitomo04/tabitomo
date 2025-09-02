package com.koreatravel.tabitomo.domain.entity;

import com.koreatravel.tabitomo.id.AddInfoId;

import jakarta.persistence.Column;
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
@IdClass(AddInfoId.class)
@Table(name = "AddInfo")
public class AddInfoEntity {

    @Id
    private int infohighnum;

    @Id
    private int infolownum;

    @Column
    private String content;
}
