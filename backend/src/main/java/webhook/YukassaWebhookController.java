package webhook;

import dto.payment.PaymentWebhookResult;
import dto.yukassa.YukassaWebhookDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.BotEventHandler;
import service.PaymentService;

import java.util.Optional;

/**
 * Контроллер вебхуков платёжной системы.
 */
@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class YukassaWebhookController {

    private final PaymentService paymentService;
    private final BotEventHandler botEventHandler;

    /**
     * Обработка входящих уведомлений от платежной системы ЮKassa.
     */
    @PostMapping("/yukassa")
    public ResponseEntity<Void> handle(@RequestBody YukassaWebhookDto webhook) {
        if (webhook.getObject() == null || webhook.getObject().getId() == null) {
            log.warn("YKassa webhook with empty object, skipping");
            return ResponseEntity.ok().build();
        }

        String yukassaPaymentId = webhook.getObject().getId();
        String event = webhook.getEvent();
        log.info("YKassa webhook event={} paymentId={}", event, yukassaPaymentId);

        Optional<PaymentWebhookResult> resultOpt = paymentService.processWebhookAtomically(yukassaPaymentId, event);

        resultOpt.ifPresent(result -> {
            if (result.isNewlyProcessed()) {
                switch (event) {
                    case "payment.succeeded" -> botEventHandler.notifyPaymentSucceeded(
                            result.payment(), result.subscription(), result.inviteToken());
                    case "payment.canceled" -> botEventHandler.notifyPaymentCanceled(result.payment());
                    default -> log.debug("Unhandled YKassa event={}", event);
                }
            } else {
                log.info("Skipping notification for already processed webhook paymentId={}", yukassaPaymentId);
            }
        });

        return ResponseEntity.ok().build();
    }
}
