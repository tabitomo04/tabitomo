package com.koreatravel.tabitomo.service.trip;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.koreatravel.tabitomo.domain.dto.trip.TripPlan;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.trip.Place;
import com.koreatravel.tabitomo.domain.entity.trip.Schedule;
import com.koreatravel.tabitomo.domain.entity.trip.Trip;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import com.koreatravel.tabitomo.repository.trip.PlaceRepository;
import com.koreatravel.tabitomo.repository.trip.ScheduleRepository;
import com.koreatravel.tabitomo.repository.trip.TripRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TripPlanService {

    private final MemberRepository memberRepository;
    private final TripRepository tripRepository;
    private final PlaceRepository placeRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional
    public UUID saveTripPlan(TripPlan tripPlan, UUID memberId) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다: " + memberId));

        Place accommodationPlace = null;
        if (tripPlan.getAccommodation() != null && tripPlan.getAccommodation().getPlaceName() != null) {
            accommodationPlace = findOrCreatePlaceFromDto(tripPlan.getAccommodation());
        }

        String tripTitle = (tripPlan.getPlanName() != null && !tripPlan.getPlanName().isEmpty())
                         ? tripPlan.getPlanName()
                         : tripPlan.getDestination() + " 여행";

        Trip trip = Trip.builder()
                .member(member)
                .title(tripTitle)
                .startDate(tripPlan.getStartDate())
                .endDate(tripPlan.getEndDate())
                .accommodation(accommodationPlace)
                .visibility("PRIVATE")
                .build();
        tripRepository.save(trip);

        if (tripPlan.getDailySchedules() != null) {
            for (TripPlan.DailySchedule dailySchedule : tripPlan.getDailySchedules()) {
                for (TripPlan.ScheduleItem item : dailySchedule.getSchedules()) {
                    Place place = findOrCreatePlaceFromDto(item);
                    Schedule schedule = Schedule.builder()
                            .trip(trip)
                            .place(place)
                            .day(dailySchedule.getDay())
                            .startTime(item.getStartTime())
                            .endTime(item.getEndTime())
                            .memo(item.getDescription())
                            .build();
                    scheduleRepository.save(schedule);
                }
            }
        }

        return trip.getId();
    }

    @Transactional
    public void updateTripPlan(TripPlan updatedTripPlan, UUID memberId) {
        log.info("Updating trip plan with ID: {}", updatedTripPlan.getId());
        Trip trip = tripRepository.findByIdAndMemberId(updatedTripPlan.getId(), memberId)
                .orElseThrow(() -> new SecurityException("해당 여행을 찾을 수 없거나 수정 권한이 없습니다."));

        trip.updateTitle(updatedTripPlan.getPlanName());

        trip.getSchedules().clear();

        if (updatedTripPlan.getDailySchedules() != null) {
            for (TripPlan.DailySchedule dailySchedule : updatedTripPlan.getDailySchedules()) {
                for (TripPlan.ScheduleItem item : dailySchedule.getSchedules()) {
                    Place place = findOrCreatePlaceFromDto(item);

                    Schedule newSchedule = Schedule.builder()
                            .trip(trip)
                            .place(place)
                            .day(dailySchedule.getDay())
                            .startTime(item.getStartTime())
                            .endTime(item.getEndTime())
                            .memo(item.getDescription())
                            .build();
                    trip.getSchedules().add(newSchedule);
                }
            }
        }
        
        log.info("Successfully updated trip plan with ID: {}", trip.getId());
    }

    private Place findOrCreatePlaceFromDto(TripPlan.Accommodation accDto) {
        return placeRepository.findByName(accDto.getPlaceName())
                .orElseGet(() -> {
                    Place newPlace = Place.builder()
                            .name(accDto.getPlaceName())
                            .description(accDto.getDescription())
                            .priceRange(accDto.getPriceRange())
                            .imageUrl(accDto.getImageUrl())
                            .address(accDto.getAddress())
                            .latitude(accDto.getLatitude())
                            .longitude(accDto.getLongitude())
                            .categoryCode("ACCOMMODATION")
                            .build();
                    return placeRepository.save(newPlace);
                });
    }

    private Place findOrCreatePlaceFromDto(TripPlan.ScheduleItem itemDto) {
        return placeRepository.findByName(itemDto.getPlace())
                .orElseGet(() -> {
                    Place newPlace = Place.builder()
                            .name(itemDto.getPlace())
                            .description(itemDto.getDescription())
                            .address(itemDto.getAddress())
                            .latitude(itemDto.getLatitude())
                            .longitude(itemDto.getLongitude())
                            .build();
                    return placeRepository.save(newPlace);
                });
    }

    @Transactional(readOnly = true)
    public List<Trip> findTripsByUserId(UUID userId) {
        return tripRepository.findByMemberId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Trip> findTripByIdAndUserId(UUID tripId, UUID userId) {
        return tripRepository.findByIdAndMemberId(tripId, userId);
    }

    @Transactional(readOnly = true)
    public List<Schedule> findSchedulesByTripId(UUID tripId) {
        return scheduleRepository.findByTripIdWithPlace(tripId);
    }
}
