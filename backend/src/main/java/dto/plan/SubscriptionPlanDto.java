package dto.plan;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Тариф подписки на сообщество.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionPlanDto {
    private Long id;
    private Long communityId;
    private String title;
    private String description;
    private BigDecimal priceRub;
    /**
     * Период в днях: 0 = разово, 7, 30, 90 дней.
     */
    private Integer periodDays;
    private Boolean isActive;
    private Integer subscribersCount;
}
