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

function getRaw(): MaxBridgeSDK | null {
  const w = window as unknown as { MaxApp?: MaxBridgeSDK; Telegram?: { WebApp?: MaxBridgeSDK } };
  return w.MaxApp ?? w.Telegram?.WebApp ?? null;
}

function getUrlInitData(): string {
  try {
    if (window.location.hash) {
      const hash = window.location.hash.substring(1);
      const params = new URLSearchParams(hash);
      const data = params.get('tgWebAppData') || params.get('maxWebAppData') || params.get('initData');
      if (data) return data;
      if (hash.includes('hash=') && hash.includes('user=')) return hash;
    }
    if (window.location.search) {
      const params = new URLSearchParams(window.location.search);
      const data = params.get('tgWebAppData') || params.get('maxWebAppData') || params.get('initData');
      if (data) return data;
    }
  } catch {
    return '';
  }
  return '';
}

const stub: MaxBridgeSDK = {
  init: () => {
    console.info('[MaxBridge] Stub init');
  },
  get initData() {
    const now = Math.floor(Date.now() / 1000);
    return (
      import.meta.env.VITE_DEV_INIT_DATA ??
      `user=%7B%22id%22%3A12345678%2C%22first_name%22%3A%22Dev%22%2C%22username%22%3A%22devuser%22%7D&auth_date=${now}&hash=dev_stub_hash`
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
      void cb;
    },
    offClick: () => {},
  },
  HapticFeedback: {
    impactOccurred: (s) => {
      if (navigator.vibrate) navigator.vibrate(s === 'heavy' ? 40 : 20);
    },
    notificationOccurred: (t) => {
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
  close: () => console.info('[MaxBridge] close'),
  openLink: (url) => window.open(url, '_blank'),
  expand: () => {},
  ready: () => {},
};

export const MaxBridge: MaxBridgeSDK = {
  init: () => {
    const raw = getRaw();
    raw?.init?.();
    raw?.ready?.();
    raw?.expand?.();
  },
  get initData() {
    const raw = getRaw();
    if (raw?.initData) return raw.initData;
    const urlData = getUrlInitData();
    if (urlData) return urlData;
    return stub.initData;
  },
  MainButton: {
    setText: (t) => (getRaw()?.MainButton?.setText ?? stub.MainButton.setText)(t),
    show: () => (getRaw()?.MainButton?.show ?? stub.MainButton.show)(),
    hide: () => (getRaw()?.MainButton?.hide ?? stub.MainButton.hide)(),
    enable: () => (getRaw()?.MainButton?.enable ?? stub.MainButton.enable)(),
    disable: () => (getRaw()?.MainButton?.disable ?? stub.MainButton.disable)(),
    showProgress: (l) => (getRaw()?.MainButton?.showProgress ?? stub.MainButton.showProgress)(l),
    hideProgress: () => (getRaw()?.MainButton?.hideProgress ?? stub.MainButton.hideProgress)(),
    onClick: (cb) => (getRaw()?.MainButton?.onClick ?? stub.MainButton.onClick)(cb),
    offClick: (cb) => (getRaw()?.MainButton?.offClick ?? stub.MainButton.offClick)(cb),
  },
  HapticFeedback: {
    impactOccurred: (s) => (getRaw()?.HapticFeedback?.impactOccurred ?? stub.HapticFeedback.impactOccurred)(s),
    notificationOccurred: (t) => (getRaw()?.HapticFeedback?.notificationOccurred ?? stub.HapticFeedback.notificationOccurred)(t),
    selectionChanged: () => (getRaw()?.HapticFeedback?.selectionChanged ?? stub.HapticFeedback.selectionChanged)(),
  },
  get ThemeParams() {
    return getRaw()?.ThemeParams ?? stub.ThemeParams;
  },
  close: () => (getRaw()?.close ?? stub.close)(),
  openLink: (url) => (getRaw()?.openLink ?? stub.openLink)(url),
  expand: () => (getRaw()?.expand ?? stub.expand)(),
  ready: () => (getRaw()?.ready ?? stub.ready)(),
};

export function initMaxBridge() {
  MaxBridge.init?.();
  MaxBridge.expand?.();
  MaxBridge.ready?.();

  const tp = MaxBridge.ThemeParams;
  if (tp?.bg_color) {
    document.documentElement.style.setProperty('--max-bg', tp.bg_color);
  }
  if (tp?.button_color) {
    document.documentElement.style.setProperty('--max-accent', tp.button_color);
  }
}

export function getInitData(): string {
  return MaxBridge.initData ?? '';
}

export function hapticMedium() {
  MaxBridge.HapticFeedback.impactOccurred('medium');
}

export function hapticSuccess() {
  MaxBridge.HapticFeedback.notificationOccurred('success');
}

export function hapticError() {
  MaxBridge.HapticFeedback.notificationOccurred('error');
}

export function hapticLight() {
  MaxBridge.HapticFeedback.selectionChanged();
}
