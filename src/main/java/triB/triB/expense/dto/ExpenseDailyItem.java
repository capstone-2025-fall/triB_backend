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
public class ExpenseDailyItem {

    private Long expenseId;
    private String description;
    private BigDecimal amount;
    private ExpenseCategory category;
    private String settlementInfo;
    private Boolean isSettled;
}
