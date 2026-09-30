import { useState, useEffect, useCallback } from 'react';
import { TopBar, Badge, EmptyState, ErrorBanner, SkeletonCard } from '../components/ui';
import { useNav } from '../contexts/NavContext';
import { useToast } from '../contexts/ToastContext';
import { api, type Subscription } from '../lib/api';
import { formatDate, formatRub } from '../lib/helpers';
import { hapticLight, hapticSuccess, hapticError } from '../lib/maxBridge';

function mockSubscriptions(): Subscription[] {
  const now = new Date();
  const future = new Date(now);
  future.setDate(now.getDate() + 18);
  const past = new Date(now);
  past.setDate(now.getDate() - 5);
  return [
    {
      id: 1,
      communityId: 1,
      communityTitle: 'IT Mentor Club',
      planId: 1,
      planTitle: 'Базовый',
      priceRub: 990,
      status: 'ACTIVE',
      startsAt: new Date(now.getTime() - 12 * 86400000).toISOString(),
      expiresAt: future.toISOString(),
      daysLeft: 18,
    },
    {
      id: 2,
      communityId: 3,
      communityTitle: 'Фитнес Дома',
      planId: 5,
      planTitle: 'Старт',
      priceRub: 690,
      status: 'EXPIRED',
      startsAt: new Date(now.getTime() - 35 * 86400000).toISOString(),
      expiresAt: past.toISOString(),
      daysLeft: 0,
    },
  ];
}

export default function MySubscriptionsScreen() {
  const { goBack, navigate } = useNav();
  const { showToast } = useToast();

  const [subs, setSubs] = useState<Subscription[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [resending, setResending] = useState<number | null>(null);

  const loadSubs = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.getMySubscriptions();
      setSubs(res.data);
    } catch {
      setSubs(mockSubscriptions());
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadSubs(); }, [loadSubs]);

  const handleResendInvite = async (sub: Subscription) => {
    hapticLight();
    setResending(sub.id);
    try {
      await api.reissueInvite(sub.id);
      hapticSuccess();
      showToast('Новая ссылка отправлена в бот', 'success');
    } catch {
      hapticError();
      showToast('Не удалось выслать ссылку', 'error');
    } finally {
      setResending(null);
    }
  };

  const handleRenew = (_sub: Subscription) => {
    navigate('catalog');
    showToast(`Выберите тариф для продления`, 'info');
  };

  if (loading) {
    return (
      <div className="screen fade-in">
        <TopBar title="Мои подписки" onBack={goBack} />
        {[1, 2].map((i) => <SkeletonCard key={i} />)}
      </div>
    );
  }

  return (
    <div className="screen fade-in">
      <TopBar title="Мои подписки" onBack={goBack} />

      {error && <ErrorBanner message={error} onRetry={loadSubs} />}

      {subs.length === 0 ? (
        <EmptyState
          icon="🌸"
          title="Нет подписок"
          description="Найдите интересное сообщество в каталоге"
          action={
            <button className="btn btn-primary btn-sm" onClick={() => navigate('catalog')}>
              В каталог
            </button>
          }
        />
      ) : (
        <div className="card" style={{ marginTop: 8 }}>
          {subs.map((sub) => (
            <div className="sub-row" key={sub.id}>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div className="sub-title">{sub.communityTitle}</div>
                <div className="sub-plan">{sub.planTitle} · {formatRub(sub.priceRub)}</div>
                <div className="sub-expires">
                  {sub.status === 'ACTIVE' ? (
                    <>
                      ⏱ Осталось <strong style={{ color: (sub.daysLeft ?? 0) <= 3 ? 'var(--error)' : 'var(--sage)' }}>
                        {sub.daysLeft ?? 0} дн.
                      </strong>{' '}
                      · до {formatDate(sub.expiresAt)}
                    </>
                  ) : (
                    <>Истекла {formatDate(sub.expiresAt)}</>
                  )}
                </div>
                {(sub.daysLeft ?? 0) <= 3 && sub.status === 'ACTIVE' && (
                  <div style={{ fontSize: 11, color: 'var(--warning)', marginTop: 3 }}>
                    ⚠️ Скоро истекает — продлите сейчас
                  </div>
                )}
              </div>
              <div className="sub-actions">
                <Badge variant={sub.status === 'ACTIVE' ? 'active' : 'expired'}>
                  {sub.status === 'ACTIVE' ? '● Активна' : '● Истекла'}
                </Badge>
                {sub.status === 'ACTIVE' && (
                  <button
                    className="btn btn-sm btn-secondary"
                    id={`resend-invite-${sub.id}`}
                    disabled={resending === sub.id}
                    onClick={() => handleResendInvite(sub)}
                    style={{ fontSize: 11, padding: '6px 10px' }}
                  >
                    {resending === sub.id ? '⏳' : '🔗 Ссылка'}
                  </button>
                )}
                {sub.status === 'EXPIRED' && (
                  <button
                    className="btn btn-sm btn-primary"
                    id={`renew-sub-${sub.id}`}
                    onClick={() => handleRenew(sub)}
                    style={{ fontSize: 11, padding: '6px 10px' }}
                  >
                    Продлить
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
