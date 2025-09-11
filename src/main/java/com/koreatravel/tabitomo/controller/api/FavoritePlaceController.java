package com.koreatravel.tabitomo.controller.api;

import com.koreatravel.tabitomo.service.trip.FavoritePlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import com.koreatravel.tabitomo.config.security.MemberDetails;
import com.koreatravel.tabitomo.dto.trip.FavoritePlaceDetailDTO;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
        // Convert Long member ID to UUID
        UUID memberUuid = UUID.nameUUIDFromBytes(memberDetails.getMember().getId().toString().getBytes());
        return ResponseEntity.ok(favoritePlaceService.getFavorites(memberUuid, pageable));
    }

    /**
     * 즐겨찾기 추가
     */
    @PostMapping("/{placeId}")
    public ResponseEntity<FavoritePlaceDetailDTO> addFavorite(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable String placeId) {
        // Convert Long member ID to UUID
        UUID memberUuid = UUID.nameUUIDFromBytes(memberDetails.getMember().getId().toString().getBytes());
        return ResponseEntity.ok(favoritePlaceService.addFavorite(memberUuid, placeId));
    }

    /**
     * 즐겨찾기 제거
     */
    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> removeFavorite(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable String placeId) {
        // Convert Long member ID to UUID
        UUID memberUuid = UUID.nameUUIDFromBytes(memberDetails.getMember().getId().toString().getBytes());
        favoritePlaceService.removeFavorite(memberUuid, placeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 즐겨찾기 여부 확인
     */
    @GetMapping("/check/{placeId}")
    public ResponseEntity<Boolean> isFavorite(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable String placeId) {
        // Convert Long member ID to UUID
        UUID memberUuid = UUID.nameUUIDFromBytes(memberDetails.getMember().getId().toString().getBytes());
        return ResponseEntity.ok(favoritePlaceService.isFavorite(memberUuid, placeId));
    }
}
