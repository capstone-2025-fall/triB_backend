package triB.triB.expense.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triB.triB.expense.entity.ExpenseCategory;
import triB.triB.expense.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDetailsResponse {

    private Long expenseId;
    private BigDecimal amount;
    private ExpenseCategory category;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expenseDate;

    private String payerNickname;

    private Integer numParticipants;

    private PaymentMethod paymentMethod;

    private String currency;
}
