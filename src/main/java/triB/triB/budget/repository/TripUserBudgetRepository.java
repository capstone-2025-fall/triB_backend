package triB.triB.budget.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import triB.triB.budget.entity.TripUserBudget;

import java.util.Optional;

@Repository
public interface TripUserBudgetRepository extends JpaRepository<TripUserBudget, Long> {

    // 인덱스 활용: uq_tub_trip_user (trip_id, user_id)
    Optional<TripUserBudget> findByTripIdAndUserId(Long tripId, Long userId);

    // 예산 존재 여부 확인
    boolean existsByTripIdAndUserId(Long tripId, Long userId);
}
