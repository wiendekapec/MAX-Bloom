package service;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * HTTP-клиент для взаимодействия с API ЮKassa.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YukassaClient {

    private final RestTemplate restTemplate;

    @Value("${yukassa.shop-id:}")
    private String shopId;

    @Value("${yukassa.secret-key:}")
    private String secretKey;

    @Value("${yukassa.api-base:https://api.yookassa.ru/v3/payments}")
    private String baseUrl;

    /**
     * Создание платежа в ЮKassa.
     */
    public YukassaCreateResponse createPayment(
            UUID idempotencyKey,
            BigDecimal amount,
            String description,
            String returnUrl,
            String maxUserId,
            Long planId
    ) {
        HttpHeaders headers = buildHeaders(idempotencyKey.toString());

        Map<String, Object> body = new LinkedHashMap<>();
        Map<String, Object> amountMap = Map.of("value", amount.toPlainString(), "currency", "RUB");
        Map<String, Object> confirmation = Map.of(
                "type", "redirect",
                "return_url", returnUrl
        );
        Map<String, String> metadata = Map.of(
                "max_user_id", maxUserId,
                "plan_id", String.valueOf(planId)
        );

        body.put("amount", amountMap);
        body.put("confirmation", confirmation);
        body.put("capture", true);
        body.put("description", description);
        body.put("metadata", metadata);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl, HttpMethod.POST, request, Map.class);

            Map<?, ?> resp = response.getBody();
            String paymentId = (String) resp.get("id");
            Map<?, ?> confirmObj = (Map<?, ?>) resp.get("confirmation");
            String confirmUrl = (String) confirmObj.get("confirmation_url");

            log.info("YKassa payment created id={} amount={}", paymentId, amount);
            return new YukassaCreateResponse(paymentId, confirmUrl);

        } catch (Exception e) {
            log.error("YKassa createPayment failed idempotencyKey={}: {}", idempotencyKey, e.getMessage(), e);
            throw new RuntimeException("PAYMENT_FAILED: " + e.getMessage(), e);
        }
    }

    /**
     * Получение URL для подтверждения существующего платежа.
     */
    public String getConfirmationUrl(String yukassaPaymentId) {
        HttpHeaders headers = buildHeaders(null);
        HttpEntity<?> req = new HttpEntity<>(headers);
        try {
            ResponseEntity<Map> resp = restTemplate.exchange(
                    baseUrl + "/" + yukassaPaymentId, HttpMethod.GET, req, Map.class);
            Map<?, ?> body = resp.getBody();
            Map<?, ?> confirmation = (Map<?, ?>) body.get("confirmation");
            return confirmation != null ? (String) confirmation.get("confirmation_url") : null;
        } catch (Exception e) {
            log.warn("YKassa getConfirmationUrl failed yukassaId={}: {}", yukassaPaymentId, e.getMessage());
            return null;
        }
    }

    private HttpHeaders buildHeaders(String idempotencyKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String creds = shopId + ":" + secretKey;
        String encoded = Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encoded);

        if (idempotencyKey != null) {
            headers.set("Idempotence-Key", idempotencyKey);
        }
        return headers;
    }

    @Data
    @Builder
    public static class YukassaCreateResponse {
        private final String paymentId;
        private final String confirmationUrl;
    }
}
