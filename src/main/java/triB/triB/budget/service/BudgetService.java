package triB.triB.budget.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triB.triB.auth.repository.UserRepository;
import triB.triB.budget.dto.BudgetCreateRequest;
import triB.triB.budget.dto.BudgetResponse;
import triB.triB.budget.dto.BudgetUpdateRequest;
import triB.triB.budget.entity.TripUserBudget;
import triB.triB.budget.repository.TripUserBudgetRepository;
import triB.triB.schedule.repository.TripRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetService {

    private final TripUserBudgetRepository budgetRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Transactional
    public BudgetResponse createBudget(Long tripId, Long userId, BudgetCreateRequest request) {
        // Validate trip exists
        tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없습니다."));

        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // Check if budget already exists
        if (budgetRepository.existsByTripIdAndUserId(tripId, userId)) {
            throw new IllegalArgumentException("이미 예산이 존재합니다. 수정 API를 사용해주세요.");
        }

        TripUserBudget budget = TripUserBudget.builder()
                .tripId(tripId)
                .userId(userId)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .build();

        TripUserBudget savedBudget = budgetRepository.save(budget);
        return mapToResponse(savedBudget);
    }

    @Transactional
    public BudgetResponse updateBudget(Long tripId, Long userId, BudgetUpdateRequest request) {
        TripUserBudget budget = budgetRepository.findByTripIdAndUserId(tripId, userId)
                .orElseThrow(() -> new IllegalArgumentException("예산을 찾을 수 없습니다. 먼저 예산을 생성해주세요."));

        if (request.getAmount() != null) {
            budget.setAmount(request.getAmount());
        }

        if (request.getCurrency() != null) {
            budget.setCurrency(request.getCurrency());
        }

        TripUserBudget updatedBudget = budgetRepository.save(budget);
        return mapToResponse(updatedBudget);
    }

    public BudgetResponse getMyBudget(Long tripId, Long userId) {
        // Validate trip exists
        tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없습니다."));

        TripUserBudget budget = budgetRepository.findByTripIdAndUserId(tripId, userId)
                .orElseThrow(() -> new IllegalArgumentException("예산을 찾을 수 없습니다."));

        return mapToResponse(budget);
    }

    private BudgetResponse mapToResponse(TripUserBudget budget) {
        return BudgetResponse.builder()
                .budgetId(budget.getTripUserBudgetId())
                .amount(budget.getAmount())
                .currency(budget.getCurrency())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }
}
