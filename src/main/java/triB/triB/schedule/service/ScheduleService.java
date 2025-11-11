package triB.triB.schedule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triB.triB.room.entity.Room;
import triB.triB.room.entity.UserRoomId;
import triB.triB.room.repository.RoomRepository;
import triB.triB.room.repository.UserRoomRepository;
import triB.triB.schedule.dto.ReorderScheduleRequest;
import triB.triB.schedule.dto.ScheduleItemResponse;
import triB.triB.schedule.dto.TripScheduleResponse;
import triB.triB.schedule.dto.VisitStatusUpdateRequest;
import triB.triB.schedule.dto.VisitStatusUpdateResponse;
import triB.triB.schedule.entity.Schedule;
import triB.triB.schedule.entity.TravelMode;
import triB.triB.schedule.entity.Trip;
import triB.triB.schedule.repository.ScheduleRepository;
import triB.triB.schedule.repository.TripRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final TripRepository tripRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRoomRepository userRoomRepository;
    private final RoomRepository roomRepository;
    private final RoutesApiService routesApiService;

    /**
     * 사용자가 해당 여행에 접근 권한이 있는지 검증
     */
    private void validateUserInTrip(Long tripId, Long userId) {
        // Trip 존재 확인
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없습니다."));

        // UserRoom 존재 확인 (권한 검증)
        Long roomId = trip.getRoomId();
        UserRoomId userRoomId = new UserRoomId(userId, roomId);

        if (!userRoomRepository.existsById(userRoomId)) {
            throw new IllegalArgumentException("해당 여행에 접근 권한이 없습니다.");
        }
    }

    /**
     * 특정 여행의 특정 날짜 일정 조회
     */
    public TripScheduleResponse getTripSchedules(Long tripId, Integer dayNumber, Long userId) {
        // 권한 검증
        validateUserInTrip(tripId, userId);

        // dayNumber가 null이면 기본값 1 사용
        Integer targetDayNumber = (dayNumber != null) ? dayNumber : 1;

        // Trip 조회
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없습니다."));

        // Room 조회 (startDate, endDate 획득)
        Room room = roomRepository.findById(trip.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        // 특정 날짜의 일정 조회
        List<Schedule> schedules = scheduleRepository.findByTripIdAndDayNumber(tripId, targetDayNumber);

        // Schedule 리스트를 ScheduleItemResponse로 매핑
        List<ScheduleItemResponse> scheduleItems = schedules.stream()
                .map(this::mapToScheduleItemResponse)
                .collect(Collectors.toList());

        // TripScheduleResponse 생성 및 반환
        return TripScheduleResponse.builder()
                .tripId(trip.getTripId())
                .destination(trip.getDestination())
                .startDate(room.getStartDate())
                .endDate(room.getEndDate())
                .currentDay(targetDayNumber)
                .schedules(scheduleItems)
                .build();
    }

    /**
     * 일정의 방문 완료 상태 변경
     */
    @Transactional
    public VisitStatusUpdateResponse updateVisitStatus(Long tripId, Long scheduleId, VisitStatusUpdateRequest request, Long userId) {
        // 권한 검증
        validateUserInTrip(tripId, userId);

        // Schedule 조회
        Schedule schedule = scheduleRepository.findByScheduleIdAndTripId(scheduleId, tripId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        // 방문 상태 업데이트
        schedule.setIsVisit(request.getIsVisit());

        // JPA dirty checking으로 자동 업데이트

        // 응답 DTO 생성 및 반환
        return VisitStatusUpdateResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .isVisit(schedule.getIsVisit())
                .build();
    }

    /**
     * 일정 방문 순서 변경
     */
    @Transactional
    public TripScheduleResponse reorderSchedule(Long tripId, Long scheduleId, ReorderScheduleRequest request, Long userId) {
        // 권한 검증
        validateUserInTrip(tripId, userId);

        // Schedule 조회 및 dayNumber 확인
        Schedule targetSchedule = scheduleRepository.findByScheduleIdAndTripId(scheduleId, tripId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        Integer dayNumber = targetSchedule.getDayNumber();
        Integer currentOrder = targetSchedule.getVisitOrder();
        Integer newOrder = request.getNewVisitOrder();

        // 같은 순서로 변경하려는 경우 아무 작업 없이 반환
        if (currentOrder.equals(newOrder)) {
            return getTripSchedules(tripId, dayNumber, userId);
        }

        // 해당 날짜의 모든 일정 조회 (visitOrder 순으로 정렬)
        List<Schedule> daySchedules = scheduleRepository.findByTripIdAndDayNumber(tripId, dayNumber)
                .stream()
                .sorted(Comparator.comparing(Schedule::getVisitOrder))
                .collect(Collectors.toList());

        // 새로운 순서가 유효한지 검증
        if (newOrder < 1 || newOrder > daySchedules.size()) {
            throw new IllegalArgumentException("유효하지 않은 방문 순서입니다. (1-" + daySchedules.size() + " 사이여야 합니다)");
        }

        // 순서 변경 로직
        // 1. 리스트에서 대상 일정 제거
        daySchedules.removeIf(s -> s.getScheduleId().equals(scheduleId));

        // 2. 새로운 위치에 삽입 (newOrder는 1-based이므로 index는 newOrder-1)
        daySchedules.add(newOrder - 1, targetSchedule);

        // 3. 모든 일정의 visitOrder 재정렬
        for (int i = 0; i < daySchedules.size(); i++) {
            daySchedules.get(i).setVisitOrder(i + 1);
        }

        // 4. 이동시간 재계산
        recalculateDayTravelTimes(tripId, dayNumber);

        // 5. 출발/도착 시간 재계산
        recalculateDepartureTimes(tripId, dayNumber);

        // 6. 업데이트된 일정 반환
        return getTripSchedules(tripId, dayNumber, userId);
    }

    /**
     * 특정 날짜의 일정들 간 이동시간 재계산
     */
    private void recalculateDayTravelTimes(Long tripId, Integer dayNumber) {
        // Trip 조회 및 travelMode 확인
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없습니다."));

        TravelMode travelMode = trip.getTravelMode();
        // travelMode가 null이면 기본값 DRIVE 사용
        if (travelMode == null) {
            travelMode = TravelMode.DRIVE;
        }

        // 해당 날짜의 모든 일정을 visitOrder 순으로 조회
        List<Schedule> daySchedules = scheduleRepository.findByTripIdAndDayNumber(tripId, dayNumber)
                .stream()
                .sorted(Comparator.comparing(Schedule::getVisitOrder))
                .collect(Collectors.toList());

        // 각 일정의 다음 일정까지 이동시간 계산
        for (int i = 0; i < daySchedules.size(); i++) {
            Schedule currentSchedule = daySchedules.get(i);

            // 다음 일정이 있으면 이동시간 계산
            if (i < daySchedules.size() - 1) {
                Schedule nextSchedule = daySchedules.get(i + 1);

                // Routes API로 이동시간 계산 (분 단위)
                Integer travelMinutes = routesApiService.calculateTravelTime(
                        currentSchedule.getLatitude(),
                        currentSchedule.getLongitude(),
                        nextSchedule.getLatitude(),
                        nextSchedule.getLongitude(),
                        travelMode
                );

                // 한국어 형식으로 변환 ("30분", "1시간 30분" 등)
                String travelTimeText = routesApiService.formatMinutesToReadable(travelMinutes);
                currentSchedule.setTravelTime(travelTimeText);
            } else {
                // 마지막 일정은 travelTime null
                currentSchedule.setTravelTime(null);
            }
        }

        // JPA dirty checking으로 자동 업데이트
    }

    /**
     * 특정 날짜의 일정들의 departure 시간 재계산
     * 순서 변경 후 연쇄적으로 시간을 재계산합니다.
     */
    private void recalculateDepartureTimes(Long tripId, Integer dayNumber) {
        // 해당 날짜의 모든 일정을 visitOrder 순으로 조회
        List<Schedule> daySchedules = scheduleRepository.findByTripIdAndDayNumber(tripId, dayNumber)
                .stream()
                .sorted(Comparator.comparing(Schedule::getVisitOrder))
                .collect(Collectors.toList());

        // 각 일정에 대해 arrival과 departure 재계산
        for (int i = 0; i < daySchedules.size(); i++) {
            Schedule currentSchedule = daySchedules.get(i);

            if (i == 0) {
                // 첫 번째 일정: arrival은 유지, departure만 재계산
                LocalDateTime arrival = currentSchedule.getArrival();
                LocalDateTime originalDeparture = currentSchedule.getDeparture();

                // 체류시간 = 기존 (departure - arrival)
                long stayMinutes = Duration.between(arrival, originalDeparture).toMinutes();

                // departure = arrival + 체류시간
                LocalDateTime newDeparture = arrival.plusMinutes(stayMinutes);
                currentSchedule.setDeparture(newDeparture);
            } else {
                // 나머지 일정: 이전 일정의 departure + travelTime = 현재 arrival
                Schedule previousSchedule = daySchedules.get(i - 1);
                LocalDateTime previousDeparture = previousSchedule.getDeparture();
                String travelTimeText = previousSchedule.getTravelTime();

                // travelTime을 분 단위로 파싱
                Integer travelMinutes = parseTravelTimeToMinutes(travelTimeText);

                // 현재 일정의 arrival = 이전 departure + travelTime
                LocalDateTime newArrival = previousDeparture.plusMinutes(travelMinutes);

                // 체류시간 = 기존 (departure - arrival)
                LocalDateTime originalArrival = currentSchedule.getArrival();
                LocalDateTime originalDeparture = currentSchedule.getDeparture();
                long stayMinutes = Duration.between(originalArrival, originalDeparture).toMinutes();

                // 새로운 departure = 새로운 arrival + 체류시간
                LocalDateTime newDeparture = newArrival.plusMinutes(stayMinutes);

                currentSchedule.setArrival(newArrival);
                currentSchedule.setDeparture(newDeparture);
            }
        }

        // JPA dirty checking으로 자동 업데이트
    }

    /**
     * travelTime 문자열을 분 단위로 파싱
     * 예: "30분" -> 30, "1시간 30분" -> 90, "2시간" -> 120
     */
    private Integer parseTravelTimeToMinutes(String travelTimeText) {
        if (travelTimeText == null || travelTimeText.isEmpty()) {
            return 0;
        }

        try {
            int totalMinutes = 0;

            // "시간" 파싱
            if (travelTimeText.contains("시간")) {
                String[] parts = travelTimeText.split("시간");
                String hourPart = parts[0].trim();
                totalMinutes += Integer.parseInt(hourPart) * 60;

                // "분" 파싱 (시간 뒤에 분이 있는 경우)
                if (parts.length > 1 && parts[1].contains("분")) {
                    String minutePart = parts[1].replace("분", "").trim();
                    if (!minutePart.isEmpty()) {
                        totalMinutes += Integer.parseInt(minutePart);
                    }
                }
            } else if (travelTimeText.contains("분")) {
                // "분"만 있는 경우
                String minutePart = travelTimeText.replace("분", "").trim();
                totalMinutes = Integer.parseInt(minutePart);
            }

            return totalMinutes;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Schedule 엔티티를 ScheduleItemResponse로 매핑
     */
    private ScheduleItemResponse mapToScheduleItemResponse(Schedule schedule) {
        return ScheduleItemResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .displayName(schedule.getPlaceName())
                .arrival(schedule.getArrival())
                .departure(schedule.getDeparture())
                .placeTag(schedule.getPlaceTag())
                .travelTime(schedule.getTravelTime())
                .visitOrder(schedule.getVisitOrder())
                .isVisit(schedule.getIsVisit())
                .build();
    }
}
