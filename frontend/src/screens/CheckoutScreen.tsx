import { useState } from 'react';
import { TopBar, SbpBadge, MainBtn } from '../components/ui';
import { useNav } from '../contexts/NavContext';
import { api, type Community, type SubscriptionPlan } from '../lib/api';
import { formatRub, formatPeriod } from '../lib/helpers';
import { hapticMedium, hapticError, MaxBridge } from '../lib/maxBridge';

export default function CheckoutScreen() {
  const { current, navigate, goBack } = useNav();
  const community = current.params?.community as Community;
  const plan = current.params?.plan as SubscriptionPlan;
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!community || !plan) {
    return (
      <div className="screen fade-in">
        <TopBar title="Оплата" onBack={goBack} />
        <div style={{ padding: 40, textAlign: 'center', color: 'var(--muted)' }}>
          Данные не найдены. Вернитесь назад.
        </div>
      </div>
    );
  }

  const handlePay = async () => {
    hapticMedium();
    setLoading(true);
    setError(null);
    try {
      const res = await api.createPayment(plan.id);
      const { paymentId, confirmationUrl } = res.data;

      MaxBridge.openLink(confirmationUrl);

      navigate('waiting', {
        paymentId,
        community,
        plan,
      });
    } catch (err: unknown) {
      hapticError();
      const e = err as { message?: string; code?: string };
      const code = e?.code;
      if (code === 'PLAN_INACTIVE') {
        setError('Этот тариф больше не продаётся');
      } else if (code === 'RATE_LIMIT_EXCEEDED') {
        setError('Слишком много попыток, подождите немного');
      } else {
        setError(e?.message ?? 'Не удалось создать платёж. Попробуйте снова.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="screen fade-in">
      <TopBar title="Оформление" onBack={goBack} />

      <div style={{ paddingTop: 8 }}>
        <div
          className="card"
          style={{
            background: 'linear-gradient(135deg, rgba(255,111,142,0.12), rgba(159,216,181,0.08))',
            borderColor: 'rgba(255,111,142,0.2)',
            marginBottom: 16,
          }}
        >
          <div style={{ fontSize: 13, color: 'var(--muted)', marginBottom: 4 }}>
            Сообщество
          </div>
          <div style={{ fontSize: 18, fontWeight: 700 }}>{community.title}</div>
        </div>

        <div className="card">
          <div style={{ fontSize: 16, fontWeight: 700, marginBottom: 16 }}>
            {plan.title}
          </div>

          <div className="sum-row">
            <span>Тариф</span>
            <span style={{ color: 'var(--text)' }}>{plan.title}</span>
          </div>
          <div className="sum-row">
            <span>Период</span>
            <span style={{ color: 'var(--text)' }}>{formatPeriod(plan.periodDays)}</span>
          </div>
          <div className="sum-row">
            <span>Стоимость</span>
            <span style={{ color: 'var(--text)' }}>{formatRub(plan.priceRub)}</span>
          </div>
          <div className="sum-row">
            <span style={{ fontSize: 12 }}>
              Комиссия платформы (~3%)
            </span>
            <span style={{ fontSize: 13 }}>
              включена
            </span>
          </div>

          <div className="sum-row total">
            <span>К оплате</span>
            <span>{formatRub(plan.priceRub)}</span>
          </div>

          <SbpBadge />
        </div>

        <div style={{
          background: 'var(--glass)',
          border: '1px solid var(--glass-b)',
          borderRadius: 'var(--radius)',
          padding: '12px 16px',
          fontSize: 12.5,
          color: 'var(--muted)',
          lineHeight: 1.6,
          marginBottom: 8,
        }}>
          После оплаты вам придёт одноразовая ссылка для вступления в канал. Ссылка действует 24 часа.
          Доступ действует{' '}
          {plan.periodDays === 0
            ? 'бессрочно'
            : `${formatPeriod(plan.periodDays)} с момента оплаты`}.
        </div>

        {error && (
          <div className="info-banner error" style={{ marginBottom: 8 }}>
            <span>⚠️</span>
            <div>{error}</div>
          </div>
        )}

        <div style={{
          textAlign: 'center', fontSize: 11.5, color: 'var(--muted)', paddingBottom: 24, lineHeight: 1.5,
        }}>
          ЮKassa sandbox — реальные списания не происходят
        </div>
      </div>

      <MainBtn
        label="Оплатить через СБП"
        onClick={handlePay}
        loading={loading}
      />
    </div>
  );
}
