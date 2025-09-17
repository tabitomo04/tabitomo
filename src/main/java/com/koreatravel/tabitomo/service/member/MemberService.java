package com.koreatravel.tabitomo.service.member;

import com.koreatravel.tabitomo.domain.dto.member.CountryDTO;
import com.koreatravel.tabitomo.domain.dto.member.LanguageDTO;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import com.koreatravel.tabitomo.repository.member.CountryRepository;
import com.koreatravel.tabitomo.repository.member.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;


@Service
@RequiredArgsConstructor
public class MemberService {
    @Autowired
    private final MemberRepository memberRepository;

    @Autowired
    private final CountryRepository countryRepository;
    
    @Autowired
    private final LanguageRepository languageRepository;
    
    public MemberEntity findById(Long id) {
        return memberRepository.findById(id).orElse(null);
    }

    public java.util.List<CountryDTO> getAllCountries() {
        return countryRepository.findAllByOrderByCountryNameAsc().stream()
                .map(country -> new CountryDTO(country.getCountryId(), country.getCountryName(), country.getRegion()))
                .toList();
    }

    public java.util.List<LanguageDTO> getAllLanguages() {
        return languageRepository.findAllActiveLanguages().stream()
                .map(language -> new LanguageDTO(language.getLanguageId(), language.getNameNative(), language.getNameEn()))
                .toList();
    }
}

