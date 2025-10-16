package triB.triB.expense.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triB.triB.auth.entity.User;
import triB.triB.auth.repository.UserRepository;
import triB.triB.expense.dto.*;
import triB.triB.expense.entity.Expense;
import triB.triB.expense.entity.ExpenseCategory;
import triB.triB.expense.entity.PaymentMethod;
import triB.triB.expense.repository.ExpenseRepository;
import triB.triB.room.entity.UserRoomId;
import triB.triB.room.repository.UserRoomRepository;
import triB.triB.schedule.entity.Trip;
import triB.triB.schedule.repository.TripRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final UserRoomRepository userRoomRepository;
    
    @Transactional
    public ExpenseDetailsResponse createExpense(Long tripId, Long userId, ExpenseCreateRequest request) {
        // Validate trip exists and user is participant
        validateUserInTrip(tripId, userId);

        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // PaymentMethod별 로직 처리
        Long finalPayerUserId;
        BigDecimal finalAmount = request.getAmount();
        Boolean isSettled;

        if (request.getPaymentMethod() == PaymentMethod.TOGETHER) {
            // TOGETHER일 때는 결제자 userId가 필수
            if (request.getPayerUserId() == null) {
                throw new IllegalArgumentException("함께결제일 때는 결제자 ID가 필수입니다.");
            }
            // 결제자가 여행 참여자인지 검증
            validateUserInTrip(tripId, request.getPayerUserId());
            finalPayerUserId = request.getPayerUserId();

            // TOGETHER일 때는 총 금액을 참여자 수로 나누어 1인당 금액 계산
            if (request.getNumParticipants() == null || request.getNumParticipants() <= 0) {
                throw new IllegalArgumentException("함께결제일 때는 참여자 수가 1명 이상이어야 합니다.");
            }
            finalAmount = request.getAmount()
                    .divide(BigDecimal.valueOf(request.getNumParticipants()), 2, RoundingMode.HALF_UP);

            // TOGETHER일 때는 정산 미완료
            isSettled = false;
        } else {
            // SEPARATE일 때는 기록자가 결제자
            finalPayerUserId = userId;

            // SEPARATE일 때는 정산 완료
            isSettled = true;
        }

        Expense expense = Expense.builder()
                .tripId(tripId)
                .userId(userId)
                .amount(finalAmount)
                .category(request.getCategory())
                .description(request.getDescription())
                .expenseDate(request.getExpenseDate())
                .payerUserId(finalPayerUserId)
                .numParticipants(request.getNumParticipants())
                .paymentMethod(request.getPaymentMethod())
                .currency(request.getCurrency())
                .isSettled(isSettled)
                .build();
        
        Expense savedExpense = expenseRepository.save(expense);
        return mapToDetailsResponse(savedExpense);
    }
    
    public ExpenseDetailsResponse getExpense(Long tripId, Long expenseId, Long userId) {
        // Validate trip exists and user is participant
        validateUserInTrip(tripId, userId);

        Expense expense = expenseRepository.findByExpenseIdAndTripId(expenseId, tripId)
                .orElseThrow(() -> new IllegalArgumentException("지출 내역을 찾을 수 없습니다."));

        // 본인이 작성한 지출만 조회 가능
        if (!expense.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 지출만 조회할 수 있습니다.");
        }

        return mapToDetailsResponse(expense);
    }
    
    @Transactional
    public ExpenseDetailsResponse updateExpense(Long tripId, Long expenseId, Long userId, ExpenseUpdateRequest request) {
        // Validate trip exists and user is participant
        validateUserInTrip(tripId, userId);

        Expense expense = expenseRepository.findByExpenseIdAndTripId(expenseId, tripId)
                .orElseThrow(() -> new IllegalArgumentException("지출 내역을 찾을 수 없습니다."));

        // 본인이 작성한 지출만 수정 가능
        if (!expense.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 지출만 수정할 수 있습니다.");
        }

        // 금액 재계산이 필요한지 확인하기 위한 변수들
        BigDecimal newAmount = request.getAmount();
        Integer newNumParticipants = request.getNumParticipants();
        PaymentMethod newPaymentMethod = request.getPaymentMethod();

        // 기존 값들 저장
        BigDecimal currentAmount = expense.getAmount();
        Integer currentNumParticipants = expense.getNumParticipants();
        PaymentMethod currentPaymentMethod = expense.getPaymentMethod();

        if (request.getCategory() != null) {
            expense.setCategory(request.getCategory());
        }
        if (request.getDescription() != null) {
            expense.setDescription(request.getDescription());
        }
        if (request.getExpenseDate() != null) {
            expense.setExpenseDate(request.getExpenseDate());
        }

        // PaymentMethod 변경 시 결제자 로직 처리
        if (newPaymentMethod != null) {
            expense.setPaymentMethod(newPaymentMethod);

            if (newPaymentMethod == PaymentMethod.TOGETHER) {
                // TOGETHER로 변경 시 결제자 userId가 필요
                if (request.getPayerUserId() != null) {
                    validateUserInTrip(tripId, request.getPayerUserId());
                    expense.setPayerUserId(request.getPayerUserId());
                } else if (expense.getPayerUserId() == null) {
                    throw new IllegalArgumentException("함께결제로 변경할 때는 결제자 ID가 필요합니다.");
                }
            } else {
                // SEPARATE로 변경 시 기록자가 결제자
                expense.setPayerUserId(expense.getUserId());
            }
        } else if (request.getPayerUserId() != null) {
            // PaymentMethod 변경 없이 결제자만 변경하는 경우
            if (expense.getPaymentMethod() == PaymentMethod.TOGETHER) {
                validateUserInTrip(tripId, request.getPayerUserId());
                expense.setPayerUserId(request.getPayerUserId());
            } else {
                throw new IllegalArgumentException("각자결제에서는 결제자를 변경할 수 없습니다.");
            }
        }

        if (newNumParticipants != null) {
            expense.setNumParticipants(newNumParticipants);
        }

        if (request.getCurrency() != null) {
            expense.setCurrency(request.getCurrency());
        }

        // 금액 재계산 로직
        PaymentMethod finalPaymentMethod = newPaymentMethod != null ? newPaymentMethod : currentPaymentMethod;
        Integer finalNumParticipants = newNumParticipants != null ? newNumParticipants : currentNumParticipants;

        if (finalPaymentMethod == PaymentMethod.TOGETHER) {
            // TOGETHER일 때 금액 계산
            if (newAmount != null) {
                // 새로운 총 금액이 입력된 경우: 총 금액 / 참여자 수
                if (finalNumParticipants == null || finalNumParticipants <= 0) {
                    throw new IllegalArgumentException("함께결제일 때는 참여자 수가 1명 이상이어야 합니다.");
                }
                expense.setAmount(newAmount.divide(BigDecimal.valueOf(finalNumParticipants), 2, RoundingMode.HALF_UP));
            } else if (newNumParticipants != null && !newNumParticipants.equals(currentNumParticipants)) {
                // 참여자 수만 변경된 경우: 기존 1인당 금액을 역산하여 총 금액 계산 후 재분할
                // 기존 총 금액 = 기존 1인당 금액 × 기존 참여자 수
                BigDecimal totalAmount = currentAmount.multiply(BigDecimal.valueOf(currentNumParticipants));
                // 새로운 1인당 금액 = 총 금액 / 새로운 참여자 수
                expense.setAmount(totalAmount.divide(BigDecimal.valueOf(finalNumParticipants), 2, RoundingMode.HALF_UP));
            } else if (newPaymentMethod != null && currentPaymentMethod == PaymentMethod.SEPARATE) {
                // SEPARATE → TOGETHER로 변경: 기존 금액을 총 금액으로 간주하고 분할
                if (finalNumParticipants == null || finalNumParticipants <= 0) {
                    throw new IllegalArgumentException("함께결제일 때는 참여자 수가 1명 이상이어야 합니다.");
                }
                expense.setAmount(currentAmount.divide(BigDecimal.valueOf(finalNumParticipants), 2, RoundingMode.HALF_UP));
            }
        } else {
            // SEPARATE일 때
            if (newAmount != null) {
                // 새로운 금액이 입력된 경우: 그대로 저장 (1인당 금액)
                expense.setAmount(newAmount);
            } else if (newPaymentMethod != null && currentPaymentMethod == PaymentMethod.TOGETHER) {
                // TOGETHER → SEPARATE로 변경: 기존 1인당 금액을 총 금액으로 역산
                BigDecimal totalAmount = currentAmount.multiply(BigDecimal.valueOf(currentNumParticipants));
                expense.setAmount(totalAmount);
            }
        }

        Expense updatedExpense = expenseRepository.save(expense);
        return mapToDetailsResponse(updatedExpense);
    }
    
    @Transactional
    public void deleteExpense(Long tripId, Long expenseId, Long userId) {
        // Validate trip exists and user is participant
        validateUserInTrip(tripId, userId);

        Expense expense = expenseRepository.findByExpenseIdAndTripId(expenseId, tripId)
                .orElseThrow(() -> new IllegalArgumentException("지출 내역을 찾을 수 없습니다."));

        // 본인이 작성한 지출만 삭제 가능
        if (!expense.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 지출만 삭제할 수 있습니다.");
        }

        expenseRepository.delete(expense);
    }
    
    public List<ExpenseSummaryByCategory> getExpenseSummaryByCategory(Long tripId, Long userId) {
        // Validate trip exists and user is participant
        validateUserInTrip(tripId, userId);

        // 본인의 지출만 카테고리별로 집계
        return expenseRepository.findExpenseSummaryByCategoryAndUserId(tripId, userId);
    }

    public ExpenseSummaryByDate getExpenseSummaryByDate(Long tripId, LocalDate date, Long userId) {
        // Validate trip exists and user is participant
        validateUserInTrip(tripId, userId);

        // 본인의 지출만 날짜별로 집계
        BigDecimal totalAmount = expenseRepository.findTotalAmountByTripIdAndDateAndUserId(tripId, date, userId)
                .orElse(BigDecimal.ZERO);

        return ExpenseSummaryByDate.builder()
                .date(date)
                .totalAmount(totalAmount)
                .build();
    }

    public List<ExpenseDailyItem> getDailyExpenses(Long tripId, LocalDate date, Long userId) {
        // Validate trip exists and user is participant
        validateUserInTrip(tripId, userId);

        // 본인의 지출만 조회
        List<Expense> expenses = expenseRepository.findByTripIdAndExpenseDateAndUserId(tripId, date, userId);

        return expenses.stream()
                .map(expense -> {
                    String settlementInfo = null;

                    if (expense.getPaymentMethod() == PaymentMethod.TOGETHER) {
                        if (expense.getPayerUserId().equals(userId)) {
                            // 결제자가 본인일 때: "{numParticipants}명 → 나"
                            settlementInfo = expense.getNumParticipants() + "명 → 나";
                        } else {
                            // 결제자가 타인일 때: "나 → {결제자닉네임}"
                            User payer = userRepository.findById(expense.getPayerUserId()).orElse(null);
                            String payerName = (payer != null) ? payer.getNickname() : "알 수 없음";
                            settlementInfo = "나 → " + payerName;
                        }
                    }
                    // SEPARATE일 때는 settlementInfo가 null로 유지됨

                    return ExpenseDailyItem.builder()
                            .expenseId(expense.getExpenseId())
                            .description(expense.getDescription())
                            .amount(expense.getAmount())
                            .settlementInfo(settlementInfo)
                            .isSettled(expense.getIsSettled())
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    public List<ExpenseCategory> getAllCategories() {
        return Arrays.asList(ExpenseCategory.values());
    }

    /**
     * 여행 참여자 목록 조회
     * 가계부에서 사용 (결제자 선택 등)
     */
    public List<TripParticipantResponse> getTripParticipants(Long tripId, Long userId) {
        // Validate trip exists and user is participant
        validateUserInTrip(tripId, userId);

        // Trip에서 roomId 조회
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없습니다."));

        Long roomId = trip.getRoomId();

        // UserRoom에서 해당 Room의 모든 참여자 조회
        List<User> participants = userRoomRepository.findUsersByRoomId(roomId);

        // DTO로 변환 (userId와 닉네임 포함)
        return participants.stream()
                .map(user -> TripParticipantResponse.builder()
                        .userId(user.getUserId())
                        .nickname(user.getNickname())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 여행 날짜 조회 (시작일, 종료일)
     * 가계부에서 사용
     */
    public TripDateResponse getTripDates(Long tripId, Long userId) {
        // Validate trip exists and user is participant
        validateUserInTrip(tripId, userId);

        // Trip에서 roomId 조회
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없습니다."));

        // Room에서 날짜 정보 조회
        triB.triB.room.entity.Room room = trip.getRoom();
        if (room == null) {
            throw new IllegalArgumentException("여행에 연결된 방 정보를 찾을 수 없습니다.");
        }

        return TripDateResponse.builder()
                .startDate(room.getStartDate())
                .endDate(room.getEndDate())
                .build();
    }

    /**
     * 정산 상태 토글 (완료 <-> 미완료)
     */
    @Transactional
    public SettlementStatusResponse toggleSettlementStatus(Long tripId, Long expenseId, Long userId) {
        // Validate trip exists and user is participant
        validateUserInTrip(tripId, userId);

        Expense expense = expenseRepository.findByExpenseIdAndTripId(expenseId, tripId)
                .orElseThrow(() -> new IllegalArgumentException("지출 내역을 찾을 수 없습니다."));

        // 본인이 작성한 지출만 수정 가능
        if (!expense.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 지출만 정산 상태를 변경할 수 있습니다.");
        }

        // 정산 상태 토글
        expense.setIsSettled(!expense.getIsSettled());

        Expense updatedExpense = expenseRepository.save(expense);
        return SettlementStatusResponse.builder()
                .isSettled(updatedExpense.getIsSettled())
                .build();
    }

    private ExpenseDetailsResponse mapToDetailsResponse(Expense expense) {
        // DB에 저장된 payerUserId로 닉네임 조회
        User payer = userRepository.findById(expense.getPayerUserId()).orElse(null);
        String payerNickname = (payer != null) ? payer.getNickname() : null;

        return ExpenseDetailsResponse.builder()
                .expenseId(expense.getExpenseId())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .description(expense.getDescription())
                .expenseDate(expense.getExpenseDate())
                .payerNickname(payerNickname)
                .numParticipants(expense.getNumParticipants())
                .paymentMethod(expense.getPaymentMethod())
                .currency(expense.getCurrency())
                .build();
    }

    /**
     * 사용자가 해당 여행의 참여자인지 검증
     * Trip -> Room -> UserRoom 순으로 확인
     */
    private void validateUserInTrip(Long tripId, Long userId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없습니다."));

        Long roomId = trip.getRoomId();
        UserRoomId userRoomId = new UserRoomId(userId, roomId);

        boolean isParticipant = userRoomRepository.existsById(userRoomId);
        if (!isParticipant) {
            throw new IllegalArgumentException("해당 여행의 참여자만 접근할 수 있습니다.");
        }
    }

}
