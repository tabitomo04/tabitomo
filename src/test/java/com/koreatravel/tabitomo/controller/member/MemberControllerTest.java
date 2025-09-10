package com.koreatravel.tabitomo.controller.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koreatravel.tabitomo.domain.dto.member.MemberFormDTO;
import com.koreatravel.tabitomo.domain.entity.member.CountryEntity;
import com.koreatravel.tabitomo.domain.entity.member.LanguageEntity;
import com.koreatravel.tabitomo.repository.member.CountryRepository;
import com.koreatravel.tabitomo.repository.member.LanguageRepository;
import com.koreatravel.tabitomo.service.member.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private MemberService memberService;

    @MockBean
    private CountryRepository countryRepository;

    @MockBean
    private LanguageRepository languageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        // Mock country and language data
        CountryEntity country = new CountryEntity();
        country.setCountryId(1L);
        country.setCountryName("대한민국");
        country.setCountryCode("KR");
        country.setIsoCode("KOR");
        country.setRegion("Asia");

        LanguageEntity language = new LanguageEntity();
        language.setLanguageId(1L);
        language.setLanguageCode("ko");
        language.setNameNative("한국어");
        language.setNameEn("Korean");

        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(languageRepository.findById(1L)).thenReturn(Optional.of(language));
    }

    @Test
    void signup_WithValidData_ShouldRedirectToLogin() throws Exception {
        // Given
        MemberFormDTO formDTO = MemberFormDTO.builder()
                .email("test@example.com")
                .password("Password1!")
                .passwordConfirm("Password1!")
                .nickname("testuser")
                .gender(1)
                .countryId(1L)
                .preferredLanguageId(1L)
                .build();

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/member/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", formDTO.getEmail())
                        .param("password", formDTO.getPassword())
                        .param("passwordConfirm", formDTO.getPasswordConfirm())
                        .param("nickname", formDTO.getNickname())
                        .param("gender", String.valueOf(formDTO.getGender()))
                        .param("countryId", String.valueOf(formDTO.getCountryId()))
                        .param("preferredLanguageId", String.valueOf(formDTO.getPreferredLanguageId())))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/member/login"));
    }

    @Test
    void signup_WithInvalidData_ShouldReturnToSignup() throws Exception {
        // Given - Missing required fields
        MemberFormDTO formDTO = new MemberFormDTO();
        formDTO.setEmail("invalid-email");
        formDTO.setPassword("short");
        formDTO.setPasswordConfirm("mismatch");

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/member/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", formDTO.getEmail())
                        .param("password", formDTO.getPassword())
                        .param("passwordConfirm", formDTO.getPasswordConfirm()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("auth/signup"));
    }
}
