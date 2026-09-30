package service;

import database.entity.*;
import database.repository.*;
import dto.payment.CreatePaymentResponse;
import dto.payment.PaymentStatus;
import dto.payment.PaymentWebhookResult;
import dto.subscription.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис управления платежами и обработкой финансовых транзакций.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final CommunityRepository communityRepository;
    private final InviteTokenRepository inviteTokenRepository;
    private final YukassaClient yukassaClient;

    @Value("${app.platform-commission-rate:0.05}")
    private BigDecimal platformCommissionRate;

    @Value("${app.base-url:http://localhost:80}")
    private String appBaseUrl;

    /**
     * Создание платежа за тарифный план (заглушка — оплата всегда подтверждается мгновенно).
     */
    @Transactional
    public CreatePaymentResponse createPayment(User user, Long planId, String idempotencyKeyStr) {
        UUID idempotencyKey = idempotencyKeyStr != null && !idempotencyKeyStr.isBlank()
                ? UUID.fromString(idempotencyKeyStr)
                : UUID.randomUUID();

        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Тариф не найден: " + planId));

        BigDecimal fee = plan.getPrice().multiply(platformCommissionRate).setScale(2, RoundingMode.HALF_UP);

        Payment payment = Payment.builder()
                .idempotencyKey(idempotencyKey)
                .user(user)
                .plan(plan)
                .amountRub(plan.getPrice())
                .platformFeeRub(fee)
                .status(PaymentStatus.SUCCEEDED)
                .receiptSent(true)
                .build();
        payment = paymentRepository.save(payment);

        Instant now = Instant.now();
        Instant expiresAt = plan.getPeriodDays() > 0
                ? now.plus(plan.getPeriodDays(), ChronoUnit.DAYS)
                : null;

        Subscription sub = subscriptionRepository
                .findByUserMaxUserIdAndCommunityId(user.getMaxUserId(), plan.getCommunity().getId())
                .orElse(Subscription.builder()
                        .user(user)
                        .community(plan.getCommunity())
                        .plan(plan)
                        .build());

        sub.setPlan(plan);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setStartsAt(now);
        sub.setExpiresAt(expiresAt);
        Subscription savedSub = subscriptionRepository.save(sub);

        InviteToken token = InviteToken.builder()
                .token(UUID.randomUUID())
                .subscription(savedSub)
                .status(InviteTokenStatus.ACTIVE)
                .expiresAt(now.plus(24, ChronoUnit.HOURS))
                .build();
        inviteTokenRepository.save(token);

        Community comm = plan.getCommunity();
        comm.setSubscribersCount(comm.getSubscribersCount() + 1);
        communityRepository.save(comm);

        String inviteUrl = appBaseUrl + "/i/" + token.getToken();
        log.info("Stub payment succeeded immediately for user={} plan={}", user.getMaxUserId(), plan.getTitle());

        return new CreatePaymentResponse(payment.getId().toString(), inviteUrl);
    }

    /**
     * Создание или получение существующего незавершенного (PENDING) платежа за тарифный план.
     */
    @Transactional
    public CreatePaymentResponse createOrGetPending(User user, Long planId) {
        return createPayment(user, planId, null);
    }

    /**
     * Атомарная обработка вебхука платежа ЮKassa.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Optional<PaymentWebhookResult> processWebhookAtomically(String yukassaPaymentId, String eventType) {
        try {
            return Optional.of(processWebhookAtomically(yukassaPaymentId, eventType, null));
        } catch (IllegalArgumentException e) {
            log.warn("Платеж не найден при обработке вебхука: {}", yukassaPaymentId);
            return Optional.empty();
        }
    }

    /**
     * Атомарная обработка вебхука платежа ЮKassa с пессимистической блокировкой.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public PaymentWebhookResult processWebhookAtomically(
            String yukassaPaymentId,
            String eventType,
            BigDecimal capturedAmount
    ) {
        Payment payment = paymentRepository.findByYukassaPaymentIdWithLock(yukassaPaymentId)
                .orElseThrow(() -> new IllegalArgumentException("Платеж не найден: " + yukassaPaymentId));

        if ("payment.succeeded".equals(eventType)) {
            if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
                Subscription sub = subscriptionRepository
                        .findFirstByUserMaxUserIdAndCommunityIdAndStatus(
                                payment.getUser().getMaxUserId(),
                                payment.getPlan().getCommunity().getId(),
                                SubscriptionStatus.ACTIVE)
                        .orElse(null);
                InviteToken token = sub != null
                        ? inviteTokenRepository.findFirstBySubscriptionIdAndStatusOrderByCreatedAtDesc(
                                sub.getId(), database.entity.InviteTokenStatus.ACTIVE).orElse(null)
                        : null;
                return new PaymentWebhookResult(payment, sub, token, false);
            }

            payment.setStatus(PaymentStatus.SUCCEEDED);
            paymentRepository.save(payment);

            SubscriptionPlan plan = payment.getPlan();
            Community community = plan.getCommunity();
            User user = payment.getUser();

            Instant now = Instant.now();
            Instant expiresAt = plan.getPeriodDays() > 0
                    ? now.plus(plan.getPeriodDays(), ChronoUnit.DAYS)
                    : null;

            Subscription sub = subscriptionRepository
                    .findByUserMaxUserIdAndCommunityId(user.getMaxUserId(), community.getId())
                    .orElse(Subscription.builder()
                            .user(user)
                            .community(community)
                            .plan(plan)
                            .build());

            sub.setPlan(plan);
            sub.setStatus(SubscriptionStatus.ACTIVE);
            sub.setStartsAt(now);
            sub.setExpiresAt(expiresAt);
            Subscription savedSub = subscriptionRepository.save(sub);

            InviteToken token = InviteToken.builder()
                    .token(UUID.randomUUID())
                    .subscription(savedSub)
                    .status(database.entity.InviteTokenStatus.ACTIVE)
                    .expiresAt(now.plus(24, ChronoUnit.HOURS))
                    .build();
            InviteToken savedToken = inviteTokenRepository.save(token);

            return new PaymentWebhookResult(payment, savedSub, savedToken, true);

        } else if ("payment.canceled".equals(eventType)) {
            if (payment.getStatus() != PaymentStatus.CANCELED) {
                payment.setStatus(PaymentStatus.CANCELED);
                paymentRepository.save(payment);
                return new PaymentWebhookResult(payment, null, null, true);
            }
            return new PaymentWebhookResult(payment, null, null, false);
        }

        return new PaymentWebhookResult(payment, null, null, false);
    }

    /**
     * Поиск платежа по первичному ключу.
     */
    @Transactional(readOnly = true)
    public Payment getById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Платеж не найден: " + id));
    }

    /**
     * Поиск платежа по идентификатору в ЮKassa.
     */
    @Transactional(readOnly = true)
    public Optional<Payment> findByYukassaPaymentId(String yukassaPaymentId) {
        return paymentRepository.findByYukassaPaymentId(yukassaPaymentId);
    }

    /**
     * Получение списка платежей пользователя по его идентификатору.
     */
    @Transactional(readOnly = true)
    public List<Payment> findUserPayments(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    /**
     * Получение списка платежей пользователя по идентификатору платформы MAX.
     */
    @Transactional(readOnly = true)
    public List<Payment> findUserPaymentsByMaxUserId(String maxUserId) {
        return paymentRepository.findByUserMaxUserIdOrderByCreatedAtDesc(maxUserId);
    }

    /**
     * Отметка об отправке фискального чека.
     */
    @Transactional
    public void markReceiptSent(Long paymentId) {
        paymentRepository.findById(paymentId).ifPresent(p -> {
            p.setReceiptSent(true);
            log.info("Receipt marked sent paymentId={}", paymentId);
        });
    }
}
