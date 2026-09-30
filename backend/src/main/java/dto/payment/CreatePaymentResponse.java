package dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ответ на создание платежа.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentResponse {
    /**
     * Идентификатор платежа в системе (или ЮKassa).
     */
    private String paymentId;
    /**
     * Ссылка на страницу оплаты (ЮKassa СБП sandbox).
     */
    private String confirmationUrl;
}
