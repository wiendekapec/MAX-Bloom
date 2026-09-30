import type { CommunityCategory, PlanPeriod } from './api';

export function formatRub(amount?: number | string | null): string {
  if (amount === undefined || amount === null || amount === '') {
    return '0 ₽';
  }
  const numeric = typeof amount === 'number' ? amount : Number(amount);
  if (isNaN(numeric) || !isFinite(numeric)) {
    return '0 ₽';
  }
  try {
    return new Intl.NumberFormat('ru-RU', {
      style: 'currency',
      currency: 'RUB',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(numeric);
  } catch {
    return `${Math.round(numeric)} ₽`;
  }
}

export function formatNumber(amount?: number | string | null, fallback = 0): number {
  if (amount === undefined || amount === null) return fallback;
  const num = typeof amount === 'number' ? amount : Number(amount);
  return isNaN(num) || !isFinite(num) ? fallback : num;
}

export function formatPeriod(days?: PlanPeriod | number | null): string {
  if (days === undefined || days === null) return '0 дней';
  const d = Number(days);
  if (isNaN(d) || !isFinite(d)) return '0 дней';
  if (d === 0) return 'Разово';
  if (d === 7) return '7 дней';
  if (d === 30) return '30 дней';
  if (d === 90) return '90 дней';
  return `${d} дней`;
}

export function formatDate(iso?: string | null): string {
  if (!iso) return '—';
  try {
    const d = new Date(iso);
    if (isNaN(d.getTime())) return '—';
    return d.toLocaleDateString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    });
  } catch {
    return '—';
  }
}

export function formatDateTime(iso?: string | null): string {
  if (!iso) return '—';
  try {
    const d = new Date(iso);
    if (isNaN(d.getTime())) return '—';
    const now = new Date();
    const diffH = (now.getTime() - d.getTime()) / 3600000;

    if (diffH < 1) return 'только что';
    if (diffH < 24) {
      return `сегодня, ${d.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}`;
    }
    if (diffH < 48) {
      return `вчера, ${d.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}`;
    }
    const days = Math.floor(diffH / 24);
    return `${days} дн. назад`;
  } catch {
    return '—';
  }
}

export function daysLeft(iso?: string | null): number {
  if (!iso) return 0;
  try {
    const ms = new Date(iso).getTime() - Date.now();
    if (isNaN(ms)) return 0;
    return Math.max(0, Math.ceil(ms / (1000 * 60 * 60 * 24)));
  } catch {
    return 0;
  }
}

export const CATEGORY_LABELS: Record<CommunityCategory, string> = {
  business: '💼 Бизнес',
  education: '📚 Образование',
  fitness: '🏋️ Фитнес',
  tech: '💻 IT',
  services: '⚖️ Услуги',
};

export const CATEGORY_GRADIENTS: Record<CommunityCategory, string> = {
  business: 'linear-gradient(135deg, #F5C84A, #FF6F8E)',
  education: 'linear-gradient(135deg, #4ECDC4, #9FD8B5)',
  fitness: 'linear-gradient(135deg, #FF6F8E, #FFB199)',
  tech: 'linear-gradient(135deg, #667EEA, #9FD8B5)',
  services: 'linear-gradient(135deg, #FFB199, #F5C84A)',
};

export const CATEGORY_EMOJIS: Record<CommunityCategory, string> = {
  business: '💼',
  education: '📚',
  fitness: '🏋️',
  tech: '👨‍💻',
  services: '⚖️',
};

export function downloadBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}

export function truncate(str: string, max: number): string {
  if (str.length <= max) return str;
  return str.slice(0, max) + '…';
}
