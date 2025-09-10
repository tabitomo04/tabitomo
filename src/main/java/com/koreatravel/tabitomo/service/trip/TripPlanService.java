package com.koreatravel.tabitomo.service.trip;

import com.koreatravel.tabitomo.domain.dto.trip.TripPlanDTO;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.trip.PlaceEntity;
import com.koreatravel.tabitomo.domain.entity.trip.ScheduleEntity;
import com.koreatravel.tabitomo.domain.entity.trip.TripEntity;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import com.koreatravel.tabitomo.repository.trip.PlaceRepository;
import com.koreatravel.tabitomo.repository.trip.ScheduleRepository;
import com.koreatravel.tabitomo.repository.trip.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TripPlanService {

    private final MemberRepository memberRepository;
    private final TripRepository tripRepository;
    private final PlaceRepository placeRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional
    public Long saveTripPlan(com.koreatravel.tabitomo.domain.dto.trip.TripPlanDTO tripPlanDTO, String email) {
        log.info("Saving trip plan for user: {}", email);
        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다: " + email));

        // 1. Find or create the accommodation Place entity using the rich data from the DTO
        PlaceEntity accommodationPlace = null;
        if (tripPlanDTO.getAccommodation() != null) {
            accommodationPlace = findOrCreatePlaceFromAccommodationDto(tripPlanDTO.getAccommodation());
        }

        // 2. Build and save the Trip entity with the accommodation
        String tripTitle = (tripPlanDTO.getDestination() != null && !tripPlanDTO.getDestination().isEmpty())
                ? tripPlanDTO.getDestination() + " 여행"
                : "새로운 여행 계획";

        TripEntity trip = TripEntity.builder()
                .member(member)
                .title(tripTitle)
                .startDate(tripPlanDTO.getStartDate())
                .endDate(tripPlanDTO.getEndDate())
                .accommodation(accommodationPlace)
                .visibility("PRIVATE")
                .build();
        tripRepository.save(trip);

        // 3. Save schedules
        if (tripPlanDTO.getDailySchedules() != null) {
            for (TripPlanDTO.DailySchedule dailySchedule : tripPlanDTO.getDailySchedules()) {
                for (TripPlanDTO.ScheduleItem item : dailySchedule.getSchedules()) {
                    PlaceEntity place = findOrCreatePlaceFromScheduleItem(item);

                    ScheduleEntity schedule = ScheduleEntity.builder()
                            .trip(trip)
                            .place(place)
                            .day(dailySchedule.getDay())
                            .startTime(item.getStartTime())
                            .endTime(item.getEndTime() != null ? item.getEndTime() : "") // Use end time if available
                            .memo(item.getDescription())
                            .build();
                    scheduleRepository.save(schedule);
                }
            }
        }

        return trip.getId();
    }

    private PlaceEntity findOrCreatePlaceFromAccommodationDto(
            com.koreatravel.tabitomo.domain.dto.trip.TripPlanDTO.Accommodation accDto) {
        return placeRepository.findByName(accDto.getPlaceName())
                .orElseGet(() -> {
                    PlaceEntity newPlace = PlaceEntity.builder()
                            .name(accDto.getPlaceName())
                            .description(accDto.getDescription())
                            .imageUrl(accDto.getImageUrl())
                            .address(accDto.getAddress() != null ? accDto.getAddress() : "")
                            .latitude(accDto.getLatitude() != null ? accDto.getLatitude() : 0.0)
                            .longitude(accDto.getLongitude() != null ? accDto.getLongitude() : 0.0)
                            .categoryCode("ACCOMMODATION")
                            .build();
                    return placeRepository.save(newPlace);
                });
    }

    private PlaceEntity findOrCreatePlaceFromScheduleItem(TripPlanDTO.ScheduleItem itemDto) {
        return placeRepository.findByName(itemDto.getPlace())
                .orElseGet(() -> {
                    PlaceEntity newPlace = PlaceEntity.builder()
                            .name(itemDto.getPlace())
                            .description(itemDto.getDescription() != null ? itemDto.getDescription() : "")
                            .latitude(itemDto.getLatitude())
                            .longitude(itemDto.getLongitude())
                            .categoryCode("ATTRACTION") // Default category
                            .build();
                    return placeRepository.save(newPlace);
                });
    }
}
