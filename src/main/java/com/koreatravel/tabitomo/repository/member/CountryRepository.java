package com.koreatravel.tabitomo.repository.member;

import com.koreatravel.tabitomo.domain.entity.member.CountryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<CountryEntity, Integer> {
    List<CountryEntity> findAllByOrderByNameKrAsc();

    CountryEntity findById(int id);
    
    Optional<CountryEntity> findByCountryCode(String countryCode);
    
    Optional<CountryEntity> findByNameKr(String nameKr);
}