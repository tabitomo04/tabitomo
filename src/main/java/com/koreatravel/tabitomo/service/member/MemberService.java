package com.koreatravel.tabitomo.service.member;

import com.google.api.services.translate.Translate.Detections.List;
import com.koreatravel.tabitomo.domain.dto.member.CountryDTO;
import com.koreatravel.tabitomo.domain.dto.member.LanguageDTO;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.test.LanguageEntity;
import com.koreatravel.tabitomo.domain.entity.member.CountryEntity;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import com.koreatravel.tabitomo.repository.member.CountryRepository;
import com.koreatravel.tabitomo.repository.member.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {
    @Autowired
    private final MemberRepository memberRepository;

    @Autowired
    private final CountryRepository countryRepository;
    
    @Autowired
    private final LanguageRepository languageRepository;
    
    public MemberEntity findById(UUID id) {
        return memberRepository.findById(id).orElse(null);
    }

    public CountryDTO findCountryById(int id) {
        CountryEntity countryEntity = countryRepository.findById(id);
        return CountryDTO.builder()
                .countryId(countryEntity.getCountryId())
                .countryName(countryEntity.getCountryName())
                .build();
    }

    public LanguageDTO findLanguageById(int id) {
        LanguageEntity languageEntity = languageRepository.findById(id);
        return LanguageDTO.builder()
                .languageId(languageEntity.getLanguageId())
                .languageName(languageEntity.getLanguageName())
                .languageNative(languageEntity.getLanguageNative())
                .build();
    }

    public List<CountryDTO> getAllCountries() {
        return countryRepository.findAll();
    }

    public List<LanguageDTO> getAllLanguages() {
        return languageRepository.findAll();
    }
}

