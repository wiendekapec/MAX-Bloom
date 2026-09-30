package database.repository;

import database.entity.Subscription;
import dto.subscription.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий подписок пользователей.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserId(Long userId);

    List<Subscription> findByUserMaxUserId(String maxUserId);

    Optional<Subscription> findByUserIdAndCommunityId(Long userId, Long communityId);

    Optional<Subscription> findByUserMaxUserIdAndCommunityId(String maxUserId, Long communityId);

    Optional<Subscription> findFirstByUserMaxUserIdAndCommunityIdAndStatus(
            String maxUserId, Long communityId, SubscriptionStatus status);

    /**
     * Поиск истекших подписок.
     */
    List<Subscription> findByStatusAndExpiresAtBefore(SubscriptionStatus status, Instant time);

    /**
     * Поиск подписок, истекающих в указанный период.
     */
    List<Subscription> findByStatusAndExpiresAtBetween(SubscriptionStatus status, Instant start, Instant end);

    /**
     * Поиск активных подписок сообщества.
     */
    List<Subscription> findByCommunityIdAndStatus(Long communityId, SubscriptionStatus status);

    /**
     * Количество подписчиков сообщества по статусу.
     */
    long countByCommunityIdAndStatus(Long communityId, SubscriptionStatus status);

    /**
     * Количество подписок по тарифному плану.
     */
    long countByPlanId(Long planId);
}
