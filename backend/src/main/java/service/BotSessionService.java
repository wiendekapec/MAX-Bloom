package service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import database.entity.BotSession;
import database.repository.BotSessionRepository;
import dto.community.CommunityCategory;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Сервис сессий FSM онбординга сообществ и продавцов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BotSessionService {

    public enum State {
        ONBOARDING_INFO,
        SELF_EMPLOYED_CONFIRM,
        ASK_TITLE,
        ASK_DESCRIPTION,
        ASK_CATEGORY,
        ASK_PLAN_TITLE,
        ASK_PLAN_PRICE,
        ASK_PLAN_PERIOD,
        WAIT_BOT_ADDED,
        DONE
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Draft {
        private String title;
        private String description;
        private CommunityCategory category;
        private String planTitle;
        private String planDescription;
        private BigDecimal planPrice;
        private Integer planPeriodDays;
    }

    @Value
    public static class PendingChatEntry {
        String maxUserId;
        Draft draft;
    }

    private final BotSessionRepository sessionRepository;
    private final ObjectMapper objectMapper;

    /**
     * Получение текущего состояния FSM для пользователя.
     */
    @Transactional(readOnly = true)
    public State getState(String maxUserId) {
        return sessionRepository.findByMaxUserId(maxUserId)
                .map(s -> State.valueOf(s.getState()))
                .orElse(State.ONBOARDING_INFO);
    }

    /**
     * Получение черновика онбординга.
     */
    @Transactional(readOnly = true)
    public Draft getDraft(String maxUserId) {
        return sessionRepository.findByMaxUserId(maxUserId)
                .map(s -> deserialize(s.getDraftJson()))
                .orElse(new Draft());
    }

    /**
     * Сохранение состояния FSM и черновика.
     */
    @Transactional
    public void save(String maxUserId, State state, Draft draft) {
        save(maxUserId, state, draft, null);
    }

    @Transactional
    public void save(String maxUserId, State state, Draft draft, String pendingChatId) {
        BotSession session = sessionRepository.findByMaxUserId(maxUserId)
                .orElse(BotSession.builder().maxUserId(maxUserId).build());

        session.setState(state.name());
        session.setDraftJson(serialize(draft));
        session.setPendingChatId(pendingChatId);

        sessionRepository.save(session);
        log.debug("FSM saved maxUserId={} state={}", maxUserId, state);
    }

    /**
     * Удаление сессии после завершения онбординга или отмены.
     */
    @Transactional
    public void clear(String maxUserId) {
        sessionRepository.deleteByMaxUserId(maxUserId);
        log.debug("FSM cleared maxUserId={}", maxUserId);
    }

    /**
     * Поиск сессии, ожидающей привязки указанного чата.
     */
    @Transactional(readOnly = true)
    public Optional<PendingChatEntry> findByPendingChat(String chatId) {
        return sessionRepository.findByPendingChatId(chatId)
                .map(s -> new PendingChatEntry(s.getMaxUserId(), deserialize(s.getDraftJson())));
    }

    private String serialize(Draft draft) {
        if (draft == null) return null;
        try {
            return objectMapper.writeValueAsString(draft);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize draft: {}", e.getMessage());
            return null;
        }
    }

    private Draft deserialize(String json) {
        if (json == null || json.isBlank()) return new Draft();
        try {
            return objectMapper.readValue(json, Draft.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize draft json: {}", e.getMessage());
            return new Draft();
        }
    }
}
