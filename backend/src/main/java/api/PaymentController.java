package api;

import database.entity.InviteToken;
import database.entity.Payment;
import database.entity.Subscription;
import database.entity.User;
import database.repository.InviteTokenRepository;
import database.repository.SubscriptionRepository;
import dto.payment.CreatePaymentRequest;
import dto.payment.CreatePaymentResponse;
import dto.payment.PaymentStatus;
import dto.payment.PaymentStatusResponse;
import dto.subscription.SubscriptionStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.MaxInitDataVerifier;
import service.PaymentService;
import service.UserService;

/**
 * REST-контроллер создания платежей и проверки их статуса.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;
    private final MaxInitDataVerifier initDataVerifier;
    private final SubscriptionRepository subscriptionRepository;
    private final InviteTokenRepository inviteTokenRepository;

    @Value("${app.base-url:http://localhost:80}")
    private String appBaseUrl;

    /**
     * Создание платежа для приобретения тарифного плана.
     */
    @PostMapping
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @RequestHeader("X-Init-Data") String initData,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        String maxUserId = initDataVerifier.verifyAndExtractUserId(initData);
        User user = userService.getByMaxUserId(maxUserId);
        CreatePaymentResponse response = paymentService.createPayment(user, request.getPlanId(), idempotencyKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Получение актуального статуса платежа.
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(
            @RequestHeader("X-Init-Data") String initData,
            @PathVariable Long id
    ) {
        String maxUserId = initDataVerifier.verifyAndExtractUserId(initData);
        Payment payment = paymentService.getById(id);

        if (!payment.getUser().getMaxUserId().equals(maxUserId)) {
            return ResponseEntity.status(403).build();
        }

        String inviteUrl = null;
        java.time.Instant expiresAt = null;

        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            Subscription sub = subscriptionRepository
                    .findFirstByUserMaxUserIdAndCommunityIdAndStatus(
                            maxUserId,
                            payment.getPlan().getCommunity().getId(),
                            SubscriptionStatus.ACTIVE)
                    .orElse(null);

            if (sub != null) {
                expiresAt = sub.getExpiresAt();
                InviteToken token = inviteTokenRepository
                        .findFirstBySubscriptionIdAndStatusOrderByCreatedAtDesc(
                                sub.getId(), database.entity.InviteTokenStatus.ACTIVE)
                        .orElse(null);
                if (token != null) {
                    inviteUrl = appBaseUrl + "/i/" + token.getToken();
                }
            }
        }

        PaymentStatusResponse response = PaymentStatusResponse.builder()
                .paymentId(payment.getId().toString())
                .status(payment.getStatus())
                .inviteUrl(inviteUrl)
                .expiresAt(expiresAt)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Обработка возврата со страницы оплаты.
     */
    @GetMapping("/return")
    public ResponseEntity<Void> returnFromYukassa(
            @RequestParam(value = "payment_id", required = false) String paymentId
    ) {
        log.debug("Payment return callback paymentId={}", paymentId);
        return ResponseEntity.ok().build();
    }
}
