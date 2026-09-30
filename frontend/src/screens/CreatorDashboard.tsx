import { useState, useEffect, useCallback } from 'react';
import {
  SectionTitle, Toggle, SkeletonCard, ErrorBanner, Badge, EmptyState
} from '../components/ui';
import { useNav } from '../contexts/NavContext';
import { useToast } from '../contexts/ToastContext';
import { api, DEMO_DASHBOARD, type DashboardData, type PaymentStatus } from '../lib/api';
import { formatRub, formatDateTime, formatPeriod, downloadBlob } from '../lib/helpers';
import { hapticLight, hapticSuccess, hapticMedium } from '../lib/maxBridge';

const STATUS_LABEL: Record<PaymentStatus, string> = {
  PENDING: '⏳ В обработке',
  SUCCEEDED: '✓ Оплачено',
  CANCELED: '✗ Отменён',
};

const STATUS_CLASS: Record<PaymentStatus, string> = {
  PENDING: 'pending',
  SUCCEEDED: 'succeeded',
  CANCELED: 'canceled',
};

export default function CreatorDashboard() {
  const { navigate } = useNav();
  const { showToast } = useToast();
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [exportLoading, setExportLoading] = useState(false);
  const [togglingPlan, setTogglingPlan] = useState<number | null>(null);
  const [useDemo, setUseDemo] = useState(false);

  const loadDashboard = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.getDashboard();
      setData(res.data);
      setUseDemo(false);
    } catch {
      setData(DEMO_DASHBOARD);
      setUseDemo(true);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadDashboard(); }, [loadDashboard]);

  const handleTogglePlan = async (planId: number, currentActive: boolean) => {
    if (!data) return;
    hapticLight();
    setTogglingPlan(planId);
    const newVal = !currentActive;

    setData((prev) => prev ? {
      ...prev,
      plans: prev.plans.map((p) => p.id === planId ? { ...p, isActive: newVal } : p),
    } : prev);

    try {
      await api.togglePlan(planId, newVal);
      showToast(newVal ? 'Тариф активирован' : 'Тариф скрыт', 'success');
      hapticSuccess();
    } catch {
      if (useDemo || planId <= 10) {
        showToast(newVal ? 'Тариф активирован' : 'Тариф скрыт', 'success');
        hapticSuccess();
        return;
      }
      setData((prev) => prev ? {
        ...prev,
        plans: prev.plans.map((p) => p.id === planId ? { ...p, isActive: currentActive } : p),
      } : prev);
      showToast('Не удалось изменить тариф', 'error');
    } finally {
      setTogglingPlan(null);
    }
  };

  const handleExport = async () => {
    hapticMedium();
    setExportLoading(true);
    try {
      const res = await api.exportExcel();
      const blob = new Blob([res.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      });
      downloadBlob(blob, `bloom-export-${new Date().toISOString().slice(0, 10)}.xlsx`);
      showToast('Excel-файл загружен', 'success');
      hapticSuccess();
    } catch {
      showToast('Ошибка экспорта — попробуйте снова', 'error');
    } finally {
      setExportLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="screen fade-in">
        <div style={{ padding: '16px 0' }}>
          {[1, 2, 3].map((i) => <SkeletonCard key={i} />)}
        </div>
      </div>
    );
  }

  if (error && !data) {
    return (
      <div className="screen fade-in" style={{ padding: '20px 0' }}>
        <ErrorBanner message={error} onRetry={loadDashboard} />
      </div>
    );
  }

  if (!data) return null;

  return (
    <div className="screen fade-in">
      {useDemo && (
        <div className="info-banner warning" style={{ marginTop: 8 }}>
          <span className="info-banner-icon">🧪</span>
          <div style={{ fontSize: 12 }}>
            <strong>Демо-данные</strong> — бэкенд недоступен. Показываются тестовые значения.
          </div>
        </div>
      )}

      <div className="stat-grid" style={{ marginTop: 8 }}>
        <div className="stat-card wide">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <div>
              <div className="stat-value">{formatRub(data.revenueThisMonthRub ?? data.revenueMonth ?? 0)}</div>
              <div className="stat-label">Выручка за 30 дней</div>
            </div>
            <div style={{ textAlign: 'right' }}>
              <div className="stat-value" style={{ fontSize: 18, color: 'var(--muted)' }}>
                {formatRub(data.totalRevenueRub ?? data.revenueTotal ?? 0)}
              </div>
              <div className="stat-label">Всего</div>
            </div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-value">{data.activeSubscribers ?? 0}</div>
          <div className="stat-label">Активных подписчиков</div>
        </div>

        <div className="stat-card">
          <div className="stat-value bloom">
            {data.plans ? data.plans.filter((p) => p.isActive).length : 0}
          </div>
          <div className="stat-label">Активных тарифов</div>
        </div>
      </div>

      <SectionTitle>Тарифы</SectionTitle>
      <div className="card">
        {!data.plans || data.plans.length === 0 ? (
          <EmptyState
            icon="📋"
            title="Нет тарифов"
            description="Создайте первый тариф, нажав кнопку +"
          />
        ) : (
          data.plans.map((plan) => (
            <div className="plan-row" key={plan.id}>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div className="plan-name" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  {plan.title}
                  {!plan.isActive && <Badge variant="inactive">Скрыт</Badge>}
                </div>
                <div className="plan-meta">
                  {formatRub(plan.priceRub)} · {formatPeriod(plan.periodDays)} ·{' '}
                  <span style={{ color: 'var(--sage)' }}>{plan.subscribersCount ?? 0} подписчиков</span>
                </div>
                {plan.communityId && (
                  <div className="plan-meta" style={{ marginTop: 2 }}>
                    Выручка: {formatRub(plan.totalRevenueRub ?? plan.revenueTotal ?? 0)}
                  </div>
                )}
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 10, flexShrink: 0 }}>
                {togglingPlan === plan.id ? (
                  <div className="spinner sm" />
                ) : (
                  <Toggle
                    on={plan.isActive}
                    onChange={() => handleTogglePlan(plan.id, plan.isActive)}
                  />
                )}
              </div>
            </div>
          ))
        )}
      </div>

      <button
        className="btn btn-ghost btn-full"
        id="dashboard-add-plan"
        style={{ borderStyle: 'dashed', marginBottom: 8 }}
        onClick={() => navigate('new-plan')}
      >
        + Добавить тариф
      </button>

      {data.plans.length > 0 && (
        <>
          <SectionTitle>Ссылка на тариф</SectionTitle>
          <div className="card" style={{ fontSize: 13 }}>
            <div style={{ color: 'var(--muted)', marginBottom: 8, fontSize: 12 }}>
              Поделитесь с покупателями:
            </div>
            <div
              style={{
                fontFamily: 'monospace',
                fontSize: 12,
                color: 'var(--sage)',
                wordBreak: 'break-all',
                lineHeight: 1.6,
                padding: '8px 12px',
                background: 'rgba(159,216,181,0.08)',
                borderRadius: 10,
                cursor: 'pointer',
              }}
              onClick={() => {
                navigator.clipboard?.writeText(
                  `https://max.ru/MaxBloomBot?start=plan_${data.plans[0]?.id}`
                );
                showToast('Ссылка скопирована');
                hapticLight();
              }}
            >
              https://max.ru/MaxBloomBot?start=plan_{data.plans[0]?.id}
            </div>
            <div style={{ fontSize: 11, color: 'var(--muted)', marginTop: 6 }}>
              Нажмите, чтобы скопировать
            </div>
          </div>
        </>
      )}

      <SectionTitle>Последние платежи</SectionTitle>
      <div className="card" id="dashboard-payments">
        {data.recentPayments.length === 0 ? (
          <EmptyState
            icon="💳"
            title="Платежей пока нет"
            description="Здесь появятся платежи после первой оплаты"
          />
        ) : (
          data.recentPayments.map((p) => (
            <div className="pay-row" key={p.id}>
              <div>
                <div className="pay-who">{p.planTitle}</div>
                <div className="pay-when">{formatDateTime(p.createdAt)}</div>
                <span className={`pay-status ${STATUS_CLASS[p.status]}`}>
                  {STATUS_LABEL[p.status]}
                </span>
              </div>
              <div style={{ textAlign: 'right' }}>
                <div className="pay-amount">{formatRub(p.amountRub)}</div>
                <div style={{ fontSize: 11, color: 'var(--muted)', marginTop: 3 }}>
                  комиссия {formatRub(p.platformFeeRub)}
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      <button
        className="btn btn-ghost btn-full"
        id="dashboard-export"
        style={{ borderStyle: 'dashed', marginBottom: 32 }}
        onClick={handleExport}
        disabled={exportLoading}
      >
        {exportLoading ? '⏳ Генерируется…' : '⬇ Экспорт в Excel (.xlsx)'}
      </button>

      <button className="fab" id="fab-new-plan" onClick={() => navigate('new-plan')} aria-label="Добавить тариф">
        +
      </button>
    </div>
  );
}
