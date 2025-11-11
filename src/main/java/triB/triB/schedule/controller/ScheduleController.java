package triB.triB.schedule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import triB.triB.global.response.ApiResponse;
import triB.triB.global.security.UserPrincipal;
import triB.triB.schedule.dto.AddScheduleRequest;
import triB.triB.schedule.dto.DeleteScheduleResponse;
import triB.triB.schedule.dto.ReorderScheduleRequest;
import triB.triB.schedule.dto.ScheduleItemResponse;
import triB.triB.schedule.dto.TripScheduleResponse;
import triB.triB.schedule.dto.UpdateAccommodationRequest;
import triB.triB.schedule.dto.UpdateStayDurationRequest;
import triB.triB.schedule.dto.UpdateVisitTimeRequest;
import triB.triB.schedule.dto.VisitStatusUpdateRequest;
import triB.triB.schedule.dto.VisitStatusUpdateResponse;
import triB.triB.schedule.service.ScheduleService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trips/{tripId}")
@Tag(name = "Schedule", description = "일정 관리 API")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/schedules")
    @Operation(
            summary = "여행 일정 조회",
            description = "특정 여행의 특정 날짜 일정을 조회합니다. dayNumber를 지정하지 않으면 1일차를 반환합니다."
    )
    public ResponseEntity<ApiResponse<TripScheduleResponse>> getTripSchedules(
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long tripId,

            @Parameter(description = "조회할 일차 (기본값: 1)", example = "1")
            @RequestParam(required = false, defaultValue = "1") Integer dayNumber,

            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        TripScheduleResponse response = scheduleService.getTripSchedules(
                tripId,
                dayNumber,
                userPrincipal.getUserId()
        );

        return ApiResponse.ok("일정을 조회했습니다.", response);
    }

    @PatchMapping("/schedules/{scheduleId}/visit-status")
    @Operation(
            summary = "방문 완료/미완료 변경",
            description = "일정의 방문 완료 상태를 변경합니다."
    )
    public ResponseEntity<ApiResponse<VisitStatusUpdateResponse>> updateVisitStatus(
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long tripId,

            @Parameter(description = "일정 ID", required = true)
            @PathVariable Long scheduleId,

            @Parameter(description = "방문 상태 변경 요청", required = true)
            @RequestBody @Valid VisitStatusUpdateRequest request,

            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        VisitStatusUpdateResponse response = scheduleService.updateVisitStatus(
                tripId,
                scheduleId,
                request,
                userPrincipal.getUserId()
        );

        return ApiResponse.ok("방문 상태를 변경했습니다.", response);
    }

    @PatchMapping("/schedules/{scheduleId}/reorder")
    @Operation(
            summary = "일정 순서 변경",
            description = "특정 날짜의 일정 순서를 변경하고 이동시간을 재계산합니다."
    )
    public ResponseEntity<ApiResponse<TripScheduleResponse>> reorderSchedule(
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long tripId,

            @Parameter(description = "일정 ID", required = true)
            @PathVariable Long scheduleId,

            @Parameter(description = "순서 변경 요청", required = true)
            @RequestBody @Valid ReorderScheduleRequest request,

            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        TripScheduleResponse response = scheduleService.reorderSchedule(
                tripId,
                scheduleId,
                request,
                userPrincipal.getUserId()
        );

        return ApiResponse.ok("일정 순서를 변경했습니다.", response);
    }

    @PatchMapping("/schedules/{scheduleId}/stay-duration")
    @Operation(
            summary = "체류시간 수정",
            description = "특정 일정의 체류시간을 수정하고 이후 일정 시간을 재계산합니다."
    )
    public ResponseEntity<ApiResponse<ScheduleItemResponse>> updateStayDuration(
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long tripId,

            @Parameter(description = "일정 ID", required = true)
            @PathVariable Long scheduleId,

            @Parameter(description = "체류시간 수정 요청", required = true)
            @RequestBody @Valid UpdateStayDurationRequest request,

            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        ScheduleItemResponse response = scheduleService.updateStayDuration(
                tripId,
                scheduleId,
                request,
                userPrincipal.getUserId()
        );

        return ApiResponse.ok("체류시간을 수정했습니다.", response);
    }

    @PatchMapping("/schedules/{scheduleId}/visit-time")
    @Operation(
            summary = "방문 시간 수정",
            description = "특정 일정의 방문 시간을 수정하고 이후 일정 시간을 재계산합니다."
    )
    public ResponseEntity<ApiResponse<ScheduleItemResponse>> updateVisitTime(
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long tripId,

            @Parameter(description = "일정 ID", required = true)
            @PathVariable Long scheduleId,

            @Parameter(description = "방문 시간 수정 요청", required = true)
            @RequestBody @Valid UpdateVisitTimeRequest request,

            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        ScheduleItemResponse response = scheduleService.updateVisitTime(
                tripId,
                scheduleId,
                request,
                userPrincipal.getUserId()
        );

        return ApiResponse.ok("방문 시간을 수정했습니다.", response);
    }

    @PostMapping("/schedules")
    @Operation(
            summary = "일정 추가",
            description = "특정 날짜의 마지막 일정으로 새로운 장소를 추가합니다."
    )
    public ResponseEntity<ApiResponse<ScheduleItemResponse>> addSchedule(
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long tripId,

            @Parameter(description = "일정 추가 요청", required = true)
            @RequestBody @Valid AddScheduleRequest request,

            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        ScheduleItemResponse response = scheduleService.addScheduleToDay(
                tripId,
                request,
                userPrincipal.getUserId()
        );

        return ApiResponse.created("일정을 추가했습니다.", response);
    }

    @DeleteMapping("/schedules/{scheduleId}")
    @Operation(
            summary = "일정 삭제",
            description = "특정 일정을 삭제하고 이후 일정의 순서와 이동시간을 재계산합니다."
    )
    public ResponseEntity<ApiResponse<DeleteScheduleResponse>> deleteSchedule(
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long tripId,

            @Parameter(description = "일정 ID", required = true)
            @PathVariable Long scheduleId,

            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        DeleteScheduleResponse response = scheduleService.deleteSchedule(
                tripId,
                scheduleId,
                userPrincipal.getUserId()
        );

        return ApiResponse.ok("일정을 삭제했습니다.", response);
    }

    @PatchMapping("/accommodation")
    @Operation(
            summary = "숙소 변경",
            description = "숙소를 변경하고 출발/도착 시간을 재계산합니다."
    )
    public ResponseEntity<ApiResponse<ScheduleItemResponse>> updateAccommodation(
            @Parameter(description = "여행 ID", required = true)
            @PathVariable Long tripId,

            @Parameter(description = "숙소 변경 요청", required = true)
            @RequestBody @Valid UpdateAccommodationRequest request,

            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        ScheduleItemResponse response = scheduleService.updateAccommodation(
                tripId,
                request,
                userPrincipal.getUserId()
        );

        return ApiResponse.ok("숙소를 변경했습니다.", response);
    }
}
