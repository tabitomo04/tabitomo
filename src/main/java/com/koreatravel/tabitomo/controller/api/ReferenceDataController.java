package com.koreatravel.tabitomo.controller.api;

import com.koreatravel.tabitomo.domain.dto.reference.CountryDTO;
import com.koreatravel.tabitomo.domain.dto.reference.LanguageDTO;
import com.koreatravel.tabitomo.service.reference.ReferenceDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing reference data like countries and languages
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReferenceDataController {

    private final ReferenceDataService referenceDataService;

    /**
     * Get all countries
     * @return List of all countries
     */
    @GetMapping("/countries")
    public ResponseEntity<List<CountryDTO>> getAllCountries() {
        return ResponseEntity.ok(referenceDataService.getAllCountries());
    }

    /**
     * Get country by ID
     * @param countryId Country ID
     * @return Country details
     */
    @GetMapping("/countries/{countryId}")
    public ResponseEntity<CountryDTO> getCountryById(@PathVariable Long countryId) {
        return ResponseEntity.ok(referenceDataService.getCountryById(countryId));
    }

    /**
     * Get all languages
     * @return List of all languages
     */
    @GetMapping("/languages")
    public ResponseEntity<List<LanguageDTO>> getAllLanguages() {
        return ResponseEntity.ok(referenceDataService.getAllLanguages());
    }

    /**
     * Get language by ID
     * @param languageId Language ID
     * @return Language details
     */
    @GetMapping("/languages/{languageId}")
    public ResponseEntity<LanguageDTO> getLanguageById(@PathVariable Long languageId) {
        return ResponseEntity.ok(referenceDataService.getLanguageById(languageId));
    }

    /**
     * Get languages by country code
     * @param countryCode ISO 3166-1 alpha-2 country code (e.g., "US", "KR")
     * @return List of languages for the specified country
     */
    @GetMapping("/languages/country/{countryCode}")
    public ResponseEntity<List<LanguageDTO>> getLanguagesByCountryCode(
            @PathVariable String countryCode) {
        return ResponseEntity.ok(referenceDataService.getLanguagesByCountryCode(countryCode));
    }

}
