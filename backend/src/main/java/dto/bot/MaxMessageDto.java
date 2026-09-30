package dto.bot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Сообщение в чате MAX.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaxMessageDto {
    @JsonProperty("message_id")
    private String messageId;

    @JsonProperty("sender")
    private MaxUserDto sender;

    @JsonProperty("chat")
    private MaxChatDto chat;

    @JsonProperty("text")
    private String text;

    @JsonProperty("payload")
    private String payload;

    @JsonProperty("timestamp")
    private Long timestamp;
}
