/**
 * ErrorBoundary — wraps the entire app per spec NFT-12
 */
import { Component, type ErrorInfo, type ReactNode } from 'react';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error?: Error;
}

export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    // In production: send to Sentry / backend logging
    console.error('[ErrorBoundary]', error, info.componentStack);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="error-boundary">
          <div style={{ fontSize: 48, marginBottom: 20 }}>😔</div>
          <div className="h-display" style={{ fontSize: 20, marginBottom: 12 }}>
            Что-то пошло не так
          </div>
          <div className="text-muted text-sm" style={{ marginBottom: 28, maxWidth: 280, lineHeight: 1.6 }}>
            Произошла неожиданная ошибка. Попробуйте перезапустить приложение.
          </div>
          <button
            className="btn btn-primary"
            onClick={() => window.location.reload()}
          >
            Перезапустить
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}
