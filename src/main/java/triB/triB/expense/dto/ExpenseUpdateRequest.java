package triB.triB.expense.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import triB.triB.expense.entity.ExpenseCategory;
import triB.triB.expense.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseUpdateRequest {
    
    @Positive(message = "지출 금액은 양수여야 합니다.")
    private BigDecimal amount;
    
    private ExpenseCategory category;
    
    private String description;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expenseDate;
    
    private Long payerUserId;
    
    @JsonProperty("num_participants")
    private Integer numParticipants;

    @JsonProperty("payment_method")
    private PaymentMethod paymentMethod;

    private String currency;
}
