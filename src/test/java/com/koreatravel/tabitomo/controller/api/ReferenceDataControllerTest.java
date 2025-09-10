package com.koreatravel.tabitomo.controller.api;

import com.koreatravel.tabitomo.domain.dto.reference.CountryDTO;
import com.koreatravel.tabitomo.domain.dto.reference.LanguageDTO;
import com.koreatravel.tabitomo.domain.entity.member.CountryEntity;
import com.koreatravel.tabitomo.domain.entity.member.LanguageEntity;
import com.koreatravel.tabitomo.repository.member.CountryRepository;
import com.koreatravel.tabitomo.repository.member.LanguageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReferenceDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CountryRepository countryRepository;

    @MockBean
    private LanguageRepository languageRepository;

    private List<CountryEntity> testCountries;
    private List<LanguageEntity> testLanguages;

    @BeforeEach
    void setUp() {
        // Setup test data
        CountryEntity korea = new CountryEntity();
        korea.setCountryId(1L);
        korea.setCountryName("대한민국");
        korea.setCountryCode("KR");
        korea.setIsoCode("KOR");
        korea.setRegion("Asia");

        CountryEntity usa = new CountryEntity();
        usa.setCountryId(2L);
        usa.setCountryName("United States");
        usa.setCountryCode("US");
        usa.setIsoCode("USA");
        usa.setRegion("North America");

        testCountries = Arrays.asList(korea, usa);

        // Setup test languages
        LanguageEntity korean = new LanguageEntity();
        korean.setLanguageId(1L);
        korean.setLanguageCode("ko");
        korean.setNameNative("한국어");
        korean.setNameEn("Korean");

        LanguageEntity english = new LanguageEntity();
        english.setLanguageId(2L);
        english.setLanguageCode("en");
        english.setNameNative("English");
        english.setNameEn("English");

        testLanguages = Arrays.asList(korean, english);
    }

    @Test
    void getAllCountries_ShouldReturnCountries() throws Exception {
        // Given
        when(countryRepository.findAll()).thenReturn(testCountries);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].countryId", is(1)))
                .andExpect(jsonPath("$[0].countryName", is("대한민국")))
                .andExpect(jsonPath("$[1].countryId", is(2)))
                .andExpect(jsonPath("$[1].countryName", is("United States")));
    }

    @Test
    void getAllLanguages_ShouldReturnLanguages() throws Exception {
        // Given
        when(languageRepository.findAll()).thenReturn(testLanguages);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/languages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].languageId", is(1)))
                .andExpect(jsonPath("$[0].nameNative", is("한국어")))
                .andExpect(jsonPath("$[1].languageId", is(2)))
                .andExpect(jsonPath("$[1].nameNative", is("English")));
    }
}
