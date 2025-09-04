package com.koreatravel.tabitomo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.koreatravel.tabitomo.repository.PlaceRepository;
import com.koreatravel.tabitomo.domain.dto.PlaceDTO;

@Service
public class TravelService {
    
    @Autowired
    private PlaceRepository placeRepository;
    
    public Page<PlaceDTO> getPlaceList(Pageable pageable) {
        return placeRepository.findAll(pageable).map(PlaceDTO::toPlaceDTO);
    }

    public PlaceDTO getPlaceById(String id) {
        return placeRepository.findById(id).map(PlaceDTO::toPlaceDTO).orElse(null);
    }
}
