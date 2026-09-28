/**
 * SuccessScreen — оплата прошла
 * US-4.3, US-5.1, US-5.2
 */

import { useNav } from '../contexts/NavContext';
import { type Community, type SubscriptionPlan } from '../lib/api';
import { formatDate, formatPeriod, formatRub } from '../lib/helpers';
import { MaxBridge } from '../lib/maxBridge';

export default function SuccessScreen() {
  const { current, navigate } = useNav();
  const community = current.params?.community as Community | undefined;
  const plan = current.params?.plan as SubscriptionPlan | undefined;
  const inviteUrl = current.params?.inviteUrl as string | undefined;
  const expiresAt = current.params?.expiresAt as string | undefined;

  // Fallback invite URL for demo
  const demoInviteUrl = 'https://bloom.example/i/7f3a-b4c2-demo';

  const displayUrl = inviteUrl ?? demoInviteUrl;

  const handleJoin = () => {
    MaxBridge.openLink(displayUrl);
    setTimeout(() => MaxBridge.close(), 500);
  };

  const handleCatalog = () => {
    navigate('catalog');
  };

  return (
    <div className="result-screen slide-up">
      <div className="result-icon success">✓</div>

      <div className="result-title">Оплата прошла!</div>

      <div className="result-desc">
        {community && (
          <>
            Вы подписаны на <strong style={{ color: 'var(--text)' }}>{community.title}</strong>.
            {plan && ` Тариф «${plan.title}» на ${formatPeriod(plan.periodDays)}.`}
          </>
        )}
        {' '}Ссылка для вступления действует 24 часа — также отправлена в чат с ботом.
      </div>

      {/* Invite link box */}
      <div className="invite-box" style={{ marginTop: 24 }}>
        <div style={{ fontSize: 11, color: 'var(--muted)', marginBottom: 6, fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
          Ссылка для вступления
        </div>
        <div className="invite-link">{displayUrl}</div>
        <div className="invite-ttl">
          🕐 Действует до {expiresAt ? formatDate(expiresAt) : 'завтра, 23:59'} · одноразовая
        </div>
      </div>

      {/* Summary */}
      {plan && (
        <div
          style={{
            background: 'var(--glass)',
            border: '1px solid var(--glass-b)',
            borderRadius: 'var(--radius)',
            padding: '12px 16px',
            width: '100%',
            maxWidth: 360,
            marginTop: 16,
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13 }}>
            <span style={{ color: 'var(--muted)' }}>Сумма</span>
            <span style={{ fontWeight: 700 }}>{formatRub(plan.priceRub)}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginTop: 6 }}>
            <span style={{ color: 'var(--muted)' }}>Способ</span>
            <span style={{ color: 'var(--sage)', fontWeight: 600 }}>⚡ СБП</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginTop: 6 }}>
            <span style={{ color: 'var(--muted)' }}>Чек</span>
            <span style={{ color: 'var(--sage)' }}>Отправлен через ЮKassa ОФД</span>
          </div>
        </div>
      )}

      {/* Actions */}
      <div className="result-actions">
        <button
          id="success-join"
          className="btn btn-primary btn-full"
          onClick={handleJoin}
          style={{ padding: '16px' }}
        >
          Вступить в канал →
        </button>
        <button
          className="btn btn-secondary btn-full"
          onClick={handleCatalog}
          style={{ padding: '14px' }}
        >
          В каталог
        </button>
      </div>

      <div style={{ fontSize: 11, color: 'var(--muted)', marginTop: 16, textAlign: 'center', lineHeight: 1.5, maxWidth: 280 }}>
        Ссылка одноразовая — передавать её другим не нужно.
        Если истекла, запросите новую через «Мои подписки» в боте.
      </div>
    </div>
  );
}
