package com.koreatravel.tabitomo.repository.member;

import com.koreatravel.tabitomo.domain.entity.member.CountryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Country entity에 대한 데이터베이스 작업을 처리하는 Repository 인터페이스
 */
public interface CountryRepository extends JpaRepository<CountryEntity, Integer> {
    
    /**
     * ISO 코드로 국가 정보 조회
     *
     * @param isoCode 국가 ISO 코드 (예: KOR, USA, JPN 등)
     * @return 조회된 국가 엔티티 (Optional)
     */
    Optional<CountryEntity> findByIsoCode(String isoCode);
    
    /**
     * 국가명으로 국가 정보 조회
     *
     * @param countryName 국가명 (예: 대한민국, United States, Japan 등)
     * @return 조회된 국가 엔티티 (Optional)
     */
    Optional<CountryEntity> findByCountryName(String countryName);
}
