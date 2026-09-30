package database.repository;

import database.entity.InviteToken;
import database.entity.InviteTokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий одноразовых инвайт-токенов.
 */
@Repository
public interface InviteTokenRepository extends JpaRepository<InviteToken, Long> {

    /**
     * Атомарное использование токена по алгоритму CAS.
     */
    @Modifying
    @Query("UPDATE InviteToken t SET t.status = :newStatus, t.usedAt = :now " +
           "WHERE t.token = :token AND t.status = :expectedStatus AND t.expiresAt > :now")
    int consumeTokenAtomically(
            @Param("token") UUID token,
            @Param("newStatus") InviteTokenStatus newStatus,
            @Param("expectedStatus") InviteTokenStatus expectedStatus,
            @Param("now") Instant now);

    /**
     * Поиск токена по значению UUID.
     */
    Optional<InviteToken> findByToken(UUID token);

    /**
     * Поиск последнего активного токена подписки.
     */
    Optional<InviteToken> findFirstBySubscriptionIdAndStatusOrderByCreatedAtDesc(
            Long subscriptionId, InviteTokenStatus status);

    /**
     * Поиск просроченных токенов с заданным статусом.
     */
    List<InviteToken> findByExpiresAtBeforeAndStatus(Instant time, InviteTokenStatus status);

    List<InviteToken> findBySubscriptionId(Long subscriptionId);

    /**
     * Поиск всех активных токенов подписки.
     */
    @Query("SELECT t FROM InviteToken t WHERE t.subscription.id = :subId AND t.status = 'ACTIVE'")
    List<InviteToken> findActiveBySubscriptionId(@Param("subId") Long subscriptionId);
}
