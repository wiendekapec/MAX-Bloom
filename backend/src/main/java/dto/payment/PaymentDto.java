package dto.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Информация о платеже.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentDto {
    private Long id;
    private UUID idempotencyKey;
    private Long userId;
    private Long planId;
    private String planTitle;
    private String communityTitle;
    private BigDecimal amountRub;
    private BigDecimal platformFeeRub;
    private PaymentStatus status;
    private Boolean receiptSent;
    private Instant createdAt;
}
