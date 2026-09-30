package database.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Сущность сессии онбординга автора.
 */
@Entity
@Table(name = "bot_sessions",
       uniqueConstraints = @UniqueConstraint(name = "uq_bot_sessions_max_user_id", columnNames = "max_user_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "max_user_id", nullable = false, unique = true, length = 64)
    private String maxUserId;

    /**
     * Текущее состояние FSM (например, "ASK_TITLE").
     */
    @Column(name = "state", nullable = false, length = 64)
    private String state;

    /**
     * JSON-сериализованный черновик (название, описание, цена, категория и т.д.).
     * Тип: TEXT — не JSONB, чтобы не создавать зависимость от диалекта Postgres в коде.
     */
    @Column(name = "draft_json", columnDefinition = "TEXT")
    private String draftJson;

    /**
     * maxChatId канала, ожидающего привязки бота (состояние WAIT_BOT_ADDED).
     * Индексируется для быстрого поиска при получении события bot_added.
     */
    @Column(name = "pending_chat_id", length = 64)
    private String pendingChatId;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }
}
