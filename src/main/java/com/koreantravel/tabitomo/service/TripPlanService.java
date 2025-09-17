package com.koreantravel.tabitomo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.koreantravel.tabitomo.domain.dto.trip.TripPlan;
import com.koreantravel.tabitomo.domain.entity.Member;
import com.koreantravel.tabitomo.domain.entity.Place;
import com.koreantravel.tabitomo.domain.entity.Schedule;
import com.koreantravel.tabitomo.domain.entity.Trip;
import com.koreantravel.tabitomo.repository.MemberRepository;
import com.koreantravel.tabitomo.repository.PlaceRepository;
import com.koreantravel.tabitomo.repository.ScheduleRepository;
import com.koreantravel.tabitomo.repository.TripRepository;

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
    public Long saveTripPlan(TripPlan tripPlan, String email) {
        Member member = memberRepository.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다: " + email));

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
    public void updateTripPlan(TripPlan updatedTripPlan, String email) {
        log.info("Updating trip plan with ID: {}", updatedTripPlan.getId());
        Trip trip = tripRepository.findById(updatedTripPlan.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 여행을 찾을 수 없습니다."));

        if (!trip.getMember().getEmail().equals(email)) {
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

    @Transactional
    public void deleteTripPlan(Long tripId, String email) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 계획을 찾을 수 없습니다."));

        if (!trip.getMember().getEmail().equals(email)) {
            throw new SecurityException("이 여행 계획을 삭제할 권한이 없습니다.");
        }

        tripRepository.delete(trip);
    }

    @Transactional
    public void deleteTripPlanByAdmin(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 계획을 찾을 수 없습니다: " + tripId));
        tripRepository.delete(trip);
    }

    @Transactional
    public void toggleTripVisibility(Long tripId, String email) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 계획을 찾을 수 없습니다."));

        if (!trip.getMember().getEmail().equals(email)) {
            throw new SecurityException("이 여행 계획의 공개 상태를 변경할 권한이 없습니다.");
        }

        trip.toggleVisibility();
    }

    private Place findOrCreatePlaceFromDto(TripPlan.Accommodation accDto) {
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

    private Place findOrCreatePlaceFromDto(TripPlan.ScheduleItem itemDto) {
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
    public Optional<Trip> findTripByIdAndEmail(Long tripId, String email) {
        return tripRepository.findByIdWithMember(tripId)
                .filter(trip -> trip.getMember().getEmail().equals(email));
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
    public List<Trip> findAllTrips() {
        return tripRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Trip> findPublicTripsByNickname(String nickname) {
        return tripRepository.findByMemberNicknameAndVisibility(nickname, "PUBLIC");
    }
}
