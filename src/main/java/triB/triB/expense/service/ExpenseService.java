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
import triB.triB.schedule.repository.TripRepository;

import java.math.BigDecimal;
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
    
    @Transactional
    public ExpenseDetailsResponse createExpense(Long tripId, Long userId, ExpenseCreateRequest request) {
        // Validate trip exists
        tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없습니다."));
        
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // PaymentMethod별 로직 처리
        Long finalPayerUserId;
        if (request.getPaymentMethod() == PaymentMethod.TOGETHER) {
            // TOGETHER일 때는 payerUserId가 필수
            if (request.getPayerUserId() == null) {
                throw new IllegalArgumentException("함께결제일 때는 결제자 ID가 필수입니다.");
            }
            // Validate payer exists
            userRepository.findById(request.getPayerUserId())
                    .orElseThrow(() -> new IllegalArgumentException("결제자를 찾을 수 없습니다."));
            finalPayerUserId = request.getPayerUserId();
        } else {
            // SEPARATE일 때는 기록자가 결제자
            finalPayerUserId = userId;
        }
        
        Expense expense = Expense.builder()
                .tripId(tripId)
                .userId(userId)
                .amount(request.getAmount())
                .category(request.getCategory())
                .description(request.getDescription())
                .expenseDate(request.getExpenseDate())
                .payerUserId(finalPayerUserId)
                .numParticipants(request.getNumParticipants())
                .paymentMethod(request.getPaymentMethod())
                .build();
        
        Expense savedExpense = expenseRepository.save(expense);
        return mapToDetailsResponse(savedExpense);
    }
    
    public ExpenseDetailsResponse getExpense(Long tripId, Long expenseId) {
        Expense expense = expenseRepository.findByExpenseIdAndTripId(expenseId, tripId)
                .orElseThrow(() -> new IllegalArgumentException("지출 내역을 찾을 수 없습니다."));
        
        return mapToDetailsResponse(expense);
    }
    
    @Transactional
    public ExpenseDetailsResponse updateExpense(Long tripId, Long expenseId, ExpenseUpdateRequest request) {
        Expense expense = expenseRepository.findByExpenseIdAndTripId(expenseId, tripId)
                .orElseThrow(() -> new IllegalArgumentException("지출 내역을 찾을 수 없습니다."));
        
        if (request.getAmount() != null) {
            expense.setAmount(request.getAmount());
        }
        if (request.getCategory() != null) {
            expense.setCategory(request.getCategory());
        }
        if (request.getDescription() != null) {
            expense.setDescription(request.getDescription());
        }
        if (request.getExpenseDate() != null) {
            expense.setExpenseDate(request.getExpenseDate());
        }
        // PaymentMethod 변경 시 payerUserId 로직 처리
        if (request.getPaymentMethod() != null) {
            expense.setPaymentMethod(request.getPaymentMethod());
            
            if (request.getPaymentMethod() == PaymentMethod.TOGETHER) {
                // TOGETHER로 변경 시 payerUserId가 필요
                if (request.getPayerUserId() != null) {
                    userRepository.findById(request.getPayerUserId())
                            .orElseThrow(() -> new IllegalArgumentException("결제자를 찾을 수 없습니다."));
                    expense.setPayerUserId(request.getPayerUserId());
                } else if (expense.getPayerUserId() == null) {
                    throw new IllegalArgumentException("함께결제로 변경할 때는 결제자 ID가 필요합니다.");
                }
            } else {
                // SEPARATE로 변경 시 기록자가 결제자
                expense.setPayerUserId(expense.getUserId());
            }
        } else if (request.getPayerUserId() != null) {
            // PaymentMethod 변경 없이 payerUserId만 변경하는 경우
            if (expense.getPaymentMethod() == PaymentMethod.TOGETHER) {
                userRepository.findById(request.getPayerUserId())
                        .orElseThrow(() -> new IllegalArgumentException("결제자를 찾을 수 없습니다."));
                expense.setPayerUserId(request.getPayerUserId());
            } else {
                throw new IllegalArgumentException("각자결제에서는 결제자를 변경할 수 없습니다.");
            }
        }
        
        if (request.getNumParticipants() != null) {
            expense.setNumParticipants(request.getNumParticipants());
        }
        
        Expense updatedExpense = expenseRepository.save(expense);
        return mapToDetailsResponse(updatedExpense);
    }
    
    @Transactional
    public void deleteExpense(Long tripId, Long expenseId) {
        Expense expense = expenseRepository.findByExpenseIdAndTripId(expenseId, tripId)
                .orElseThrow(() -> new IllegalArgumentException("지출 내역을 찾을 수 없습니다."));
        
        expenseRepository.delete(expense);
    }
    
    public List<ExpenseSummaryByCategory> getExpenseSummaryByCategory(Long tripId) {
        // Validate trip exists
        tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없습니다."));
        
        return expenseRepository.findExpenseSummaryByCategory(tripId);
    }
    
    public ExpenseSummaryByDate getExpenseSummaryByDate(Long tripId, LocalDate date) {
        // Validate trip exists
        tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없습니다."));
        
        BigDecimal totalAmount = expenseRepository.findTotalAmountByTripIdAndDate(tripId, date)
                .orElse(BigDecimal.ZERO);
        
        return ExpenseSummaryByDate.builder()
                .date(date)
                .totalAmount(totalAmount)
                .build();
    }
    
    public List<ExpenseDailyItem> getDailyExpenses(Long tripId, LocalDate date) {
        // Validate trip exists
        tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없습니다."));
        
        List<Expense> expenses = expenseRepository.findByTripIdAndExpenseDate(tripId, date);
        
        return expenses.stream()
                .map(expense -> {
                    User payer = userRepository.findById(expense.getPayerUserId()).orElse(null);
                    String payerName = (payer != null) ? payer.getNickname() : "알 수 없음";
                    
                    return ExpenseDailyItem.builder()
                            .expenseId(expense.getExpenseId())
                            .description(expense.getDescription())
                            .amount(expense.getAmount())
                            .numParticipants(expense.getNumParticipants())
                            .payer(payerName)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    public List<ExpenseCategory> getAllCategories() {
        return Arrays.asList(ExpenseCategory.values());
    }
    
    private ExpenseDetailsResponse mapToDetailsResponse(Expense expense) {
        return ExpenseDetailsResponse.builder()
                .expenseId(expense.getExpenseId())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .description(expense.getDescription())
                .expenseDate(expense.getExpenseDate())
                .payerUserId(expense.getPayerUserId())
                .numParticipants(expense.getNumParticipants())
                .paymentMethod(expense.getPaymentMethod())
                .build();
    }
}
