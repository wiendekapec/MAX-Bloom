package dto.plan;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Расширенная информация о тарифе для аналитики дашборда.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionPlanExtendedDto {
    private Long id;
    private Long communityId;
    private String title;
    private String description;
    private BigDecimal priceRub;
    private Integer periodDays;
    private Boolean isActive;
    private Integer subscribersCount;
    private BigDecimal totalRevenueRub;
}
