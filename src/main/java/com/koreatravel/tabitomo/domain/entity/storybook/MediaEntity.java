package com.koreatravel.tabitomo.domain.entity.storybook;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "media")
public class MediaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id", nullable = false)
    private Integer mediaId;

    @Column(name = "book_num")
    private Integer bookNum;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MediaStatus status;

    @Column(name = "media_url", nullable = false, length = 255)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    private MediaType mediaType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_num", referencedColumnName = "book_num", insertable = false, updatable = false)
    private StorybookEntity storybook;

    public enum MediaStatus {
        UPLOAD("upload"),
        TEMP("temp"),
        DELETED("deleted");

        private final String value;

        MediaStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
        
        public static MediaStatus fromValue(String value) {
            for (MediaStatus status : values()) {
                if (status.value.equalsIgnoreCase(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("No enum constant " + MediaStatus.class.getName() + "." + value);
        }
    }

    public enum MediaType {
        IMAGE("image"),
        VIDEO("video");

        private final String value;

        MediaType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
        
        public static MediaType fromValue(String value) {
            for (MediaType type : values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No enum constant " + MediaType.class.getName() + "." + value);
        }
    }
    
    // Builder pattern implementation
    public static MediaEntityBuilder builder() {
        return new MediaEntityBuilder();
    }
}
