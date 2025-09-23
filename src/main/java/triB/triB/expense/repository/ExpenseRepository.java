package triB.triB.expense.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import triB.triB.expense.dto.ExpenseSummaryByCategory;
import triB.triB.expense.entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    Optional<Expense> findByExpenseIdAndTripId(Long expenseId, Long tripId);
    
    List<Expense> findByTripIdAndExpenseDate(Long tripId, LocalDate expenseDate);
    
    @Query("SELECT new triB.triB.expense.dto.ExpenseSummaryByCategory(e.category, SUM(e.amount)) " +
           "FROM Expense e WHERE e.tripId = :tripId GROUP BY e.category")
    List<ExpenseSummaryByCategory> findExpenseSummaryByCategory(@Param("tripId") Long tripId);
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.tripId = :tripId AND e.expenseDate = :date")
    Optional<BigDecimal> findTotalAmountByTripIdAndDate(@Param("tripId") Long tripId, @Param("date") LocalDate date);
}
