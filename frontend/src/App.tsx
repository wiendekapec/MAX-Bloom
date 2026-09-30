import { useEffect, useState } from 'react';
import { NavProvider, type Screen } from './contexts/NavContext';
import { ToastProvider } from './contexts/ToastContext';
import { ErrorBoundary } from './components/ErrorBoundary';
import { initMaxBridge } from './lib/maxBridge';
import { DEMO_COMMUNITIES } from './lib/api';
import AppRouter from './AppRouter';

function detectInitialScreen(): { screen: Screen; params?: Record<string, unknown> } {
  const hash = window.location.hash.slice(1);
  const search = new URLSearchParams(window.location.search);
  const startParam = hash || search.get('start') || search.get('startapp') || '';

  if (startParam === 'dashboard' || startParam === 'business') {
    return { screen: 'dashboard' };
  }

  if (startParam.startsWith('plan_')) {
    const planIdStr = startParam.replace('plan_', '');
    const planId = parseInt(planIdStr, 10);
    if (!isNaN(planId)) {
      const community = DEMO_COMMUNITIES.find((c) =>
        c.plans.some((p) => p.id === planId)
      );
      const plan = community?.plans.find((p) => p.id === planId);
      if (community && plan) {
        return { screen: 'community', params: { communityId: community.id, community, selectedPlan: plan } };
      }
    }
  }

  return { screen: 'catalog' };
}

export default function App() {
  const [initialized, setInitialized] = useState(false);
  const { screen: initialScreen, params: initialParams } = detectInitialScreen();

  useEffect(() => {
    initMaxBridge();
    setInitialized(true);
  }, []);

  if (!initialized) {
    return (
      <div style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'var(--bg-1)',
      }}>
        <div style={{ fontSize: 32, animation: 'pulse 1s ease-in-out infinite' }}>🌸</div>
      </div>
    );
  }

  return (
    <ErrorBoundary>
      <ToastProvider>
        <NavProvider initialScreen={initialScreen}>
          <div className="app-container">
            <AppRouter initialParams={initialParams} />
          </div>
        </NavProvider>
      </ToastProvider>
    </ErrorBoundary>
  );
}
