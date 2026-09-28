/**
 * Shared UI components
 */

import { type ReactNode } from 'react';

// ──────────────────────────────────────────────
// Spinner
// ──────────────────────────────────────────────

export function Spinner({ size = 'md' }: { size?: 'sm' | 'md' }) {
  return <div className={`spinner ${size === 'sm' ? 'sm' : ''}`} />;
}

// ──────────────────────────────────────────────
// Skeleton
// ──────────────────────────────────────────────

export function SkeletonCard() {
  return (
    <div className="card" style={{ gap: 8, display: 'flex', flexDirection: 'column' }}>
      <div className="skeleton skeleton-line medium" />
      <div className="skeleton skeleton-line short" />
      <div className="skeleton skeleton-line full" />
    </div>
  );
}

// ──────────────────────────────────────────────
// TopBar
// ──────────────────────────────────────────────

interface TopBarProps {
  title: string;
  onBack?: () => void;
  right?: ReactNode;
}

export function TopBar({ title, onBack, right }: TopBarProps) {
  return (
    <div className="topbar">
      {onBack && (
        <button className="btn-back" onClick={onBack} aria-label="Назад">
          ←
        </button>
      )}
      <span className="topbar-title">{title}</span>
      {right && <div style={{ marginLeft: 'auto' }}>{right}</div>}
    </div>
  );
}

// ──────────────────────────────────────────────
// Empty State
// ──────────────────────────────────────────────

interface EmptyStateProps {
  icon?: string;
  title: string;
  description?: string;
  action?: ReactNode;
}

export function EmptyState({ icon = '🌸', title, description, action }: EmptyStateProps) {
  return (
    <div className="empty-state">
      <div className="empty-icon">{icon}</div>
      <div className="empty-title">{title}</div>
      {description && <div className="empty-desc">{description}</div>}
      {action && <div style={{ marginTop: 16 }}>{action}</div>}
    </div>
  );
}

// ──────────────────────────────────────────────
// Error Banner
// ──────────────────────────────────────────────

interface ErrorBannerProps {
  message: string;
  onRetry?: () => void;
}

export function ErrorBanner({ message, onRetry }: ErrorBannerProps) {
  return (
    <div className="info-banner error">
      <span className="info-banner-icon">⚠️</span>
      <div>
        <div style={{ marginBottom: 6 }}>{message}</div>
        {onRetry && (
          <button
            className="btn btn-sm btn-ghost"
            onClick={onRetry}
            style={{ padding: '6px 12px', fontSize: 12 }}
          >
            Повторить
          </button>
        )}
      </div>
    </div>
  );
}

// ──────────────────────────────────────────────
// Main Action Button (fixed at bottom)
// ──────────────────────────────────────────────

interface MainBtnProps {
  label: string;
  onClick: () => void;
  loading?: boolean;
  disabled?: boolean;
  variant?: 'primary' | 'sage';
}

export function MainBtn({ label, onClick, loading, disabled, variant = 'primary' }: MainBtnProps) {
  return (
    <div className="main-btn-wrap">
      <button
        className={`btn btn-full ${variant === 'sage' ? 'btn-sage' : 'btn-primary'}`}
        onClick={onClick}
        disabled={loading || disabled}
      >
        {loading ? <Spinner size="sm" /> : label}
      </button>
    </div>
  );
}

// ──────────────────────────────────────────────
// Toggle
// ──────────────────────────────────────────────

interface ToggleProps {
  on: boolean;
  onChange: (val: boolean) => void;
}

export function Toggle({ on, onChange }: ToggleProps) {
  return (
    <div
      className={`toggle ${on ? '' : 'off'}`}
      role="switch"
      aria-checked={on}
      onClick={() => onChange(!on)}
    >
      <div className="toggle-knob" />
    </div>
  );
}

// ──────────────────────────────────────────────
// Badge
// ──────────────────────────────────────────────

interface BadgeProps {
  variant: 'active' | 'expired' | 'inactive' | 'demo';
  children: ReactNode;
}

export function Badge({ variant, children }: BadgeProps) {
  return <span className={`badge ${variant}`}>{children}</span>;
}

// ──────────────────────────────────────────────
// SBP Badge
// ──────────────────────────────────────────────

export function SbpBadge() {
  return (
    <div className="sbp-badge">
      <span>⚡</span>
      <span>СБП · sandbox</span>
    </div>
  );
}

// ──────────────────────────────────────────────
// Section Title
// ──────────────────────────────────────────────

export function SectionTitle({ children }: { children: ReactNode }) {
  return <div className="h-section">{children}</div>;
}

// ──────────────────────────────────────────────
// Divider
// ──────────────────────────────────────────────

export function Divider() {
  return <div className="divider" />;
}

// ──────────────────────────────────────────────
// Info Banner
// ──────────────────────────────────────────────

interface InfoBannerProps {
  type: 'info' | 'warning' | 'error';
  icon?: string;
  children: ReactNode;
}

export function InfoBanner({ type, icon, children }: InfoBannerProps) {
  const defaultIcons = { info: 'ℹ️', warning: '⚠️', error: '❌' };
  return (
    <div className={`info-banner ${type}`}>
      <span className="info-banner-icon">{icon ?? defaultIcons[type]}</span>
      <div>{children}</div>
    </div>
  );
}
