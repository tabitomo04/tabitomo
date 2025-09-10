package com.koreatravel.tabitomo.service.trip;

import com.koreatravel.tabitomo.domain.entity.trip.FavoritePlaceEntity;
import com.koreatravel.tabitomo.domain.entity.trip.PlaceEntity;
import com.koreatravel.tabitomo.dto.trip.FavoritePlaceDetailDTO;
import com.koreatravel.tabitomo.repository.trip.PlaceRepository;
import com.koreatravel.tabitomo.repository.trip.FavoritePlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FavoritePlaceService {

    private final FavoritePlaceRepository favoritePlaceRepository;
    private final PlaceRepository placeRepository;

    /**
     * 즐겨찾기 추가
     */
    @Transactional
    public FavoritePlaceDetailDTO addFavorite(String memberEmail, String placeId) {
        // 이미 존재하는지 확인
        if (favoritePlaceRepository.existsByMemberEmailAndPlaceId(memberEmail, placeId)) {
            throw new IllegalStateException("이미 즐겨찾기에 추가된 장소입니다.");
        }

        // 장소 정보 조회
        PlaceEntity place = placeRepository.findById(placeId).orElseThrow(
            () -> new IllegalArgumentException("존재하지 않는 장소입니다.")
        );

        FavoritePlaceEntity favorite = FavoritePlaceEntity.builder()
                .memberEmail(memberEmail)
                .email(memberEmail) // memberEmail과 동기화
                .placeId(placeId)
                .createdAt(LocalDateTime.now())
                .build();

        FavoritePlaceEntity saved = favoritePlaceRepository.save(favorite);
        return FavoritePlaceDetailDTO.fromEntity(saved, place);
    }

    /**
     * 즐겨찾기 제거
     */
    @Transactional
    public void removeFavorite(String memberEmail, String placeId) {
        favoritePlaceRepository.deleteByMemberEmailAndPlaceId(memberEmail, placeId);
    }

    /**
     * 회원의 즐겨찾기 목록 조회 (페이징 처리)
     */
    @Transactional(readOnly = true)
    public Page<FavoritePlaceDetailDTO> getFavorites(String memberEmail, Pageable pageable) {
        return favoritePlaceRepository.findByMemberEmail(memberEmail, pageable)
                .map(favorite -> {
                    // 장소 조회
                    PlaceEntity place = placeRepository.findById(favorite.getPlaceId())
                            .orElse(null);
                    return FavoritePlaceDetailDTO.fromEntity(favorite, place);
                });
    }

    /**
     * 특정 장소가 즐겨찾기로 등록되어 있는지 확인
     */
    @Transactional(readOnly = true)
    public boolean isFavorite(String memberEmail, String placeId) {
        return favoritePlaceRepository.existsByMemberEmailAndPlaceId(memberEmail, placeId);
    }
}
