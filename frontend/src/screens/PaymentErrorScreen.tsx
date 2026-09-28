/**
 * PaymentErrorScreen — ошибка оплаты
 * US-4.4 (must have — рабочая ветка, не мок)
 */

import { useState } from 'react';
import { useNav } from '../contexts/NavContext';
import { api, type Community, type SubscriptionPlan } from '../lib/api';
import { MaxBridge, hapticMedium, hapticError } from '../lib/maxBridge';

const ERROR_MESSAGES: Record<string, string> = {
  PAYMENT_CANCELED: 'Оплата была отменена.',
  PAYMENT_FAILED: 'Платёж не прошёл. Проверьте баланс или попробуйте другую карту.',
  PLAN_INACTIVE: 'Этот тариф больше не продаётся.',
  RATE_LIMIT_EXCEEDED: 'Слишком много попыток. Подождите немного.',
  INSUFFICIENT_FUNDS: 'Недостаточно средств на счёте.',
};

const SUPPORT_LINK = 'https://max.ru/MaxBloomBot'; // deep link to bot support

export default function PaymentErrorScreen() {
  const { current, navigate } = useNav();
  const community = current.params?.community as Community | undefined;
  const plan = current.params?.plan as SubscriptionPlan | undefined;
  const errorCode = (current.params?.errorCode as string | undefined) ?? 'PAYMENT_FAILED';
  const [retrying, setRetrying] = useState(false);

  const message = ERROR_MESSAGES[errorCode] ?? 'Произошла ошибка при оплате.';
  const canRetry = !['PLAN_INACTIVE'].includes(errorCode);

  const handleRetry = async () => {
    if (!plan) return;
    hapticMedium();
    setRetrying(true);
    try {
      const res = await api.createPayment(plan.id);
      const { paymentId, confirmationUrl } = res.data;
      MaxBridge.openLink(confirmationUrl);
      navigate('waiting', { paymentId, community, plan });
    } catch (err: unknown) {
      hapticError();
      const e = err as { message?: string };
      // Stay on this screen with updated error
      navigate('payment-error', {
        community,
        plan,
        errorCode: 'PAYMENT_FAILED',
        errorMessage: e?.message,
      });
    } finally {
      setRetrying(false);
    }
  };

  const handleSupport = () => {
    MaxBridge.openLink(SUPPORT_LINK);
  };

  const handleCatalog = () => {
    navigate('catalog');
  };

  return (
    <div className="result-screen slide-up">
      <div className="result-icon error">✕</div>

      <div className="result-title">Оплата не прошла</div>

      <div className="result-desc">{message}</div>

      {community && plan && (
        <div style={{
          background: 'rgba(255,107,107,0.08)',
          border: '1px solid rgba(255,107,107,0.2)',
          borderRadius: 'var(--radius)',
          padding: '12px 16px',
          width: '100%',
          maxWidth: 320,
          marginTop: 20,
          fontSize: 13,
          color: 'var(--muted)',
          textAlign: 'left',
        }}>
          <div><strong style={{ color: 'var(--text)' }}>{community.title}</strong></div>
          <div style={{ marginTop: 4 }}>{plan.title} · {plan.priceRub.toLocaleString('ru-RU')} ₽</div>
        </div>
      )}

      <div className="result-actions">
        {canRetry && (
          <button
            id="retry-payment"
            className="btn btn-primary btn-full"
            disabled={retrying}
            onClick={handleRetry}
            style={{ padding: '16px' }}
          >
            {retrying ? '⏳ Создаём платёж…' : '🔄 Попробовать снова'}
          </button>
        )}

        <button
          id="contact-support"
          className="btn btn-secondary btn-full"
          onClick={handleSupport}
          style={{ padding: '14px' }}
        >
          💬 Написать в поддержку
        </button>

        <button
          className="btn btn-ghost btn-full"
          onClick={handleCatalog}
          style={{ padding: '12px' }}
        >
          В каталог
        </button>
      </div>

      <div style={{
        fontSize: 11,
        color: 'var(--muted)',
        marginTop: 16,
        textAlign: 'center',
        lineHeight: 1.5,
        maxWidth: 280,
      }}>
        Средства не списаны. При повторной попытке создаётся новый платёж с уникальным ключом идемпотентности.
      </div>
    </div>
  );
}
