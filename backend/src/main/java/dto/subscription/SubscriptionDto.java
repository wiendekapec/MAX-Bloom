package dto.subscription;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Подписка пользователя на сообщество.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionDto {
    private Long id;
    private Long communityId;
    private String communityTitle;
    private Long planId;
    private String planTitle;
    private BigDecimal priceRub;
    private SubscriptionStatus status;
    private Instant startsAt;
    private Instant expiresAt;
    private Integer daysLeft;
}
