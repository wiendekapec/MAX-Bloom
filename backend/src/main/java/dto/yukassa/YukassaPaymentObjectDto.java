package dto.yukassa;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Объект платежа внутри уведомления от ЮKassa.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YukassaPaymentObjectDto {
    private String id;
    private String status;
    private Boolean paid;
    private YukassaAmountDto amount;
    private String description;

    @JsonProperty("created_at")
    private String createdAt;

    private Map<String, String> metadata;
}
