/**
 * maxBridge.ts
 * Обёртка над MAX Mini App SDK.
 * При запуске вне MAX (браузер/разработка) используются заглушки.
 *
 * [СВЕРИТЬ] реальные названия методов по dev.max.ru перед деплоем.
 * В документации встречаются: MaxApp, MAX Bridge, MAX UI — использовать
 * актуальный объект. Заглушки позволяют разрабатывать без открытого MAX.
 */

export type HapticImpactStyle = 'light' | 'medium' | 'heavy' | 'rigid' | 'soft';
export type HapticNotificationType = 'error' | 'success' | 'warning';

interface MaxBridgeSDK {
  init: () => void;
  initData: string;
  MainButton: {
    setText: (text: string) => void;
    show: () => void;
    hide: () => void;
    enable: () => void;
    disable: () => void;
    showProgress: (leaveActive?: boolean) => void;
    hideProgress: () => void;
    onClick: (callback: () => void) => void;
    offClick: (callback: () => void) => void;
  };
  HapticFeedback: {
    impactOccurred: (style: HapticImpactStyle) => void;
    notificationOccurred: (type: HapticNotificationType) => void;
    selectionChanged: () => void;
  };
  ThemeParams: {
    bg_color?: string;
    text_color?: string;
    hint_color?: string;
    link_color?: string;
    button_color?: string;
    button_text_color?: string;
    secondary_bg_color?: string;
  };
  close: () => void;
  openLink: (url: string) => void;
  expand: () => void;
  ready: () => void;
}

// ──────────────────────────────────────────────
// Detect real MAX environment
// ──────────────────────────────────────────────

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const _raw = (window as any).MaxApp ?? (window as any).Telegram?.WebApp ?? null;

const IS_MAX = Boolean(_raw);

// ──────────────────────────────────────────────
// Stub implementation (for browser dev)
// ──────────────────────────────────────────────

const stub: MaxBridgeSDK = {
  init: () => {
    console.info('[MaxBridge] Stub init — running outside MAX');
  },
  get initData() {
    // In dev mode, return a fake initData or empty string
    return (
      import.meta.env.VITE_DEV_INIT_DATA ??
      'user=%7B%22id%22%3A12345678%2C%22first_name%22%3A%22Dev%22%2C%22username%22%3A%22devuser%22%7D&auth_date=1700000000&hash=dev_stub_hash'
    );
  },
  MainButton: {
    setText: (t) => console.info('[MaxBridge:MainButton] setText:', t),
    show: () => console.info('[MaxBridge:MainButton] show'),
    hide: () => console.info('[MaxBridge:MainButton] hide'),
    enable: () => console.info('[MaxBridge:MainButton] enable'),
    disable: () => console.info('[MaxBridge:MainButton] disable'),
    showProgress: () => console.info('[MaxBridge:MainButton] showProgress'),
    hideProgress: () => console.info('[MaxBridge:MainButton] hideProgress'),
    onClick: (cb) => {
      console.info('[MaxBridge:MainButton] onClick registered (stub noop)');
      void cb; // suppress unused warning in dev
    },
    offClick: () => {},
  },
  HapticFeedback: {
    impactOccurred: (s) => {
      console.info('[MaxBridge:Haptic] impact:', s);
      if (navigator.vibrate) navigator.vibrate(s === 'heavy' ? 40 : 20);
    },
    notificationOccurred: (t) => {
      console.info('[MaxBridge:Haptic] notification:', t);
      if (navigator.vibrate) navigator.vibrate(t === 'success' ? [20, 50, 20] : 30);
    },
    selectionChanged: () => {
      if (navigator.vibrate) navigator.vibrate(10);
    },
  },
  ThemeParams: {
    bg_color: '#0D1E18',
    text_color: '#F0F5F2',
    hint_color: '#7A9990',
    link_color: '#FF6F8E',
    button_color: '#FF6F8E',
    button_text_color: '#1A0D12',
    secondary_bg_color: '#152C22',
  },
  close: () => console.info('[MaxBridge] close (stub)'),
  openLink: (url) => window.open(url, '_blank'),
  expand: () => console.info('[MaxBridge] expand (stub)'),
  ready: () => console.info('[MaxBridge] ready (stub)'),
};

// ──────────────────────────────────────────────
// Export unified bridge
// ──────────────────────────────────────────────

export const MaxBridge: MaxBridgeSDK = IS_MAX ? (_raw as MaxBridgeSDK) : stub;

/** Call once at app startup */
export function initMaxBridge() {
  MaxBridge.init?.();
  MaxBridge.expand?.();
  MaxBridge.ready?.();

  // Apply theme CSS vars if available
  const tp = MaxBridge.ThemeParams;
  if (tp?.bg_color) {
    document.documentElement.style.setProperty('--max-bg', tp.bg_color);
  }
  if (tp?.button_color) {
    document.documentElement.style.setProperty('--max-accent', tp.button_color);
  }
}

/** Get initData string for X-Init-Data header */
export function getInitData(): string {
  return MaxBridge.initData ?? '';
}

/** Haptic: tap on important button */
export function hapticMedium() {
  MaxBridge.HapticFeedback.impactOccurred('medium');
}

/** Haptic: action success */
export function hapticSuccess() {
  MaxBridge.HapticFeedback.notificationOccurred('success');
}

/** Haptic: action error */
export function hapticError() {
  MaxBridge.HapticFeedback.notificationOccurred('error');
}

/** Haptic: light selection */
export function hapticLight() {
  MaxBridge.HapticFeedback.selectionChanged();
}
