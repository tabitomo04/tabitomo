package com.koreatravel.tabitomo.repository.trip;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.koreatravel.tabitomo.domain.entity.trip.CitiesEntity;

public interface CitiesRepository extends JpaRepository<CitiesEntity, Integer> {
    @Query(value = "with a as " +
            "(select f.place_id as id, p.region, p.address from favorite_place as f left join place as p on f.place_id = p.place_id union "
            +
            "SELECT s.place_id as id, p.region, p.address from place as p join schedule as s on p.place_id = s.place_id), "
            +
            "b as (select type, a.region, city from cities as i join a on a.region = i.region having type != '도'), " +
            "c as (select type, f.region, f.city from (select region, SUBSTRING_INDEX(SUBSTRING_INDEX(address , ' ', 2), ' ', -1) AS city from a) as f "
            +
            "join cities as i on f.region = i.region and f.city = i.city having type = '도') " +
            "select id, cit.type, cit.region, cit.city, description, image_url from cities as cit right join (select type, region, city, count(*) as recommend from b group by region, city, type union select type, region, city, count(*) as recommend from c group by city, region, type order by recommend desc, region limit 3) "
            +
            "as d on cit.region = d.region and cit.city = d.city", nativeQuery = true)
    List<CitiesEntity> findRecommendCities();

    List<CitiesEntity> findAll();

    Optional<CitiesEntity> findById(Integer id);
}
