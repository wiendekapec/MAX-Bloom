package service;

import database.entity.InviteToken;
import database.entity.InviteTokenStatus;
import database.entity.Subscription;
import database.repository.BotSessionRepository;
import database.repository.InviteTokenRepository;
import database.repository.SubscriptionRepository;
import dto.subscription.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Планировщик фоновых периодических задач для управления подписками и сессиями.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final InviteTokenRepository inviteTokenRepository;
    private final BotSessionRepository botSessionRepository;
    private final MaxBotClient botClient;

    /**
     * Проверка и деактивация истекших подписок каждый час.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void processExpiredSubscriptions() {
        List<Subscription> expired = subscriptionRepository
                .findByStatusAndExpiresAtBefore(SubscriptionStatus.ACTIVE, Instant.now());

        for (Subscription sub : expired) {
            sub.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(sub);

            try {
                String text = "Срок действия вашей подписки на «" + sub.getCommunity().getTitle() + "» истек.";
                botClient.sendMessage(sub.getUser().getMaxUserId(), text);
            } catch (Exception e) {
                log.warn("Не удалось отправить уведомление об истечении подписки subId={}: {}", sub.getId(), e.getMessage());
            }
        }

        if (!expired.isEmpty()) {
            log.info("Обработано {} истекших подписок", expired.size());
        }
    }

    /**
     * Пометка просроченных инвайт-токенов каждые 30 минут.
     */
    @Scheduled(cron = "0 30 * * * *")
    @Transactional
    public void markExpiredTokens() {
        List<InviteToken> tokens = inviteTokenRepository
                .findByExpiresAtBeforeAndStatus(Instant.now(), InviteTokenStatus.ACTIVE);
        tokens.forEach(t -> t.setStatus(InviteTokenStatus.EXPIRED));
        if (!tokens.isEmpty()) {
            inviteTokenRepository.saveAll(tokens);
            log.info("Отмечено {} истекших инвайт-токенов", tokens.size());
        }
    }

    /**
     * Очистка неактивных FSM-сессий онбординга старше 1 часа.
     */
    @Scheduled(cron = "0 45 * * * *")
    @Transactional
    public void clearStaleBotSessions() {
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        int deleted = botSessionRepository.deleteStale(oneHourAgo);
        if (deleted > 0) {
            log.info("Удалено {} устаревших FSM-сессий", deleted);
        }
    }
}
