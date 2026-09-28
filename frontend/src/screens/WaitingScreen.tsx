/**
 * WaitingScreen — polling статуса платежа
 * US-4.2, BOT_AND_MINIAPP_LOGIC п.9
 *
 * Polling каждые 2с, максимум 60с.
 * SUCCEEDED → success screen
 * CANCELED → payment-error screen
 * Timeout → информационное сообщение (webhook всё равно дойдёт)
 */

import { useEffect, useRef, useState } from 'react';
import { useNav } from '../contexts/NavContext';
import { api, type Community, type SubscriptionPlan } from '../lib/api';
import { hapticSuccess, hapticError } from '../lib/maxBridge';

const POLL_INTERVAL_MS = 2000;
const MAX_POLLS = 30; // 30 × 2s = 60s

export default function WaitingScreen() {
  const { current, navigate } = useNav();
  const paymentId = current.params?.paymentId as string;
  const community = current.params?.community as Community;
  const plan = current.params?.plan as SubscriptionPlan;

  const [pollCount, setPollCount] = useState(0);
  const [timedOut, setTimedOut] = useState(false);
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    if (!paymentId) return;

    const poll = async () => {
      try {
        const res = await api.getPaymentStatus(paymentId);
        const { status, inviteUrl, expiresAt } = res.data;

        if (status === 'SUCCEEDED') {
          clearInterval(timerRef.current!);
          hapticSuccess();
          navigate('success', { community, plan, inviteUrl, expiresAt });
          return;
        }

        if (status === 'CANCELED') {
          clearInterval(timerRef.current!);
          hapticError();
          navigate('payment-error', { community, plan, paymentId, errorCode: 'PAYMENT_CANCELED' });
          return;
        }

        // Still PENDING — keep polling
        setPollCount((n) => {
          if (n + 1 >= MAX_POLLS) {
            clearInterval(timerRef.current!);
            setTimedOut(true);
          }
          return n + 1;
        });
      } catch {
        // Network error during poll — continue until timeout
        setPollCount((n) => n + 1);
      }
    };

    timerRef.current = setInterval(poll, POLL_INTERVAL_MS);
    poll(); // immediate first poll

    return () => { if (timerRef.current) clearInterval(timerRef.current); };
  }, [paymentId, community, plan, navigate]);

  if (timedOut) {
    return (
      <div className="waiting-screen fade-in">
        <div className="pulse-ring">🕐</div>
        <div style={{ fontFamily: 'var(--font-display)', fontSize: 18, fontWeight: 700 }}>
          Проверяем оплату…
        </div>
        <div style={{ fontSize: 14, color: 'var(--muted)', lineHeight: 1.6, maxWidth: 280 }}>
          Обработка занимает чуть дольше. Ссылка для вступления придёт в чат с ботом, как только
          оплата подтвердится.
        </div>
        <div style={{ fontSize: 12, color: 'var(--muted)', marginTop: 8 }}>
          Это окно можно закрыть.
        </div>
      </div>
    );
  }

  const progress = Math.min(100, Math.round((pollCount / MAX_POLLS) * 100));

  return (
    <div className="waiting-screen fade-in">
      <div className="pulse-ring">💳</div>
      <div style={{ fontFamily: 'var(--font-display)', fontSize: 18, fontWeight: 700 }}>
        Ожидаем оплату
      </div>
      <div style={{ fontSize: 14, color: 'var(--muted)', lineHeight: 1.6, maxWidth: 280 }}>
        Подтвердите оплату по СБП в приложении вашего банка, затем вернитесь сюда.
      </div>

      {/* Progress bar */}
      <div style={{
        width: '80%',
        maxWidth: 280,
        height: 4,
        background: 'var(--glass-b)',
        borderRadius: 4,
        overflow: 'hidden',
        marginTop: 8,
      }}>
        <div style={{
          height: '100%',
          width: `${progress}%`,
          background: 'var(--bloom)',
          borderRadius: 4,
          transition: 'width 1.8s linear',
        }} />
      </div>

      <div style={{ fontSize: 12, color: 'var(--muted)' }}>
        Проверяем статус…
      </div>
    </div>
  );
}
