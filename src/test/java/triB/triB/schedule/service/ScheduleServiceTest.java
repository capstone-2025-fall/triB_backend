package triB.triB.schedule.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import triB.triB.chat.entity.PlaceTag;
import triB.triB.room.entity.Room;
import triB.triB.room.entity.UserRoomId;
import triB.triB.room.repository.RoomRepository;
import triB.triB.room.repository.UserRoomRepository;
import triB.triB.schedule.dto.*;
import triB.triB.schedule.entity.Schedule;
import triB.triB.schedule.entity.TravelMode;
import triB.triB.schedule.entity.Trip;
import triB.triB.schedule.repository.ScheduleRepository;
import triB.triB.schedule.repository.TripRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleService 단위 테스트")
class ScheduleServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private UserRoomRepository userRoomRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoutesApiService routesApiService;

    @InjectMocks
    private ScheduleService scheduleService;

    private Trip testTrip;
    private Room testRoom;
    private Schedule testSchedule1;
    private Schedule testSchedule2;
    private Long userId;
    private Long tripId;
    private Long roomId;

    @BeforeEach
    void setUp() {
        userId = 1L;
        tripId = 100L;
        roomId = 10L;

        // Test Room 생성
        testRoom = Room.builder()
                .roomId(roomId)
                .roomName("Test Room")
                .destination("Seoul")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 3))
                .build();

        // Test Trip 생성
        testTrip = Trip.builder()
                .tripId(tripId)
                .roomId(roomId)
                .destination("Seoul")
                .travelMode(TravelMode.DRIVE)
                .build();

        // Test Schedule 생성
        testSchedule1 = Schedule.builder()
                .scheduleId(1L)
                .tripId(tripId)
                .dayNumber(1)
                .date(LocalDate.of(2025, 1, 1))
                .visitOrder(1)
                .placeName("장소1")
                .placeTag(PlaceTag.TOURIST_SPOT)
                .latitude(37.5665)
                .longitude(126.9780)
                .isVisit(false)
                .arrival(LocalDateTime.of(2025, 1, 1, 9, 0))
                .departure(LocalDateTime.of(2025, 1, 1, 10, 0))
                .travelTime("30분")
                .build();

        testSchedule2 = Schedule.builder()
                .scheduleId(2L)
                .tripId(tripId)
                .dayNumber(1)
                .date(LocalDate.of(2025, 1, 1))
                .visitOrder(2)
                .placeName("장소2")
                .placeTag(PlaceTag.RESTAURANT)
                .latitude(37.5700)
                .longitude(126.9800)
                .isVisit(false)
                .arrival(LocalDateTime.of(2025, 1, 1, 10, 30))
                .departure(LocalDateTime.of(2025, 1, 1, 11, 30))
                .travelTime(null)
                .build();
    }

    @Test
    @DisplayName("일정 조회 성공")
    void getTripSchedules_Success() {
        // given
        Integer dayNumber = 1;
        List<Schedule> schedules = Arrays.asList(testSchedule1, testSchedule2);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(userRoomRepository.existsById(any(UserRoomId.class))).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(scheduleRepository.findByTripIdAndDayNumber(tripId, dayNumber)).thenReturn(schedules);

        // when
        TripScheduleResponse response = scheduleService.getTripSchedules(tripId, dayNumber, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTripId()).isEqualTo(tripId);
        assertThat(response.getDestination()).isEqualTo("Seoul");
        assertThat(response.getCurrentDay()).isEqualTo(dayNumber);
        assertThat(response.getSchedules()).hasSize(2);
        assertThat(response.getStartDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(response.getEndDate()).isEqualTo(LocalDate.of(2025, 1, 3));

        verify(tripRepository, times(2)).findById(tripId);
        verify(scheduleRepository).findByTripIdAndDayNumber(tripId, dayNumber);
    }

    @Test
    @DisplayName("일정 조회 - dayNumber가 null일 때 기본값 1 사용")
    void getTripSchedules_WithDefaultDay() {
        // given
        Integer dayNumber = null;
        List<Schedule> schedules = Arrays.asList(testSchedule1);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(userRoomRepository.existsById(any(UserRoomId.class))).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(scheduleRepository.findByTripIdAndDayNumber(tripId, 1)).thenReturn(schedules);

        // when
        TripScheduleResponse response = scheduleService.getTripSchedules(tripId, dayNumber, userId);

        // then
        assertThat(response.getCurrentDay()).isEqualTo(1);
        verify(scheduleRepository).findByTripIdAndDayNumber(tripId, 1);
    }

    @Test
    @DisplayName("일정 조회 실패 - Trip이 존재하지 않음")
    void getTripSchedules_TripNotFound() {
        // given
        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.getTripSchedules(tripId, 1, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("여행을 찾을 수 없습니다");

        verify(tripRepository).findById(tripId);
        verify(scheduleRepository, never()).findByTripIdAndDayNumber(anyLong(), anyInt());
    }

    @Test
    @DisplayName("일정 조회 실패 - 사용자가 해당 여행에 권한이 없음")
    void getTripSchedules_UnauthorizedUser() {
        // given
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(userRoomRepository.existsById(any(UserRoomId.class))).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> scheduleService.getTripSchedules(tripId, 1, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 여행에 접근 권한이 없습니다");

        verify(userRoomRepository).existsById(any(UserRoomId.class));
    }

    @Test
    @DisplayName("방문 상태 변경 성공")
    void updateVisitStatus_Success() {
        // given
        Long scheduleId = 1L;
        VisitStatusUpdateRequest request = new VisitStatusUpdateRequest(true);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(userRoomRepository.existsById(any(UserRoomId.class))).thenReturn(true);
        when(scheduleRepository.findByScheduleIdAndTripId(scheduleId, tripId))
                .thenReturn(Optional.of(testSchedule1));

        // when
        VisitStatusUpdateResponse response = scheduleService.updateVisitStatus(tripId, scheduleId, request, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getScheduleId()).isEqualTo(scheduleId);
        assertThat(response.getIsVisit()).isTrue();
        assertThat(testSchedule1.getIsVisit()).isTrue();

        verify(scheduleRepository).findByScheduleIdAndTripId(scheduleId, tripId);
    }

    @Test
    @DisplayName("방문 상태 변경 실패 - Schedule이 존재하지 않음")
    void updateVisitStatus_ScheduleNotFound() {
        // given
        Long scheduleId = 999L;
        VisitStatusUpdateRequest request = new VisitStatusUpdateRequest(true);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(userRoomRepository.existsById(any(UserRoomId.class))).thenReturn(true);
        when(scheduleRepository.findByScheduleIdAndTripId(scheduleId, tripId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleService.updateVisitStatus(tripId, scheduleId, request, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("일정을 찾을 수 없습니다");

        verify(scheduleRepository).findByScheduleIdAndTripId(scheduleId, tripId);
    }

    @Test
    @DisplayName("일정 순서 변경 성공")
    void reorderSchedule_Success() {
        // given
        Long scheduleId = 1L;
        ReorderScheduleRequest request = new ReorderScheduleRequest(2);

        List<Schedule> daySchedules = Arrays.asList(testSchedule1, testSchedule2);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(userRoomRepository.existsById(any(UserRoomId.class))).thenReturn(true);
        when(scheduleRepository.findByScheduleIdAndTripId(scheduleId, tripId))
                .thenReturn(Optional.of(testSchedule1));
        when(scheduleRepository.findByTripIdAndDayNumber(tripId, 1))
                .thenReturn(daySchedules);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(routesApiService.calculateTravelTime(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any()))
                .thenReturn(30);
        when(routesApiService.formatMinutesToReadable(anyInt())).thenReturn("30분");

        // when
        TripScheduleResponse response = scheduleService.reorderSchedule(tripId, scheduleId, request, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(testSchedule1.getVisitOrder()).isEqualTo(2);
        assertThat(testSchedule2.getVisitOrder()).isEqualTo(1);

        verify(scheduleRepository, atLeastOnce()).findByTripIdAndDayNumber(tripId, 1);
        verify(routesApiService, atLeastOnce()).calculateTravelTime(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any());
    }

    @Test
    @DisplayName("일정 순서 변경 - 이동시간 재계산 확인")
    void reorderSchedule_RecalculatesTravelTimes() {
        // given
        Long scheduleId = 2L;
        ReorderScheduleRequest request = new ReorderScheduleRequest(1);

        List<Schedule> daySchedules = Arrays.asList(testSchedule1, testSchedule2);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(userRoomRepository.existsById(any(UserRoomId.class))).thenReturn(true);
        when(scheduleRepository.findByScheduleIdAndTripId(scheduleId, tripId))
                .thenReturn(Optional.of(testSchedule2));
        when(scheduleRepository.findByTripIdAndDayNumber(tripId, 1))
                .thenReturn(daySchedules);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(routesApiService.calculateTravelTime(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any()))
                .thenReturn(25);
        when(routesApiService.formatMinutesToReadable(anyInt())).thenReturn("25분");

        // when
        scheduleService.reorderSchedule(tripId, scheduleId, request, userId);

        // then
        verify(routesApiService, atLeastOnce()).calculateTravelTime(anyDouble(), anyDouble(), anyDouble(), anyDouble(), eq(TravelMode.DRIVE));
    }

    @Test
    @DisplayName("일정 순서 변경 실패 - 유효하지 않은 순서")
    void reorderSchedule_InvalidOrder() {
        // given
        Long scheduleId = 1L;
        ReorderScheduleRequest request = new ReorderScheduleRequest(10); // 유효하지 않은 순서

        List<Schedule> daySchedules = Arrays.asList(testSchedule1, testSchedule2);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(userRoomRepository.existsById(any(UserRoomId.class))).thenReturn(true);
        when(scheduleRepository.findByScheduleIdAndTripId(scheduleId, tripId))
                .thenReturn(Optional.of(testSchedule1));
        when(scheduleRepository.findByTripIdAndDayNumber(tripId, 1))
                .thenReturn(daySchedules);

        // when & then
        assertThatThrownBy(() -> scheduleService.reorderSchedule(tripId, scheduleId, request, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 방문 순서입니다");
    }

    @Test
    @DisplayName("일정 추가 성공")
    void addScheduleToDay_Success() {
        // given
        AddScheduleRequest request = AddScheduleRequest.builder()
                .dayNumber(1)
                .placeName("새로운 장소")
                .placeTag(PlaceTag.CAFE)
                .latitude(37.5800)
                .longitude(126.9900)
                .stayMinutes(60)
                .build();

        List<Schedule> existingSchedules = Arrays.asList(testSchedule1, testSchedule2);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(userRoomRepository.existsById(any(UserRoomId.class))).thenReturn(true);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(testRoom));
        when(scheduleRepository.findByTripIdAndDayNumber(tripId, 1))
                .thenReturn(existingSchedules);
        when(scheduleRepository.save(any(Schedule.class))).thenAnswer(invocation -> {
            Schedule schedule = invocation.getArgument(0);
            schedule.setScheduleId(3L);
            return schedule;
        });
        when(routesApiService.calculateTravelTime(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any()))
                .thenReturn(20);
        when(routesApiService.formatMinutesToReadable(anyInt())).thenReturn("20분");

        // when
        ScheduleItemResponse response = scheduleService.addScheduleToDay(tripId, request, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getDisplayName()).isEqualTo("새로운 장소");
        assertThat(testSchedule2.getTravelTime()).isEqualTo("20분");

        verify(scheduleRepository).save(any(Schedule.class));
        verify(routesApiService).calculateTravelTime(anyDouble(), anyDouble(), anyDouble(), anyDouble(), eq(TravelMode.DRIVE));
    }

    @Test
    @DisplayName("일정 삭제 - 이후 일정 순서 재정렬")
    void deleteSchedule_ReordersSubsequent() {
        // given
        Long scheduleId = 1L;
        Schedule schedule3 = Schedule.builder()
                .scheduleId(3L)
                .tripId(tripId)
                .dayNumber(1)
                .visitOrder(3)
                .placeName("장소3")
                .placeTag(PlaceTag.TOURIST_SPOT)
                .latitude(37.5900)
                .longitude(127.0000)
                .isVisit(false)
                .arrival(LocalDateTime.of(2025, 1, 1, 12, 0))
                .departure(LocalDateTime.of(2025, 1, 1, 13, 0))
                .travelTime(null)
                .build();

        List<Schedule> remainingSchedules = Arrays.asList(testSchedule2, schedule3);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(userRoomRepository.existsById(any(UserRoomId.class))).thenReturn(true);
        when(scheduleRepository.findByScheduleIdAndTripId(scheduleId, tripId))
                .thenReturn(Optional.of(testSchedule1));
        when(scheduleRepository.findByTripIdAndDayNumber(tripId, 1))
                .thenReturn(remainingSchedules);
        when(routesApiService.calculateTravelTime(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any()))
                .thenReturn(30);
        when(routesApiService.formatMinutesToReadable(anyInt())).thenReturn("30분");

        // when
        DeleteScheduleResponse response = scheduleService.deleteSchedule(tripId, scheduleId, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getDeletedScheduleId()).isEqualTo(scheduleId);
        assertThat(testSchedule2.getVisitOrder()).isEqualTo(1);
        assertThat(schedule3.getVisitOrder()).isEqualTo(2);

        verify(scheduleRepository).delete(testSchedule1);
        verify(scheduleRepository, atLeastOnce()).findByTripIdAndDayNumber(tripId, 1);
    }

    @Test
    @DisplayName("체류시간 수정 - departure 시간 업데이트")
    void updateStayDuration_UpdatesDeparture() {
        // given
        Long scheduleId = 1L;
        UpdateStayDurationRequest request = new UpdateStayDurationRequest(90);

        LocalDateTime originalArrival = testSchedule1.getArrival();
        List<Schedule> daySchedules = Arrays.asList(testSchedule1, testSchedule2);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(userRoomRepository.existsById(any(UserRoomId.class))).thenReturn(true);
        when(scheduleRepository.findByScheduleIdAndTripId(scheduleId, tripId))
                .thenReturn(Optional.of(testSchedule1));
        when(scheduleRepository.findByTripIdAndDayNumber(tripId, 1))
                .thenReturn(daySchedules);

        // when
        ScheduleItemResponse response = scheduleService.updateStayDuration(tripId, scheduleId, request, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(testSchedule1.getDeparture()).isEqualTo(originalArrival.plusMinutes(90));

        verify(scheduleRepository).findByScheduleIdAndTripId(scheduleId, tripId);
    }

    @Test
    @DisplayName("방문 시간 수정 - 연쇄 계산 확인")
    void updateVisitTime_RecalculatesChain() {
        // given
        Long scheduleId = 1L;
        UpdateVisitTimeRequest request = new UpdateVisitTimeRequest(LocalTime.of(10, 0));

        List<Schedule> daySchedules = Arrays.asList(testSchedule1, testSchedule2);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(userRoomRepository.existsById(any(UserRoomId.class))).thenReturn(true);
        when(scheduleRepository.findByScheduleIdAndTripId(scheduleId, tripId))
                .thenReturn(Optional.of(testSchedule1));
        when(scheduleRepository.findByTripIdAndDayNumber(tripId, 1))
                .thenReturn(daySchedules);

        // when
        ScheduleItemResponse response = scheduleService.updateVisitTime(tripId, scheduleId, request, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(testSchedule1.getArrival().toLocalTime()).isEqualTo(LocalTime.of(10, 0));

        verify(scheduleRepository, atLeastOnce()).findByTripIdAndDayNumber(tripId, 1);
    }

    @Test
    @DisplayName("숙소 변경 성공")
    void updateAccommodation_Success() {
        // given
        Schedule accommodationSchedule = Schedule.builder()
                .scheduleId(3L)
                .tripId(tripId)
                .dayNumber(1)
                .visitOrder(3)
                .placeName("기존 숙소")
                .placeTag(PlaceTag.HOME)
                .latitude(37.5600)
                .longitude(126.9700)
                .isVisit(false)
                .arrival(LocalDateTime.of(2025, 1, 1, 18, 0))
                .departure(LocalDateTime.of(2025, 1, 2, 9, 0))
                .travelTime(null)
                .build();

        UpdateAccommodationRequest request = new UpdateAccommodationRequest(1, "새로운 숙소", 37.5650, 126.9750);

        List<Schedule> daySchedules = Arrays.asList(testSchedule1, testSchedule2, accommodationSchedule);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(userRoomRepository.existsById(any(UserRoomId.class))).thenReturn(true);
        when(scheduleRepository.findByTripIdAndDayNumber(tripId, 1))
                .thenReturn(daySchedules);
        when(scheduleRepository.findByTripIdAndDayNumber(tripId, 2))
                .thenReturn(Arrays.asList());
        when(routesApiService.calculateTravelTime(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any()))
                .thenReturn(15);
        when(routesApiService.formatMinutesToReadable(anyInt())).thenReturn("15분");

        // when
        ScheduleItemResponse response = scheduleService.updateAccommodation(tripId, request, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(accommodationSchedule.getPlaceName()).isEqualTo("새로운 숙소");
        assertThat(accommodationSchedule.getLatitude()).isEqualTo(37.5650);
        assertThat(accommodationSchedule.getLongitude()).isEqualTo(126.9750);

        verify(routesApiService, atLeastOnce()).calculateTravelTime(anyDouble(), anyDouble(), anyDouble(), anyDouble(), eq(TravelMode.DRIVE));
    }

    @Test
    @DisplayName("숙소 변경 실패 - 숙소가 존재하지 않음")
    void updateAccommodation_NotFound() {
        // given
        UpdateAccommodationRequest request = new UpdateAccommodationRequest(1, "새로운 숙소", 37.5650, 126.9750);

        // HOME 태그가 없는 일정들만 반환
        List<Schedule> daySchedules = Arrays.asList(testSchedule1, testSchedule2);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(testTrip));
        when(userRoomRepository.existsById(any(UserRoomId.class))).thenReturn(true);
        when(scheduleRepository.findByTripIdAndDayNumber(tripId, 1))
                .thenReturn(daySchedules);

        // when & then
        assertThatThrownBy(() -> scheduleService.updateAccommodation(tripId, request, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 날짜에 숙소를 찾을 수 없습니다");
    }
}
