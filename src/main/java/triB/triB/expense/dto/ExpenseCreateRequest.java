package triB.triB.expense.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
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
public class ExpenseCreateRequest {

    @NotNull(message = "지출 금액은 필수입니다.")
    @Positive(message = "지출 금액은 양수여야 합니다.")
    private BigDecimal amount;

    @NotNull(message = "카테고리는 필수입니다.")
    private ExpenseCategory category;

    private String description;

    @NotNull(message = "지출 날짜는 필수입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expenseDate;

    // TOGETHER일 때만 필수, SEPARATE일 때는 null 가능
    private String payerNickname;

    private Integer numParticipants = 1;

    private PaymentMethod paymentMethod = PaymentMethod.SEPARATE;

    private String currency = "KRW";
}
