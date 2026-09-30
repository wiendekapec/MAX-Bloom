package dto.bot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Запрос к MAX API на отправку сообщения пользователю или в чат.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaxSendMessageRequest {
    @JsonProperty("chat_id")
    private Long chatId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("text")
    private String text;

    @JsonProperty("inline_keyboard")
    private List<List<MaxInlineButtonDto>> inlineKeyboard;
}
