package com.koreatravel.tabitomo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Language")
public class LanguageEntity {
    @Id
    private int lang_id;
    
    @Column
    private String lang_code;
    
    @Column
    private String lang_name;
}
