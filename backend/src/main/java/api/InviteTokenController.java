package api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import service.SubscriptionService;

import java.net.URI;
import java.util.UUID;

/**
 * REST-контроллер для обработки перехода по одноразовым инвайт-ссылкам.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class InviteTokenController {

    private final SubscriptionService subscriptionService;

    @Value("${app.base-url:http://localhost:80}")
    private String appBaseUrl;

    /**
     * Обработка перехода по инвайт-токену с перенаправлением в чат или сообщество.
     */
    @GetMapping("/i/{token}")
    public ResponseEntity<Void> redirect(@PathVariable String token) {
        UUID uuid;
        try {
            uuid = UUID.fromString(token);
        } catch (IllegalArgumentException e) {
            log.warn("Некорректный формат токена инвайта: {}", token);
            return ResponseEntity.badRequest().build();
        }

        try {
            String inviteLink = subscriptionService.consumeToken(uuid);
            log.info("Инвайт-токен {} успешно активирован, перенаправление на {}", token, inviteLink);

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(inviteLink));
            return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();

        } catch (IllegalArgumentException e) {
            log.warn("Инвайт-токен не найден: {}", token);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (IllegalStateException e) {
            log.warn("Инвайт-токен {} недействителен: {}", token, e.getMessage());
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(appBaseUrl + "?error=token_expired"));
            return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
        }
    }
}
