package database.repository;

import database.entity.Subscription;
import dto.subscription.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserId(Long userId);

    List<Subscription> findByUserMaxUserId(String maxUserId);

    Optional<Subscription> findByUserIdAndCommunityId(Long userId, Long communityId);

    Optional<Subscription> findByUserMaxUserIdAndCommunityId(String maxUserId, Long communityId);

    Optional<Subscription> findFirstByUserMaxUserIdAndCommunityIdAndStatus(
            String maxUserId, Long communityId, SubscriptionStatus status);

    /**
     * Поиск истекших подписок для шедулера кика участников.
     */
    List<Subscription> findByStatusAndExpiresAtBefore(SubscriptionStatus status, Instant time);

    /**
     * Поиск подписок, истекающих в заданном временном окне (для превентивных напоминаний).
     */
    List<Subscription> findByStatusAndExpiresAtBetween(SubscriptionStatus status, Instant start, Instant end);

    /**
     * Список активных подписок сообщества (для периодической сверки списка участников канала).
     */
    List<Subscription> findByCommunityIdAndStatus(Long communityId, SubscriptionStatus status);

    /**
     * Количество активных подписчиков сообщества (для дашборда крейтора).
     */
    long countByCommunityIdAndStatus(Long communityId, SubscriptionStatus status);

    /**
     * Проверка наличия связанных подписок перед удалением тарифа (ФЗ-54 / правило удаления).
     */
    long countByPlanId(Long planId);
}
