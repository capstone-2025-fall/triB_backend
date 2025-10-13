package triB.triB.budget.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import triB.triB.budget.dto.BudgetCreateRequest;
import triB.triB.budget.dto.BudgetResponse;
import triB.triB.budget.dto.BudgetUpdateRequest;
import triB.triB.budget.service.BudgetService;
import triB.triB.global.response.ApiResponse;

@Tag(name = "Budget", description = "여행 예산 관리 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @Operation(summary = "예산 생성", description = "여행에 대한 사용자의 예산을 생성합니다.")
    @PostMapping("/trips/{tripId}/budgets")
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
            @Parameter(description = "여행 ID") @PathVariable Long tripId,
            @Parameter(description = "사용자 ID") @RequestParam Long userId,
            @Valid @RequestBody BudgetCreateRequest request) {

        BudgetResponse response = budgetService.createBudget(tripId, userId, request);
        return ApiResponse.created("예산이 성공적으로 생성되었습니다.", response);
    }

    @Operation(summary = "예산 수정", description = "사용자의 예산을 수정합니다.")
    @PatchMapping("/trips/{tripId}/budgets")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @Parameter(description = "여행 ID") @PathVariable Long tripId,
            @Parameter(description = "사용자 ID") @RequestParam Long userId,
            @Valid @RequestBody BudgetUpdateRequest request) {

        BudgetResponse response = budgetService.updateBudget(tripId, userId, request);
        return ApiResponse.ok("예산이 성공적으로 수정되었습니다.", response);
    }

    @Operation(summary = "내 예산 조회", description = "현재 사용자의 예산을 조회합니다.")
    @GetMapping("/trips/{tripId}/budgets/me")
    public ResponseEntity<ApiResponse<BudgetResponse>> getMyBudget(
            @Parameter(description = "여행 ID") @PathVariable Long tripId,
            @Parameter(description = "사용자 ID") @RequestParam Long userId) {

        BudgetResponse response = budgetService.getMyBudget(tripId, userId);
        return ApiResponse.ok("예산 조회가 완료되었습니다.", response);
    }
}
