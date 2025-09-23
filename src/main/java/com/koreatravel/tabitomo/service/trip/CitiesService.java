package com.koreatravel.tabitomo.service.trip;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.koreatravel.tabitomo.domain.dto.trip.CitiesDTO;
import com.koreatravel.tabitomo.repository.trip.CitiesRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitiesService {
    private final CitiesRepository citiesRepository;

    /**
     * 모든 도시 정보를 조회합니다.
     * @return 도시 정보 DTO 리스트 (비어있을 수 있음)
     */
    public List<CitiesDTO> getAllCities() {
        try {
            return citiesRepository.findAll().stream()
                    .map(CitiesDTO::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch all cities", e);
            return Collections.emptyList();
        }
    }

    /**
     * ID로 도시 정보를 조회합니다.
     * @param id 조회할 도시 ID
     * @return 도시 정보 DTO (존재하지 않을 경우 Optional.empty())
     */
    public Optional<CitiesDTO> getCityById(Integer id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        
        try {
            return citiesRepository.findById(id)
                    .map(CitiesDTO::new);
        } catch (Exception e) {
            log.error("Failed to fetch city by id: {}", id, e);
            return Optional.empty();
        }
    }

    /**
     * 추천 도시 목록을 조회합니다.
     * @return 추천 도시 정보 DTO 리스트 (비어있을 수 있음)
     */
    public List<CitiesDTO> getRecommendCities() {
        try {
            return citiesRepository.findRecommendCities().stream()
                    .map(CitiesDTO::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to fetch recommended cities", e);
            return Collections.emptyList();
        }
    }
}
