package triB.triB.expense.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonProperty("payer_user_id")
    private Long payerUserId;
    
    @JsonProperty("num_participants")
    private Integer numParticipants;
    
    @JsonProperty("payment_method")
    private PaymentMethod paymentMethod;
}
