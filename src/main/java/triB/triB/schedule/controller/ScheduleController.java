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
import triB.triB.schedule.dto.ReorderScheduleRequest;
import triB.triB.schedule.dto.TripScheduleResponse;
import triB.triB.schedule.dto.VisitStatusUpdateRequest;
import triB.triB.schedule.dto.VisitStatusUpdateResponse;
import triB.triB.schedule.service.ScheduleService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trips/{tripId}/schedules")
@Tag(name = "Schedule", description = "일정 관리 API")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
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

    @PatchMapping("/{scheduleId}/visit-status")
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

    @PatchMapping("/{scheduleId}/reorder")
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
}
