package triB.triB.budget.dto;

import jakarta.validation.constraints.NotNull;
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
public class BudgetCreateRequest {

    @NotNull(message = "예산 금액은 필수입니다.")
    @Positive(message = "예산 금액은 양수여야 합니다.")
    private BigDecimal amount;

    private String currency = "KRW";
}
