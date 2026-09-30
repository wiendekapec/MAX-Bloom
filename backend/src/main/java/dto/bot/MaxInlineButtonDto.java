package dto.bot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Инлайн-кнопка для клавиатуры в MAX боте (callback, web_app или ссылка).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaxInlineButtonDto {
    @JsonProperty("text")
    private String text;

    @JsonProperty("callback_data")
    private String callbackData;

    @JsonProperty("url")
    private String url;

    @JsonProperty("web_app")
    private WebAppInfo webApp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebAppInfo {
        @JsonProperty("url")
        private String url;
    }

    public static MaxInlineButtonDto callback(String text, String callbackData) {
        return MaxInlineButtonDto.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    public static MaxInlineButtonDto link(String text, String url) {
        return MaxInlineButtonDto.builder()
                .text(text)
                .url(url)
                .build();
    }

    public static MaxInlineButtonDto webApp(String text, String webAppUrl) {
        return MaxInlineButtonDto.builder()
                .text(text)
                .webApp(new WebAppInfo(webAppUrl))
                .build();
    }
}
