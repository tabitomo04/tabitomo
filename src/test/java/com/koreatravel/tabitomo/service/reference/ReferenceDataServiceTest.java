package com.koreatravel.tabitomo.service.reference;

import com.koreatravel.tabitomo.domain.dto.reference.CountryDTO;
import com.koreatravel.tabitomo.domain.dto.reference.LanguageDTO;
import com.koreatravel.tabitomo.domain.entity.member.CountryEntity;
import com.koreatravel.tabitomo.domain.entity.member.LanguageEntity;
import com.koreatravel.tabitomo.exception.ResourceNotFoundException;
import com.koreatravel.tabitomo.repository.member.CountryRepository;
import com.koreatravel.tabitomo.repository.member.LanguageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReferenceDataServiceTest {

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private LanguageRepository languageRepository;

    @InjectMocks
    private ReferenceDataServiceImpl referenceDataService;

    private CountryEntity testCountry;
    private LanguageEntity testLanguage;

    @BeforeEach
    void setUp() {
        testCountry = CountryEntity.builder()
                .countryId(1L)
                .countryCode("KR")
                .countryName("South Korea")
                .isoCode("KOR")
                .region("Asia")
                .build();

        testLanguage = LanguageEntity.builder()
                .languageId(1L)
                .languageCode("ko-KR")
                .nameNative("한국어")
                .nameEn("Korean")
                .build();
    }

    @Test
    void getAllCountries_ShouldReturnListOfCountries() {
        // Given
        when(countryRepository.findAll()).thenReturn(Arrays.asList(testCountry));

        // When
        List<CountryDTO> result = referenceDataService.getAllCountries();

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("South Korea", result.get(0).getCountryName());
        
        verify(countryRepository, times(1)).findAll();
    }

    @Test
    void getCountryById_WithValidId_ShouldReturnCountry() {
        // Given
        when(countryRepository.findById(1L)).thenReturn(Optional.of(testCountry));

        // When
        CountryDTO result = referenceDataService.getCountryById(1L);

        // Then
        assertNotNull(result);
        assertEquals("South Korea", result.getCountryName());
        
        verify(countryRepository, times(1)).findById(1L);
    }

    @Test
    void getCountryById_WithInvalidId_ShouldThrowException() {
        // Given
        when(countryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            referenceDataService.getCountryById(999L);
        });
        
        verify(countryRepository, times(1)).findById(999L);
    }

    @Test
    void getAllLanguages_ShouldReturnListOfLanguages() {
        // Given
        when(languageRepository.findAll()).thenReturn(Arrays.asList(testLanguage));

        // When
        List<LanguageDTO> result = referenceDataService.getAllLanguages();

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Korean", result.get(0).getNameEn());
        
        verify(languageRepository, times(1)).findAll();
    }

    @Test
    void getLanguageById_WithValidId_ShouldReturnLanguage() {
        // Given
        when(languageRepository.findById(1L)).thenReturn(Optional.of(testLanguage));

        // When
        LanguageDTO result = referenceDataService.getLanguageById(1L);

        // Then
        assertNotNull(result);
        assertEquals("Korean", result.getNameEn());
        
        verify(languageRepository, times(1)).findById(1L);
    }

    @Test
    void getLanguagesByCountryCode_ShouldFilterLanguages() {
        // Given
        LanguageEntity englishUS = LanguageEntity.builder()
                .languageId(2L)
                .languageCode("en-US")
                .nameNative("English (US)")
                .nameEn("English (US)")
                .build();
                
        when(languageRepository.findAll()).thenReturn(Arrays.asList(testLanguage, englishUS));

        // When
        List<LanguageDTO> result = referenceDataService.getLanguagesByCountryCode("KR");

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Korean", result.get(0).getNameEn());
        
        verify(languageRepository, times(1)).findAll();
    }
}
