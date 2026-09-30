package dto.payment;

import database.entity.InviteToken;
import database.entity.Payment;
import database.entity.Subscription;

/**
 * Результат обработки вебхука платежа.
 */
public record PaymentWebhookResult(
        Payment payment,
        Subscription subscription,
        InviteToken inviteToken,
        boolean isNewlyProcessed
) {}
