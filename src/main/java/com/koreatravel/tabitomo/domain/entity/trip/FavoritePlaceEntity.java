package com.koreatravel.tabitomo.domain.entity.trip;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "FavoritePlace")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoritePlaceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column
    private String email;
    
    @Column
    private String place_id;
    
    @Column
    private LocalDateTime createdAt;
}
