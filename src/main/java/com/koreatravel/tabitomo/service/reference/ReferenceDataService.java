package com.koreatravel.tabitomo.service.reference;

import com.koreatravel.tabitomo.domain.dto.reference.CountryDTO;
import com.koreatravel.tabitomo.domain.dto.reference.LanguageDTO;

import java.util.List;

/**
 * Reference data service for managing countries and languages
 */
public interface ReferenceDataService {
    
    /**
     * Get all countries
     * @return List of CountryDTO
     */
    List<CountryDTO> getAllCountries();
    
    /**
     * Get country by ID
     * @param countryId Country ID
     * @return CountryDTO
     */
    CountryDTO getCountryById(Long countryId);
    
    /**
     * Get all languages
     * @return List of LanguageDTO
     */
    List<LanguageDTO> getAllLanguages();
    
    /**
     * Get language by ID
     * @param languageId Language ID
     * @return LanguageDTO
     */
    LanguageDTO getLanguageById(Long languageId);
    
    /**
     * Get languages by country code
     * @param countryCode ISO 3166-1 alpha-2 country code
     * @return List of LanguageDTO
     */
    List<LanguageDTO> getLanguagesByCountryCode(String countryCode);
}
