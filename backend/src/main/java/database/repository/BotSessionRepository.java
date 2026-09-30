package database.repository;

import database.entity.BotSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * Репозиторий сессий онбординга бота.
 */
@Repository
public interface BotSessionRepository extends JpaRepository<BotSession, Long> {

    Optional<BotSession> findByMaxUserId(String maxUserId);

    /**
     * Поиск сессии по ожидающему чату.
     */
    Optional<BotSession> findByPendingChatId(String pendingChatId);

    void deleteByMaxUserId(String maxUserId);

    /**
     * Удаление устаревших сессий онбординга.
     */
    @Modifying
    @Query("DELETE FROM BotSession s WHERE s.updatedAt < :before")
    int deleteStale(@Param("before") Instant before);
}
