package dto.bot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Нажатие инлайн-кнопки пользователем (callback query).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaxCallbackDto {
    @JsonProperty("callback_id")
    private String callbackId;

    @JsonProperty("user")
    private MaxUserDto user;

    @JsonProperty("data")
    private String data;

    @JsonProperty("message_id")
    private String messageId;

    @JsonProperty("chat_id")
    private Long chatId;

    public String getPayload() {
        return data;
    }
}
