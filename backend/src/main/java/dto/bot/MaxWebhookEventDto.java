package dto.bot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Входящее событие вебхука платформы MAX (message_created, message_callback, bot_added, etc.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaxWebhookEventDto {
    /**
     * Тип события: message_created, message_callback, bot_added, bot_removed, user_added.
     */
    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("type")
    private String type;

    @JsonProperty("chat")
    private MaxChatDto chat;

    @JsonProperty("user")
    private MaxUserDto user;

    @JsonProperty("message")
    private MaxMessageDto message;

    @JsonProperty("callback")
    private MaxCallbackDto callback;

    @JsonProperty("timestamp")
    private Long timestamp;

    /**
     * Вспомогательный метод для получения реального типа события независимо от формата.
     */
    public String resolveEventType() {
        if (eventType != null && !eventType.isBlank()) {
            return eventType;
        }
        return type;
    }
}
