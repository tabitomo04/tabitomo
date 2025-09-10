package com.koreatravel.tabitomo.service.reference;

import com.koreatravel.tabitomo.domain.dto.reference.CountryDTO;
import com.koreatravel.tabitomo.domain.dto.reference.LanguageDTO;
import com.koreatravel.tabitomo.domain.entity.member.CountryEntity;
import com.koreatravel.tabitomo.domain.entity.member.LanguageEntity;
import com.koreatravel.tabitomo.exception.ResourceNotFoundException;
import com.koreatravel.tabitomo.repository.member.CountryRepository;
import com.koreatravel.tabitomo.repository.member.LanguageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReferenceDataServiceImpl implements ReferenceDataService {

    private final CountryRepository countryRepository;
    private final LanguageRepository languageRepository;

    @Override
    public List<CountryDTO> getAllCountries() {
        log.debug("Fetching all countries");
        return countryRepository.findAll().stream()
                .map(this::convertToCountryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CountryDTO getCountryById(Long countryId) {
        log.debug("Fetching country by ID: {}", countryId);
        return countryRepository.findById(countryId)
                .map(this::convertToCountryDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Country not found with id: " + countryId));
    }

    @Override
    public List<LanguageDTO> getAllLanguages() {
        log.debug("Fetching all languages");
        return languageRepository.findAll().stream()
                .map(this::convertToLanguageDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LanguageDTO getLanguageById(Long languageId) {
        log.debug("Fetching language by ID: {}", languageId);
        return languageRepository.findById(languageId)
                .map(this::convertToLanguageDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found with id: " + languageId));
    }

    @Override
    public List<LanguageDTO> getLanguagesByCountryCode(String countryCode) {
        log.debug("Fetching languages for country code: {}", countryCode);
        // Get all languages and filter by country code prefix
        // Note: This is a simplified implementation. In a production environment,
        // you might want to add a country_code column to the language table
        // or create a many-to-many relationship between countries and languages.
        return languageRepository.findAll().stream()
                .filter(lang -> {
                    // Check if language code starts with the country code (case-insensitive)
                    String langCode = lang.getLanguageCode().toLowerCase();
                    return langCode.startsWith(countryCode.toLowerCase() + "-") || 
                           langCode.startsWith(countryCode.toLowerCase() + "_");
                })
                .map(this::convertToLanguageDTO)
                .collect(Collectors.toList());
    }

    private CountryDTO convertToCountryDTO(CountryEntity entity) {
        return CountryDTO.builder()
                .countryId(entity.getCountryId())
                .countryName(entity.getCountryName())
                .countryCode(entity.getCountryCode())
                .isoCode(entity.getIsoCode())
                .region(entity.getRegion())
                .build();
    }

    private LanguageDTO convertToLanguageDTO(LanguageEntity entity) {
        return LanguageDTO.builder()
                .languageId(entity.getLanguageId())
                .languageCode(entity.getLanguageCode())
                .nameNative(entity.getNameNative())
                .nameEn(entity.getNameEn())
                .build();
    }
}
