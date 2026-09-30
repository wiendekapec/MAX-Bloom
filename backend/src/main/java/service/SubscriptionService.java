package service;

import database.entity.*;
import database.repository.InviteTokenRepository;
import database.repository.SubscriptionRepository;
import dto.subscription.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Сервис управления подписками пользователей и инвайт-токенами.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final InviteTokenRepository inviteTokenRepository;

    /**
     * Поиск подписки по идентификатору.
     */
    @Transactional(readOnly = true)
    public Subscription getById(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Подписка не найдена: " + id));
    }

    /**
     * Получение всех подписок пользователя.
     */
    @Transactional(readOnly = true)
    public List<Subscription> findMySubscriptions(String maxUserId) {
        return subscriptionRepository.findByUserMaxUserId(maxUserId);
    }

    /**
     * Активация или продление подписки на основе успешного платежа.
     */
    @Transactional
    public Subscription activate(User user, Payment payment) {
        SubscriptionPlan plan = payment.getPlan();
        Community community = plan.getCommunity();

        Instant now = Instant.now();
        Instant expiresAt = plan.getPeriodDays() > 0
                ? now.plus(plan.getPeriodDays(), ChronoUnit.DAYS)
                : null;

        Subscription sub = subscriptionRepository
                .findByUserMaxUserIdAndCommunityId(user.getMaxUserId(), community.getId())
                .orElse(Subscription.builder()
                        .user(user)
                        .community(community)
                        .plan(plan)
                        .build());

        sub.setPlan(plan);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setStartsAt(now);
        sub.setExpiresAt(expiresAt);

        return subscriptionRepository.save(sub);
    }

    /**
     * Создание нового одноразового инвайт-токена со сроком жизни 24 часа.
     */
    @Transactional
    public InviteToken createToken(Subscription sub) {
        Instant now = Instant.now();
        InviteToken token = InviteToken.builder()
                .token(UUID.randomUUID())
                .subscription(sub)
                .status(InviteTokenStatus.ACTIVE)
                .expiresAt(now.plus(24, ChronoUnit.HOURS))
                .build();
        return inviteTokenRepository.save(token);
    }

    /**
     * Перевыпуск инвайт-токена с аннулированием предыдущих активных ссылок.
     */
    @Transactional
    public InviteToken reissueToken(Long subscriptionId, String maxUserId) {
        Subscription sub = getById(subscriptionId);
        if (!sub.getUser().getMaxUserId().equals(maxUserId)) {
            throw new SecurityException("Доступ запрещен: подписка принадлежит другому пользователю");
        }
        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Нельзя перевыпустить токен для неактивной подписки");
        }

        List<InviteToken> activeTokens = inviteTokenRepository.findActiveBySubscriptionId(sub.getId());
        for (InviteToken t : activeTokens) {
            t.setStatus(InviteTokenStatus.REVOKED);
        }
        if (!activeTokens.isEmpty()) {
            inviteTokenRepository.saveAll(activeTokens);
        }

        return createToken(sub);
    }

    /**
     * Атомарное погашение инвайт-токена и получение ссылки на канал.
     */
    @Transactional
    public String consumeToken(UUID token) {
        InviteToken inviteToken = inviteTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Инвайт-токен не найден"));

        Instant now = Instant.now();
        int updated = inviteTokenRepository.consumeTokenAtomically(
                token,
                InviteTokenStatus.USED,
                InviteTokenStatus.ACTIVE,
                now
        );

        if (updated == 0) {
            if (inviteToken.getStatus() == InviteTokenStatus.USED) {
                throw new IllegalStateException("Токен уже был использован");
            }
            if (inviteToken.getStatus() == InviteTokenStatus.EXPIRED || inviteToken.getExpiresAt().isBefore(now)) {
                throw new IllegalStateException("Срок действия токена истек");
            }
            throw new IllegalStateException("Токен недействителен: " + inviteToken.getStatus());
        }

        String link = inviteToken.getSubscription().getCommunity().getInviteLink();
        if (link == null || link.isBlank()) {
            throw new IllegalStateException("Ссылка на сообщество не настроена");
        }
        return link;
    }

    /**
     * Поиск подписок, срок которых истекает в ближайшие дни.
     */
    @Transactional(readOnly = true)
    public List<Subscription> findExpiringSoon(int withinDays) {
        Instant now = Instant.now();
        return subscriptionRepository.findByStatusAndExpiresAtBetween(
                SubscriptionStatus.ACTIVE, now, now.plus(withinDays, ChronoUnit.DAYS));
    }
}
