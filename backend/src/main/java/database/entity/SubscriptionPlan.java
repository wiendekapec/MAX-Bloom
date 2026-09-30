package database.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Сущность тарифного плана подписки на сообщество.
 */
@Entity
@Table(name = "subscription_plans", indexes = {
    @Index(name = "idx_subscription_plans_community_id", columnList = "community_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @Column(name = "title", nullable = false, length = 128)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_rub", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "period_days", nullable = false)
    private Integer periodDays;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Получение цены в рублях.
     */
    public BigDecimal getPriceRub() {
        return price;
    }

    /**
     * Установка цены в рублях.
     */
    public void setPriceRub(BigDecimal priceRub) {
        this.price = priceRub;
    }
}
