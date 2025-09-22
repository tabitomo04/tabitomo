package com.koreatravel.tabitomo.controller.trip;

import com.koreatravel.tabitomo.config.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.koreatravel.tabitomo.domain.entity.trip.FavoritePlace;
import com.koreatravel.tabitomo.service.trip.FavoritePlaceService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
public class FavoritePlaceController {

    private final FavoritePlaceService favoritePlaceService;

    @GetMapping
    public ResponseEntity<?> getFavorites(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }
        try {
            List<FavoritePlace> favorites = favoritePlaceService.getFavorites(userDetails.getEmail());
            return ResponseEntity.ok(favorites);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> addFavorite(@RequestBody Map<String, String> payload, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }
        try {
            String placeId = payload.get("placeId");
            favoritePlaceService.addFavorite(userDetails.getEmail(), Long.parseLong(placeId));
            return ResponseEntity.ok().body(Collections.singletonMap("message", "즐겨찾기에 추가되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> removeFavorite(@RequestBody Map<String, String> payload, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }
        try {
            String placeId = payload.get("placeId");
            favoritePlaceService.removeFavorite(userDetails.getEmail(), Long.parseLong(placeId));
            return ResponseEntity.ok().body(Collections.singletonMap("message", "즐겨찾기에서 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getFavoriteStatus(@RequestParam String placeId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Collections.singletonMap("isFavorite", false));
        }
        boolean isFavorite = favoritePlaceService.isFavorite(userDetails.getEmail(), Long.parseLong(placeId));
        return ResponseEntity.ok(Collections.singletonMap("isFavorite", isFavorite));
    }
}
