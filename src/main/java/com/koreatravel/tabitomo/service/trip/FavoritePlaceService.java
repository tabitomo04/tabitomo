package com.koreatravel.tabitomo.service.trip;

import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.trip.FavoritePlace;
import com.koreatravel.tabitomo.domain.entity.trip.Place;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import com.koreatravel.tabitomo.repository.trip.FavoritePlaceRepository;
import com.koreatravel.tabitomo.repository.trip.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class FavoritePlaceService {

    private final FavoritePlaceRepository favoritePlaceRepository;
    private final MemberRepository memberRepository;
    private final PlaceRepository placeRepository;

    public void addFavorite(String email, Long placeId) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다: " + email));
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다."));

        if (favoritePlaceRepository.existsByMemberAndPlace(member, place)) {
            throw new IllegalStateException("이미 즐겨찾기에 추가된 장소입니다.");
        }

        FavoritePlace favoritePlace = FavoritePlace.builder()
                .member(member)
                .place(place)
                .build();

        favoritePlaceRepository.save(favoritePlace);
    }

    public void removeFavorite(String email, Long placeId) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다: " + email));
        FavoritePlace favoritePlace = favoritePlaceRepository.findByMemberAndPlace_Id(member, placeId)
                .orElseThrow(() -> new IllegalArgumentException("즐겨찾기 정보를 찾을 수 없습니다."));

        favoritePlaceRepository.delete(favoritePlace);
    }

    @Transactional(readOnly = true)
    public List<FavoritePlace> getFavorites(String email) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다: " + email));
        return favoritePlaceRepository.findByMember(member);
    }

    @Transactional(readOnly = true)
    public Page<FavoritePlace> getFavorites(String email, Pageable pageable) {
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다: " + email));
        return favoritePlaceRepository.findByMember(member, pageable);
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(String email, Long placeId) {
         MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다: " + email));
        return favoritePlaceRepository.existsByMemberAndPlace_Id(member, placeId);
    }
}
