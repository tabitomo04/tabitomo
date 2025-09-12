package com.koreatravel.tabitomo.repository.member;

import com.koreatravel.tabitomo.domain.entity.member.LanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Language entity에 대한 데이터베이스 작업을 처리하는 Repository 인터페이스
 */
public interface LanguageRepository extends JpaRepository<LanguageEntity, Integer> {
    
    /**
     * 언어 코드로 언어 정보 조회 (예: ko, en, ja 등)
     *
     * @param languageCode 언어 코드 (ISO 639-1)
     * @return 조회된 언어 엔티티 (Optional)
     */
    Optional<LanguageEntity> findByLanguageCode(String languageCode);
    
    /**
     * 기본 이름으로 언어 정보 조회 (예: 한국어, English, 日本語 등)
     *
     * @param nameNative 언어의 원어 이름
     * @return 조회된 언어 엔티티 (Optional)
     */
    Optional<LanguageEntity> findByNameNative(String nameNative);
    
    /**
     * 영어 이름으로 언어 정보 조회 (예: Korean, English, Japanese 등)
     *
     * @param nameEn 언어의 영어 이름
     * @return 조회된 언어 엔티티 (Optional)
     */
    Optional<LanguageEntity> findByNameEn(String nameEn);
}
