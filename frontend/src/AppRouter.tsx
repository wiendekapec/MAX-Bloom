/**
 * AppRouter — screen switching + role tabs
 */

import { useNav, type Screen } from './contexts/NavContext';
import { hapticLight } from './lib/maxBridge';

// Screens
import CreatorDashboard from './screens/CreatorDashboard';
import NewPlanScreen from './screens/NewPlanScreen';
import CatalogScreen from './screens/CatalogScreen';
import CommunityScreen from './screens/CommunityScreen';
import CheckoutScreen from './screens/CheckoutScreen';
import WaitingScreen from './screens/WaitingScreen';
import SuccessScreen from './screens/SuccessScreen';
import PaymentErrorScreen from './screens/PaymentErrorScreen';
import MySubscriptionsScreen from './screens/MySubscriptionsScreen';

interface AppRouterProps {
  initialParams?: Record<string, unknown>;
}

// Screens that show the role switcher tabs
const SCREENS_WITH_TABS: Screen[] = ['dashboard', 'catalog', 'my-subscriptions'];

export default function AppRouter({ initialParams: _initialParams }: AppRouterProps) {
  const { current, navigate } = useNav();
  const { screen } = current;

  const showTabs = SCREENS_WITH_TABS.includes(screen);

  const switchRole = (toScreen: Screen) => {
    hapticLight();
    navigate(toScreen);
  };

  return (
    <>
      {/* Role tabs — shown only on top-level screens */}
      {showTabs && (
        <div className="role-tabs">
          <button
            id="tab-creator"
            className={`role-tab ${screen === 'dashboard' ? 'active' : ''}`}
            onClick={() => switchRole('dashboard')}
          >
            👑 Кабинет
          </button>
          <button
            id="tab-catalog"
            className={`role-tab ${screen === 'catalog' ? 'active' : ''}`}
            onClick={() => switchRole('catalog')}
          >
            🛍 Каталог
          </button>
          <button
            id="tab-subs"
            className={`role-tab ${screen === 'my-subscriptions' ? 'active' : ''}`}
            onClick={() => switchRole('my-subscriptions')}
          >
            📋 Мои
          </button>
        </div>
      )}

      {/* Screen rendering */}
      {screen === 'dashboard' && <CreatorDashboard />}
      {screen === 'new-plan' && <NewPlanScreen />}
      {screen === 'catalog' && <CatalogScreen />}
      {screen === 'community' && <CommunityScreen />}
      {screen === 'checkout' && <CheckoutScreen />}
      {screen === 'waiting' && <WaitingScreen />}
      {screen === 'success' && <SuccessScreen />}
      {screen === 'payment-error' && <PaymentErrorScreen />}
      {screen === 'my-subscriptions' && <MySubscriptionsScreen />}
    </>
  );
}
