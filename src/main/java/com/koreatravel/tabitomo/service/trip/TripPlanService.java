package vio.tabitomo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vio.tabitomo.domain.dto.trip.TripPlan;
import vio.tabitomo.domain.entity.Member;
import vio.tabitomo.domain.entity.Place;
import vio.tabitomo.domain.entity.Schedule;
import vio.tabitomo.domain.entity.Trip;
import vio.tabitomo.repository.MemberRepository;
import vio.tabitomo.repository.PlaceRepository;
import vio.tabitomo.repository.ScheduleRepository;
import vio.tabitomo.repository.TripRepository;

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
    public Long saveTripPlan(TripPlan tripPlan, String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다: " + username));

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
    public void updateTripPlan(TripPlan updatedTripPlan, String username) {
        log.info("Updating trip plan with ID: {}", updatedTripPlan.getId());
        Trip trip = tripRepository.findById(updatedTripPlan.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 여행을 찾을 수 없습니다."));

        if (!trip.getMember().getUsername().equals(username)) {
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
    public List<Trip> findTripsByUsername(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다: " + username));
        return tripRepository.findByMemberId(member.getId());
    }

    @Transactional(readOnly = true)
    public Optional<Trip> findTripByIdAndUsername(Long tripId, String username) {
        return tripRepository.findByIdWithMember(tripId)
                .filter(trip -> trip.getMember().getUsername().equals(username));
    }

    @Transactional(readOnly = true)
    public List<Schedule> findSchedulesByTripId(Long tripId) {
        return scheduleRepository.findByTripIdWithPlace(tripId);
    }
}
