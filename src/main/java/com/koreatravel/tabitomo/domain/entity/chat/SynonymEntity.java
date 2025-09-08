package com.koreatravel.tabitomo.domain.entity.chat;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "synonym")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SynonymEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "synonym_id", columnDefinition = "INT NOT NULL AUTO_INCREMENT")
    private Integer synonymId;

    @Column(name = "main_keyword", nullable = false, length = 100)
    private String mainKeyword;

    @Column(name = "synonym_keyword", nullable = false, length = 100)
    private String synonymKeyword;
    
    // Builder pattern implementation
    public static SynonymEntityBuilder builder() {
        return new SynonymEntityBuilder();
    }
}
