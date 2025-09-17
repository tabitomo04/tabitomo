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
    public Long saveTripPlan(TripPlan tripPlan, Long id) {
        MemberEntity member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다: " + id));

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
    public void updateTripPlan(TripPlan updatedTripPlan, Long id) {
        log.info("Updating trip plan with ID: {}", updatedTripPlan.getId());
        Trip trip = tripRepository.findById(updatedTripPlan.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 여행을 찾을 수 없습니다."));

        if (!trip.getMember().getId().equals(id)) {
            throw new SecurityException("이 여행 계획을 수정할 권한이 없습니다.");
        }

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
    public List<Trip> findTripsByUserId(Long id) {
        return tripRepository.findByMemberId(id);
    }

    @Transactional(readOnly = true)
    public Optional<Trip> findTripByIdAndUserId(Long tripId, Long id) {
        return tripRepository.findByIdWithMember(tripId)
                .filter(trip -> trip.getMember().getId().equals(id));
    }

    @Transactional(readOnly = true)
    public List<Schedule> findSchedulesByTripId(Long tripId) {
        return scheduleRepository.findByTripIdWithPlace(tripId);
    }
}
