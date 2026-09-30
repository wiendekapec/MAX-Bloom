package database.entity;

import dto.payment.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Сущность платежа за подписку.
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_idempotency_key", columnList = "idempotency_key", unique = true),
    @Index(name = "idx_payments_user_id", columnList = "user_id"),
    @Index(name = "idx_payments_plan_id", columnList = "plan_id"),
    @Index(name = "idx_payments_status", columnList = "status"),
    @Index(name = "idx_payments_yukassa_id", columnList = "yukassa_payment_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private UUID idempotencyKey;

    @Column(name = "yukassa_payment_id", length = 64)
    private String yukassaPaymentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Column(name = "amount_rub", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountRub;

    @Column(name = "platform_fee_rub", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal platformFeeRub = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "receipt_sent", nullable = false)
    @Builder.Default
    private Boolean receiptSent = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
