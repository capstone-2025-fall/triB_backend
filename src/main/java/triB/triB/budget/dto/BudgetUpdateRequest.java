package triB.triB.budget.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetUpdateRequest {

    @Positive(message = "예산 금액은 양수여야 합니다.")
    private BigDecimal amount;

    private String currency;
}
