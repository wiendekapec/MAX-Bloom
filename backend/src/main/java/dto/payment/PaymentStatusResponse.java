package dto.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Ответ со статусом платежа при polling из мини-приложения.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentStatusResponse {
    private String paymentId;
    private PaymentStatus status;
    /**
     * Ссылка доступа (https://bloom.example/i/{token}).
     * Выдаётся только при статусе SUCCEEDED.
     */
    private String inviteUrl;
    private Instant expiresAt;

    public PaymentStatusResponse(String paymentId, PaymentStatus status) {
        this.paymentId = paymentId;
        this.status = status;
    }
}
