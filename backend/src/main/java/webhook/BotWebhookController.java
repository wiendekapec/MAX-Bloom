package webhook;

import dto.bot.MaxWebhookEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.BotEventHandler;

/**
 * Контроллер приема входящих вебхуков от платформы MAX Bot.
 */
@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class BotWebhookController {

    private final BotEventHandler botEventHandler;

    @Value("${max.bot.secret:}")
    private String botSecret;

    /**
     * Обработка событий от платформы MAX.
     */
    @PostMapping("/max")
    public ResponseEntity<Void> handleMaxWebhook(
            @RequestBody MaxWebhookEventDto event,
            @RequestHeader(value = "X-Max-Bot-Api-Secret", required = false) String secretHeader,
            @RequestHeader(value = "X-Max-Bot-Secret", required = false) String secretHeader2) {

        String incoming = secretHeader != null ? secretHeader : secretHeader2;
        if (botSecret != null && !botSecret.isBlank() && !botSecret.equals("replace_me_secret")) {
            if (!botSecret.equals(incoming)) {
                log.warn("Webhook secret mismatch, rejecting request");
                return ResponseEntity.status(401).build();
            }
        }

        String eventType = event.resolveEventType();
        log.info("MAX webhook event={}", eventType);

        try {
            switch (eventType) {
                case "message_created" -> {
                    if (event.getUser() != null) {
                        String text = event.getMessage() != null
                                ? event.getMessage().getText()
                                : null;
                        botEventHandler.handleMessage(event.getUser(), text);
                    }
                }
                case "message_callback" -> {
                    if (event.getUser() != null && event.getCallback() != null) {
                        botEventHandler.handleCallback(event.getUser(), event.getCallback().getPayload());
                    }
                }
                case "bot_added" -> {
                    String chatId = (event.getChat() != null && event.getChat().getChatId() != null)
                            ? String.valueOf(event.getChat().getChatId())
                            : null;
                    if (chatId != null) {
                        botEventHandler.handleBotAdded(chatId);
                    }
                }
                case "bot_removed" -> {
                    log.info("Bot removed from chat={}", event.getChat() != null ? event.getChat().getChatId() : "?");
                }
                default -> log.debug("Unhandled MAX event type={}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing MAX webhook event={}: {}", eventType, e.getMessage(), e);

        }

        return ResponseEntity.ok().build();
    }
}
