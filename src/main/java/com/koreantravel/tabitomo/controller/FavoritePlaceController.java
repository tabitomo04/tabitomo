package com.koreantravel.tabitomo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.koreantravel.tabitomo.domain.entity.FavoritePlace;
import com.koreantravel.tabitomo.service.FavoritePlaceService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
public class FavoritePlaceController {

    private final FavoritePlaceService favoritePlaceService;

    @GetMapping
    public ResponseEntity<?> getFavorites(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }
        try {
            List<FavoritePlace> favorites = favoritePlaceService.getFavorites(userDetails.getUsername());
            return ResponseEntity.ok(favorites);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> addFavorite(@RequestBody Map<String, Long> payload, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }
        try {
            Long placeId = payload.get("placeId");
            favoritePlaceService.addFavorite(userDetails.getUsername(), placeId);
            return ResponseEntity.ok().body(Collections.singletonMap("message", "즐겨찾기에 추가되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> removeFavorite(@RequestBody Map<String, Long> payload, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }
        try {
            Long placeId = payload.get("placeId");
            favoritePlaceService.removeFavorite(userDetails.getUsername(), placeId);
            return ResponseEntity.ok().body(Collections.singletonMap("message", "즐겨찾기에서 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getFavoriteStatus(@RequestParam Long placeId, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Collections.singletonMap("isFavorite", false));
        }
        boolean isFavorite = favoritePlaceService.isFavorite(userDetails.getUsername(), placeId);
        return ResponseEntity.ok(Collections.singletonMap("isFavorite", isFavorite));
    }
}
