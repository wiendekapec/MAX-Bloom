package database.repository;

import database.entity.BotSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface BotSessionRepository extends JpaRepository<BotSession, Long> {

    Optional<BotSession> findByMaxUserId(String maxUserId);

    /**
     * Поиск сессии, ожидающей привязки конкретного чата (bot_added event).
     * Использует частичный индекс idx_bot_sessions_pending_chat.
     */
    Optional<BotSession> findByPendingChatId(String pendingChatId);

    void deleteByMaxUserId(String maxUserId);

    /**
     * Удалить зависшие сессии (TTL: 1 час без активности) — для SubscriptionScheduler.
     */
    @Modifying
    @Query("DELETE FROM BotSession s WHERE s.updatedAt < :before")
    int deleteStale(@Param("before") Instant before);
}
