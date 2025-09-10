package com.koreatravel.tabitomo.controller.api;

import com.koreatravel.tabitomo.dto.trip.FavoritePlaceDetailDTO;
import com.koreatravel.tabitomo.service.trip.FavoritePlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(favoritePlaceService.getFavorites(email, pageable));
    }

    /**
     * 즐겨찾기 추가
     */
    @PostMapping("/{placeId}")
    public ResponseEntity<FavoritePlaceDetailDTO> addFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String placeId) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(favoritePlaceService.addFavorite(email, placeId));
    }

    /**
     * 즐겨찾기 제거
     */
    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> removeFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String placeId) {
        String email = userDetails.getUsername();
        favoritePlaceService.removeFavorite(email, placeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 즐겨찾기 여부 확인
     */
    @GetMapping("/check/{placeId}")
    public ResponseEntity<Boolean> isFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String placeId) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(favoritePlaceService.isFavorite(email, placeId));
    }
}
