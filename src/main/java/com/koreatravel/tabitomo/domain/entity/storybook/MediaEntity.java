package com.koreatravel.tabitomo.domain.entity.storybook;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Builder
@Table(name = "Media")
public class MediaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "num")
    private Integer num;

    @Column(name = "status")
    private MediaStatus status;

    @Column(name = "media_url", length = 255)
    private String mediaUrl;

    @Column(name = "media_type", length = 30)
    private MediaType mediaType;

    @Column(name = "uploaded_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime uploadTime;

    public enum MediaStatus {
        upload, temp
    }

    public enum MediaType {
        image, video
    }
}
