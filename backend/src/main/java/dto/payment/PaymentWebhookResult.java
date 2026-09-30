package dto.payment;

import database.entity.InviteToken;
import database.entity.Payment;
import database.entity.Subscription;

/**
 * Результат атомарной обработки вебхука платежа.
 *
 * @param payment            сущность платежа
 * @param subscription       активированная/продленная подписка (при успехе)
 * @param inviteToken        созданный одноразовый инвайт-токен
 * @param isNewlyProcessed  true если статус изменился именно в этом вызове (защита от дубликатов)
 */
public record PaymentWebhookResult(
        Payment payment,
        Subscription subscription,
        InviteToken inviteToken,
        boolean isNewlyProcessed
) {}
