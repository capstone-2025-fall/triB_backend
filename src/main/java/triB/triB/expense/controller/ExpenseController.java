package triB.triB.expense.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import triB.triB.expense.dto.*;
import triB.triB.expense.entity.ExpenseCategory;
import triB.triB.expense.service.ExpenseService;
import triB.triB.global.response.ApiResponse;
import triB.triB.global.security.UserPrincipal;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Expense", description = "여행 지출 관리 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ExpenseController {
    
    private final ExpenseService expenseService;
    
    @Operation(summary = "지출 추가", description = "여행에 새로운 지출 내역을 추가합니다.")
    @PostMapping("/trips/{tripId}/expenses")
    public ResponseEntity<ApiResponse<ExpenseDetailsResponse>> createExpense(
            @Parameter(description = "여행 ID") @PathVariable Long tripId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ExpenseCreateRequest request) {
        Long userId = userPrincipal.getUserId();
        ExpenseDetailsResponse response = expenseService.createExpense(tripId, userId, request);
        return ApiResponse.created("지출 내역이 성공적으로 추가되었습니다.", response);
    }
    
    @Operation(summary = "지출 단건 조회", description = "특정 지출 내역의 상세 정보를 조회합니다.")
    @GetMapping("/trips/{tripId}/expenses/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseDetailsResponse>> getExpense(
            @Parameter(description = "여행 ID") @PathVariable Long tripId,
            @Parameter(description = "지출 ID") @PathVariable Long expenseId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long userId = userPrincipal.getUserId();
        ExpenseDetailsResponse response = expenseService.getExpense(tripId, expenseId, userId);
        return ApiResponse.ok("지출 내역 조회가 완료되었습니다.", response);
    }
    
    @Operation(summary = "지출 수정", description = "기존 지출 내역의 정보를 수정합니다.")
    @PatchMapping("/trips/{tripId}/expenses/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseDetailsResponse>> updateExpense(
            @Parameter(description = "여행 ID") @PathVariable Long tripId,
            @Parameter(description = "지출 ID") @PathVariable Long expenseId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ExpenseUpdateRequest request) {

        Long userId = userPrincipal.getUserId();
        ExpenseDetailsResponse response = expenseService.updateExpense(tripId, expenseId, userId, request);
        return ApiResponse.ok("지출 내역이 성공적으로 수정되었습니다.", response);
    }
    
    @Operation(summary = "지출 삭제", description = "특정 지출 내역을 삭제합니다.")
    @DeleteMapping("/trips/{tripId}/expenses/{expenseId}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @Parameter(description = "여행 ID") @PathVariable Long tripId,
            @Parameter(description = "지출 ID") @PathVariable Long expenseId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long userId = userPrincipal.getUserId();
        expenseService.deleteExpense(tripId, expenseId, userId);
        return ApiResponse.ok("지출 내역이 성공적으로 삭제되었습니다.", null);
    }
    
    @Operation(summary = "카테고리별 지출 합계 조회", description = "여행의 카테고리별 지출 합계를 조회합니다.")
    @GetMapping("/trips/{tripId}/expenses/summary/by-category")
    public ResponseEntity<ApiResponse<List<ExpenseSummaryByCategory>>> getExpenseSummaryByCategory(
            @Parameter(description = "여행 ID") @PathVariable Long tripId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long userId = userPrincipal.getUserId();
        List<ExpenseSummaryByCategory> response = expenseService.getExpenseSummaryByCategory(tripId, userId);
        return ApiResponse.ok("카테고리별 지출 합계 조회가 완료되었습니다.", response);
    }
    
    @Operation(summary = "일별 지출 합계 조회", description = "특정 날짜의 총 지출 합계를 조회합니다.")
    @GetMapping("/trips/{tripId}/expenses/summary/by-date")
    public ResponseEntity<ApiResponse<ExpenseSummaryByDate>> getExpenseSummaryByDate(
            @Parameter(description = "여행 ID") @PathVariable Long tripId,
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long userId = userPrincipal.getUserId();
        ExpenseSummaryByDate response = expenseService.getExpenseSummaryByDate(tripId, date, userId);
        return ApiResponse.ok("일별 지출 합계 조회가 완료되었습니다.", response);
    }
    
    @Operation(summary = "특정 일자 지출 내역 목록 조회", description = "특정 날짜의 모든 지출 내역 목록을 조회합니다.")
    @GetMapping("/trips/{tripId}/expenses/daily/{date}")
    public ResponseEntity<ApiResponse<List<ExpenseDailyItem>>> getDailyExpenses(
            @Parameter(description = "여행 ID") @PathVariable Long tripId,
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)")
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long userId = userPrincipal.getUserId();
        List<ExpenseDailyItem> response = expenseService.getDailyExpenses(tripId, date, userId);
        return ApiResponse.ok("특정 일자 지출 내역 조회가 완료되었습니다.", response);
    }
    
    @Operation(summary = "카테고리 목록 조회", description = "사용 가능한 전체 지출 카테고리 목록을 조회합니다.")
    @GetMapping("/expenses/meta/categories")
    public ResponseEntity<ApiResponse<List<ExpenseCategory>>> getAllCategories() {

        List<ExpenseCategory> response = expenseService.getAllCategories();
        return ApiResponse.ok("카테고리 목록 조회가 완료되었습니다.", response);
    }

    @Operation(summary = "여행 참여자 목록 조회", description = "여행의 모든 참여자 정보를 조회합니다. 가계부에서 사용됩니다.")
    @GetMapping("/trips/{tripId}/expenses/participants")
    public ResponseEntity<ApiResponse<List<TripParticipantResponse>>> getTripParticipants(
            @Parameter(description = "여행 ID") @PathVariable Long tripId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long userId = userPrincipal.getUserId();
        List<TripParticipantResponse> response = expenseService.getTripParticipants(tripId, userId);
        return ApiResponse.ok("여행 참여자 조회가 완료되었습니다.", response);
    }

    @Operation(summary = "여행 날짜 조회", description = "여행의 시작일과 종료일을 조회합니다. 가계부에서 사용됩니다.")
    @GetMapping("/trips/{tripId}/expenses/dates")
    public ResponseEntity<ApiResponse<TripDateResponse>> getTripDates(
            @Parameter(description = "여행 ID") @PathVariable Long tripId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long userId = userPrincipal.getUserId();
        TripDateResponse response = expenseService.getTripDates(tripId, userId);
        return ApiResponse.ok("여행 날짜 조회가 완료되었습니다.", response);
    }
}
