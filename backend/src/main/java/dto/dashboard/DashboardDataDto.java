package dto.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import dto.payment.PaymentDto;
import dto.plan.SubscriptionPlanExtendedDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Сводные данные дашборда создателя сообщества (выручка, подписчики, тарифы, последние платежи).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardDataDto {
    private Long communityId;
    private String communityTitle;
    private BigDecimal totalRevenueRub;
    private BigDecimal revenueThisMonthRub;
    private Integer activeSubscribers;
    private Integer plansCount;
    private List<SubscriptionPlanExtendedDto> plans;
    private List<PaymentDto> recentPayments;
}
