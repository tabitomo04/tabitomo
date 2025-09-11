package com.koreatravel.tabitomo.controller.api;

import com.koreatravel.tabitomo.dto.trip.FavoritePlaceDetailDTO;
import com.koreatravel.tabitomo.service.trip.FavoritePlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import com.koreatravel.tabitomo.config.security.MemberDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
public class FavoritePlaceController {

    private final FavoritePlaceService favoritePlaceService;

    /**
     * 즐겨찾기 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<FavoritePlaceDetailDTO>> getFavorites(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(favoritePlaceService.getFavorites(memberDetails.getMember().getId(), pageable));
    }

    /**
     * 즐겨찾기 추가
     */
    @PostMapping("/{placeId}")
    public ResponseEntity<FavoritePlaceDetailDTO> addFavorite(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable String placeId) {
        return ResponseEntity.ok(favoritePlaceService.addFavorite(memberDetails.getMember().getId(), placeId));
    }

    /**
     * 즐겨찾기 제거
     */
    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> removeFavorite(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable String placeId) {
        favoritePlaceService.removeFavorite(memberDetails.getMember().getId(), placeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 즐겨찾기 여부 확인
     */
    @GetMapping("/check/{placeId}")
    public ResponseEntity<Boolean> isFavorite(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable String placeId) {
        return ResponseEntity.ok(favoritePlaceService.isFavorite(memberDetails.getMember().getId(), placeId));
    }
}
