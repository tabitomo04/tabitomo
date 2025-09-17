package com.koreatravel.tabitomo.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "storytag",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_storytag_booknum_tagid", columnNames = {"booknum", "tag_id"})
        })
@EntityListeners(AuditingEntityListener.class)
public class StorytagEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "storytag_id")
    private Integer storytagId;

    @ManyToOne
    @JoinColumn(name = "booknum", nullable = false)
    private StorybookEntity storybook;

    @ManyToOne
    @JoinColumn(name = "tag_id", nullable = false)
    private TagMasterEntity tagId;

}
