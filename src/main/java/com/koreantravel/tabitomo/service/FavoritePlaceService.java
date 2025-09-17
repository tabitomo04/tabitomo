package com.koreantravel.tabitomo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.koreantravel.tabitomo.domain.entity.FavoritePlace;
import com.koreantravel.tabitomo.domain.entity.Member;
import com.koreantravel.tabitomo.domain.entity.Place;
import com.koreantravel.tabitomo.repository.FavoritePlaceRepository;
import com.koreantravel.tabitomo.repository.MemberRepository;
import com.koreantravel.tabitomo.repository.PlaceRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FavoritePlaceService {

    private final FavoritePlaceRepository favoritePlaceRepository;
    private final MemberRepository memberRepository;
    private final PlaceRepository placeRepository;

    public void addFavorite(String email, Long placeId) {
        Member member = memberRepository.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다."));

        if (favoritePlaceRepository.existsByMemberEmailAndPlaceId(email, placeId)) {
            throw new IllegalStateException("이미 즐겨찾기에 추가된 장소입니다.");
        }

        FavoritePlace favoritePlace = FavoritePlace.builder()
                .member(member)
                .place(place)
                .build();

        favoritePlaceRepository.save(favoritePlace);
    }

    public void removeFavorite(String email, Long placeId) {
        FavoritePlace favoritePlace = favoritePlaceRepository.findByMemberEmailAndPlaceId(email, placeId)
                .orElseThrow(() -> new IllegalArgumentException("즐겨찾기 정보를 찾을 수 없습니다."));

        favoritePlaceRepository.delete(favoritePlace);
    }

    @Transactional(readOnly = true)
    public List<FavoritePlace> getFavorites(String email) {
        return favoritePlaceRepository.findByMemberEmail(email);
    }

    @Transactional(readOnly = true)
    public Page<FavoritePlace> getFavorites(String email, Pageable pageable) {
        return favoritePlaceRepository.findByMemberEmail(email, pageable);
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(String email, Long placeId) {
        return favoritePlaceRepository.existsByMemberEmailAndPlaceId(email, placeId);
    }
}
