package com.koreatravel.tabitomo.service.trip;

import com.koreatravel.tabitomo.domain.dto.trip.TripPlanDTO;
import com.koreatravel.tabitomo.domain.entity.member.MemberEntity;
import com.koreatravel.tabitomo.domain.entity.trip.Place;
import com.koreatravel.tabitomo.domain.entity.trip.Schedule;
import com.koreatravel.tabitomo.domain.entity.trip.Trip;
import com.koreatravel.tabitomo.domain.entity.trip.TripPlan;
import com.koreatravel.tabitomo.repository.member.MemberRepository;
import com.koreatravel.tabitomo.repository.trip.PlaceRepository;
import com.koreatravel.tabitomo.repository.trip.ScheduleRepository;
import com.koreatravel.tabitomo.repository.trip.TripPlanRepository;
import com.koreatravel.tabitomo.repository.trip.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final TripPlanRepository tripPlanRepository;

    @Transactional
    public Long saveTripPlan(TripPlanDTO tripPlan, UUID memberId) {
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
            for (TripPlanDTO.DailySchedule dailySchedule : tripPlan.getDailySchedules()) {
                for (TripPlanDTO.ScheduleItem item : dailySchedule.getSchedules()) {
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
    public void updateTripPlan(TripPlanDTO updatedTripPlan, UUID memberId) {
        log.info("Updating trip plan with ID: {}", updatedTripPlan.getId());
        Trip trip = tripRepository.findById(updatedTripPlan.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 여행을 찾을 수 없습니다."));

        if (!trip.getMember().getId().equals(memberId)) {
            throw new SecurityException("이 여행 계획을 수정할 권한이 없습니다.");
        }

        trip.updateTitle(updatedTripPlan.getPlanName());

        trip.getSchedules().clear();

        if (updatedTripPlan.getDailySchedules() != null) {
            for (TripPlanDTO.DailySchedule dailySchedule : updatedTripPlan.getDailySchedules()) {
                for (TripPlanDTO.ScheduleItem item : dailySchedule.getSchedules()) {
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

    @Transactional
    public void deleteTripPlan(Long tripId, UUID memberId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 계획을 찾을 수 없습니다."));

        if (!trip.getMember().getId().equals(memberId)) {
            throw new SecurityException("이 여행 계획을 삭제할 권한이 없습니다.");
        }

        tripRepository.delete(trip);
    }

    @Transactional
    public void deleteTripPlanByAdmin(Long tripPlanId) {
        TripPlan tripPlan = tripPlanRepository.findById(tripPlanId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 계획을 찾을 수 없습니다: " + tripPlanId));
        tripPlanRepository.delete(tripPlan);
    }

    @Transactional
    public void toggleTripVisibility(Long tripId, UUID memberId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 계획을 찾을 수 없습니다."));

        if (!trip.getMember().getId().equals(memberId)) {
            throw new SecurityException("이 여행 계획의 공개 상태를 변경할 권한이 없습니다.");
        }

        trip.toggleVisibility();
    }

    private Place findOrCreatePlaceFromDto(TripPlanDTO.Accommodation accDto) {
        return placeRepository.findFirstByName(accDto.getPlaceName())
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

    private Place findOrCreatePlaceFromDto(TripPlanDTO.ScheduleItem itemDto) {
        return placeRepository.findFirstByName(itemDto.getPlace())
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
    public List<Trip> findTripsByEmail(String email) {
        return tripRepository.findByMemberEmail(email);
    }

    @Transactional(readOnly = true)
    public Page<Trip> findTripsByEmail(String email, Pageable pageable) {
        return tripRepository.findByMemberEmail(email, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Trip> findTripById(Long tripId) {
        return tripRepository.findById(tripId);
    }

    @Transactional(readOnly = true)
    public Optional<Trip> findTripByIdAndMemberId(Long tripId, UUID memberId) {
        return tripRepository.findById(tripId)
                .filter(trip -> trip.getMember().getId().equals(memberId));
    }

    @Transactional(readOnly = true)
    public List<Schedule> findSchedulesByTripId(Long tripId) {
        return scheduleRepository.findByTripIdWithPlace(tripId);
    }

    @Transactional(readOnly = true)
    public List<Trip> findAllPublicTrips() {
        return tripRepository.findAllByVisibility("PUBLIC");
    }

    @Transactional(readOnly = true)
    public Page<Trip> findAllPublicTrips(Pageable pageable, String searchType, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return tripRepository.findAllByVisibility("PUBLIC", pageable);
        }
        if ("title".equalsIgnoreCase(searchType)) {
            return tripRepository.findAllByVisibilityAndTitleContainingIgnoreCase("PUBLIC", keyword, pageable);
        } else if ("nickname".equalsIgnoreCase(searchType)) {
            return tripRepository.findAllByVisibilityAndMemberNicknameContainingIgnoreCase("PUBLIC", keyword, pageable);
        } else {
            return tripRepository.findAllByVisibility("PUBLIC", pageable);
        }
    }

    @Transactional(readOnly = true)
    public List<Trip> findAllTrips() {
        return tripRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TripPlan> findAllTripPlans() {
        return tripPlanRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Trip> findPublicTripsByNickname(String nickname, Pageable pageable) {
        return tripRepository.findByMemberNicknameAndVisibility(nickname, "PUBLIC", pageable);
    }

    @Transactional(readOnly = true)
    public List<Trip> findPublicTripsByNickname(String nickname) {
        return tripRepository.findByMemberNicknameAndVisibility(nickname, "PUBLIC");
    }
}
