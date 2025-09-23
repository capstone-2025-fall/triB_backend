package triB.triB.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triB.triB.expense.entity.ExpenseCategory;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSummaryByCategory {
    
    private ExpenseCategory category;
    private BigDecimal totalAmount;
}
