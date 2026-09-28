/**
 * Toast context — lightweight in-app notifications
 */
import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';

interface ToastItem {
  id: number;
  message: string;
  type: 'info' | 'success' | 'error';
}

interface ToastCtx {
  showToast: (msg: string, type?: ToastItem['type']) => void;
}

const ToastContext = createContext<ToastCtx>({ showToast: () => {} });

let _counter = 0;

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  const showToast = useCallback((message: string, type: ToastItem['type'] = 'info') => {
    const id = ++_counter;
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 2200);
  }, []);

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      <div style={{ position: 'fixed', bottom: 'calc(var(--safe-bottom) + 80px)', left: '50%', transform: 'translateX(-50%)', zIndex: 100, display: 'flex', flexDirection: 'column', gap: 8, alignItems: 'center' }}>
        {toasts.map((t) => (
          <div
            key={t.id}
            className="toast visible"
            style={{
              position: 'static',
              transform: 'none',
              left: 'auto',
              bottom: 'auto',
              color: t.type === 'error' ? 'var(--error)' : t.type === 'success' ? 'var(--sage)' : 'var(--text)',
            }}
          >
            {t.message}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  return useContext(ToastContext);
}
