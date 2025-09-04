package com.koreatravel.tabitomo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.koreatravel.tabitomo.repository.PlaceRepository;
import com.koreatravel.tabitomo.domain.dto.PlaceDTO;
import com.koreatravel.tabitomo.repository.FavoritePlaceRepository;
import com.koreatravel.tabitomo.domain.entity.FavoritePlaceEntity;
import com.koreatravel.tabitomo.domain.entity.PlaceEntity;

@Service
public class TravelService {
    
    @Autowired
    private PlaceRepository placeRepository;
    
    @Autowired
    private FavoritePlaceRepository favoritePlaceRepository;
    
    public Page<PlaceDTO> getPlaceList(Pageable pageable, String email) {
        return placeRepository.findAll(pageable).map(place -> {
            PlaceDTO placeDTO = PlaceDTO.toPlaceDTO(place);
            placeDTO.setFavorite(isPlaceFavorite(email, placeDTO.getId()));
            return placeDTO;
        });
    }

    public PlaceDTO getPlaceById(String id, String email) {
        PlaceDTO place = placeRepository.findById(id).map(PlaceDTO::toPlaceDTO).orElse(null);
        place.setFavorite(isPlaceFavorite(email, place.getId()));
        return place;
    }

    public void addFavoritePlace(String email, String placeId) {
        favoritePlaceRepository.save(FavoritePlaceEntity.builder()
                .email(email)
                .place_id(placeId)
                .build());
    }

    public void removeFavoritePlace(String email, String placeId) {
        favoritePlaceRepository.deleteByEmailAndPlaceId(email, placeId);
    }

public Page<PlaceDTO> getFavoritePlaceList(Pageable pageable, String email) {
    return favoritePlaceRepository.findByEmail(email, pageable)
        .map(favorite -> {
            // Get the full place details from PlaceRepository
            PlaceEntity place = placeRepository.findById(favorite.getPlace_id())
                .orElseThrow(() -> new RuntimeException("Place not found with id: " + favorite.getPlace_id()));
            
            // Convert to DTO and set favorite status
            PlaceDTO placeDTO = PlaceDTO.toPlaceDTO(place);
            placeDTO.setFavorite(true); // Since it's from favorites, it's definitely a favorite
            return placeDTO;
        });
}

    public boolean isPlaceFavorite(String email, String placeId) {
        return favoritePlaceRepository.existsByEmailAndPlaceId(email, placeId);
    }
}
