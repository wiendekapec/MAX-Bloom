package dto.bot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Чат или канал в мессенджере MAX.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaxChatDto {
    @JsonProperty("chat_id")
    private Long chatId;

    @JsonProperty("type")
    private String type;

    @JsonProperty("title")
    private String title;

    /**
     * Ссылка на канал.
     */
    @JsonProperty("link")
    private String link;
}
