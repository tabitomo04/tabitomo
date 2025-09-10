package com.koreatravel.tabitomo.config.data;

import com.koreatravel.tabitomo.domain.entity.member.CountryEntity;
import com.koreatravel.tabitomo.domain.entity.member.LanguageEntity;
import com.koreatravel.tabitomo.repository.member.CountryRepository;
import com.koreatravel.tabitomo.repository.member.LanguageRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class InitialDataLoader {

    private final CountryRepository countryRepository;
    private final LanguageRepository languageRepository;

    @PostConstruct
    @Transactional
    public void init() {
        // Only load data if database is empty
        if (countryRepository.count() == 0) {
            loadCountries();
        }
        
        if (languageRepository.count() == 0) {
            loadLanguages();
        }
    }

    private void loadCountries() {
        List<CountryEntity> countries = Arrays.asList(
                createCountry("KR", "대한민국", "Asia", "KOR"),
                createCountry("US", "미국", "North America", "USA"),
                createCountry("JP", "일본", "Asia", "JPN"),
                createCountry("CN", "중국", "Asia", "CHN"),
                createCountry("GB", "영국", "Europe", "GBR"),
                createCountry("FR", "프랑스", "Europe", "FRA"),
                createCountry("DE", "독일", "Europe", "DEU"),
                createCountry("CA", "캐나다", "North America", "CAN"),
                createCountry("AU", "호주", "Oceania", "AUS"),
                createCountry("SG", "싱가포르", "Asia", "SGP")
        );

        countryRepository.saveAll(countries);
    }

    private CountryEntity createCountry(String code, String name, String region, String isoCode) {
        return CountryEntity.builder()
                .countryCode(code)
                .countryName(name)
                .region(region)
                .isoCode(isoCode)
                .build();
    }

    private void loadLanguages() {
        List<LanguageEntity> languages = Arrays.asList(
                createLanguage("ko", "한국어", "Korean"),
                createLanguage("en", "English", "English"),
                createLanguage("ja", "日本語", "Japanese"),
                createLanguage("zh-CN", "简体中文", "Simplified Chinese"),
                createLanguage("zh-TW", "繁體中文", "Traditional Chinese"),
                createLanguage("es", "Español", "Spanish"),
                createLanguage("fr", "Français", "French"),
                createLanguage("de", "Deutsch", "German"),
                createLanguage("ru", "Русский", "Russian"),
                createLanguage("th", "ไทย", "Thai")
        );

        languageRepository.saveAll(languages);
    }

    private LanguageEntity createLanguage(String code, String nameNative, String nameEn) {
        return LanguageEntity.builder()
                .languageCode(code)
                .nameNative(nameNative)
                .nameEn(nameEn)
                .build();
    }
}
