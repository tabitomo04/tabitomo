package com.koreatravel.tabitomo.service.trip;

import com.koreatravel.tabitomo.domain.entity.trip.FavoritePlaceEntity;
import com.koreatravel.tabitomo.domain.dto.trip.FavoritePlaceDetailDTO;
import com.koreatravel.tabitomo.repository.trip.FavoritePlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.repository.member.MemberRepository;

@Service
@RequiredArgsConstructor
public class FavoritePlaceService {

    private final FavoritePlaceRepository favoritePlaceRepository;
    private final MemberRepository memberRepository;

    /**
     * 즐겨찾기 추가
     */
    @Transactional
    public FavoritePlaceDetailDTO addFavorite(UUID memberId, String placeId) {
        // 멤버 조회
        MemberEntity member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 이미 즐겨찾기한 장소인지 확인
        if (favoritePlaceRepository.existsByMemberIdAndPlaceId(memberId, placeId)) {
            throw new IllegalStateException("이미 즐겨찾기에 추가된 장소입니다.");
        }

        FavoritePlaceEntity favorite = FavoritePlaceEntity.builder()
                .member(member)
                .placeId(placeId)
                .build();
        favorite.setEmail(member.getEmail()); // 이메일은 조회용으로만 사용

        FavoritePlaceEntity saved = favoritePlaceRepository.save(favorite);
        return FavoritePlaceDetailDTO.fromEntity(saved);
    }

    /**
     * 즐겨찾기 제거
     */
    @Transactional
    public void removeFavorite(UUID memberId, String placeId) {
        favoritePlaceRepository.removeByMemberIdAndPlaceId(memberId, placeId);
    }

    /**
     * 회원의 즐겨찾기 목록 조회 (페이징 처리)
     */
    @Transactional(readOnly = true)
    public Page<FavoritePlaceDetailDTO> getFavorites(UUID memberId, Pageable pageable) {
        Page<FavoritePlaceEntity> favorites = favoritePlaceRepository.findByMemberId(memberId, pageable);
        return favorites.map(FavoritePlaceDetailDTO::fromEntity);
    }

    /**
     * 특정 장소가 즐겨찾기로 등록되어 있는지 확인
     */
    @Transactional(readOnly = true)
    public boolean isFavorite(UUID memberId, String placeId) {
        return favoritePlaceRepository.existsByMemberIdAndPlaceId(memberId, placeId);
    }
}
