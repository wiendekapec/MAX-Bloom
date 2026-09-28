/**
 * CommunityScreen — карточка сообщества + выбор тарифа
 * US-4.1
 */

import { useState } from 'react';
import { TopBar, MainBtn } from '../components/ui';
import { useNav } from '../contexts/NavContext';
import { type Community, type SubscriptionPlan, DEMO_COMMUNITIES } from '../lib/api';
import { CATEGORY_GRADIENTS, CATEGORY_EMOJIS, formatRub, formatPeriod } from '../lib/helpers';
import { hapticLight, hapticMedium } from '../lib/maxBridge';

export default function CommunityScreen() {
  const { current, navigate, goBack } = useNav();
  const communityId = current.params?.communityId as number | undefined;
  const community = (current.params?.community as Community | undefined)
    ?? DEMO_COMMUNITIES.find((c) => c.id === communityId);

  // Pre-select plan from deep link (?start=plan_42) if passed via params
  const preselectedPlan = current.params?.selectedPlan as SubscriptionPlan | undefined;
  const [selectedPlan, setSelectedPlan] = useState<SubscriptionPlan | null>(
    preselectedPlan ?? community?.plans?.[0] ?? null
  );

  if (!community) {
    return (
      <div className="screen fade-in">
        <TopBar title="Сообщество" onBack={goBack} />
        <div style={{ padding: '40px 16px', textAlign: 'center', color: 'var(--muted)' }}>
          Сообщество не найдено
        </div>
      </div>
    );
  }

  const activePlans = community.plans.filter((p) => p.isActive);

  const handleContinue = () => {
    if (!selectedPlan) return;
    hapticMedium();
    navigate('checkout', { community, plan: selectedPlan });
  };

  return (
    <div className="screen fade-in" style={{ padding: '0 16px 110px' }}>
      <TopBar title="" onBack={goBack} />

      {/* Hero */}
      <div
        className="hero-banner"
        style={{ background: CATEGORY_GRADIENTS[community.category] }}
      >
        <div className="hero-overlay" />
        <span className="hero-emoji">{CATEGORY_EMOJIS[community.category]}</span>
      </div>

      {/* Title */}
      <h1 className="h-display" style={{ fontSize: 22, marginBottom: 8 }}>
        {community.title}
      </h1>
      <p style={{ color: 'var(--muted)', fontSize: 14, lineHeight: 1.6, marginBottom: 20 }}>
        {community.description}
      </p>

      {/* Stats row */}
      <div style={{ display: 'flex', gap: 12, marginBottom: 24 }}>
        <div style={{
          flex: 1, background: 'var(--glass)', border: '1px solid var(--glass-b)',
          borderRadius: 'var(--radius)', padding: '10px 14px', textAlign: 'center'
        }}>
          <div style={{ fontSize: 18, fontWeight: 700, color: 'var(--sage)' }}>
            {community.subscribersCount}
          </div>
          <div style={{ fontSize: 11, color: 'var(--muted)', marginTop: 2 }}>подписчиков</div>
        </div>
        <div style={{
          flex: 1, background: 'var(--glass)', border: '1px solid var(--glass-b)',
          borderRadius: 'var(--radius)', padding: '10px 14px', textAlign: 'center'
        }}>
          <div style={{ fontSize: 18, fontWeight: 700, color: 'var(--bloom)' }}>
            {activePlans.length}
          </div>
          <div style={{ fontSize: 11, color: 'var(--muted)', marginTop: 2 }}>тарифов</div>
        </div>
      </div>

      {/* Plans */}
      <div style={{ fontSize: 11, fontWeight: 700, color: 'var(--muted)', textTransform: 'uppercase', letterSpacing: '0.8px', marginBottom: 10 }}>
        Выберите тариф
      </div>

      {activePlans.length === 0 ? (
        <div style={{ color: 'var(--muted)', fontSize: 14, padding: '20px 0', textAlign: 'center' }}>
          😔 Нет доступных тарифов
        </div>
      ) : (
        activePlans.map((plan) => (
          <div
            key={plan.id}
            id={`plan-${plan.id}`}
            className={`plan-card ${selectedPlan?.id === plan.id ? 'selected' : ''}`}
            onClick={() => {
              hapticLight();
              setSelectedPlan(plan);
            }}
          >
            <div className="plan-card-left">
              <div className="plan-card-title">{plan.title}</div>
              {plan.description && (
                <div className="plan-card-desc">{plan.description}</div>
              )}
              <div className="plan-card-period">
                {formatPeriod(plan.periodDays)}
              </div>
            </div>
            <div>
              <div className="plan-card-price">{formatRub(plan.priceRub)}</div>
              {selectedPlan?.id === plan.id && (
                <div style={{ fontSize: 18, textAlign: 'center', marginTop: 4 }}>✓</div>
              )}
            </div>
          </div>
        ))
      )}

      <MainBtn
        label={selectedPlan ? `Продолжить — ${formatRub(selectedPlan.priceRub)}` : 'Выберите тариф'}
        onClick={handleContinue}
        disabled={!selectedPlan || activePlans.length === 0}
      />
    </div>
  );
}
