package triB.triB.expense.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import triB.triB.auth.entity.User;
import triB.triB.schedule.entity.Trip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "expenses")
public class Expense {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Long expenseId;
    
    @Column(name = "trip_id", nullable = false)
    private Long tripId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", insertable = false, updatable = false)
    private Trip trip;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;
    
    @Column(name = "payer_user_id", nullable = false)
    private Long payerUserId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_user_id", insertable = false, updatable = false)
    private User payer;
    
    @Builder.Default
    @Column(name = "num_participants", nullable = false)
    private Integer numParticipants = 1;
    
    @Builder.Default
    @Column(name = "payment_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod = PaymentMethod.SEPARATE;
    
    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
