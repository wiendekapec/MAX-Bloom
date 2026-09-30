import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';

export type Screen =
  | 'dashboard'
  | 'new-plan'
  | 'catalog'
  | 'community'
  | 'checkout'
  | 'waiting'
  | 'success'
  | 'payment-error'
  | 'my-subscriptions';

export interface NavState {
  screen: Screen;
  params?: Record<string, unknown>;
}

interface NavCtx {
  current: NavState;
  navigate: (screen: Screen, params?: Record<string, unknown>) => void;
  goBack: () => void;
  canGoBack: boolean;
}

const NavContext = createContext<NavCtx>({
  current: { screen: 'catalog' },
  navigate: () => {},
  goBack: () => {},
  canGoBack: false,
});

interface NavProviderProps {
  initialScreen: Screen;
  children: ReactNode;
}

export function NavProvider({ initialScreen, children }: NavProviderProps) {
  const [stack, setStack] = useState<NavState[]>([{ screen: initialScreen }]);

  const navigate = useCallback((screen: Screen, params?: Record<string, unknown>) => {
    setStack((prev) => [...prev, { screen, params }]);
  }, []);

  const goBack = useCallback(() => {
    setStack((prev) => (prev.length > 1 ? prev.slice(0, -1) : prev));
  }, []);

  const current = stack[stack.length - 1];
  const canGoBack = stack.length > 1;

  return (
    <NavContext.Provider value={{ current, navigate, goBack, canGoBack }}>
      {children}
    </NavContext.Provider>
  );
}

export function useNav() {
  return useContext(NavContext);
}
