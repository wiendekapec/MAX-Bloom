package dto.yukassa;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Уведомление вебхука от ЮKassa (payment.succeeded, payment.canceled, etc.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YukassaWebhookDto {
    private String type;
    private String event;
    private YukassaPaymentObjectDto object;
}
