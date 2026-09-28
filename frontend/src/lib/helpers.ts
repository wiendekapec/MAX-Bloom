/**
 * helpers.ts — shared utility functions
 */

import type { CommunityCategory, PlanPeriod } from './api';

/** Format RUB amount */
export function formatRub(amount: number): string {
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(amount);
}

/** Format period_days to human-readable */
export function formatPeriod(days: PlanPeriod): string {
  if (days === 0) return 'Разово';
  if (days === 7) return '7 дней';
  if (days === 30) return '30 дней';
  if (days === 90) return '90 дней';
  return `${days} дней`;
}

/** Format date to Russian locale */
export function formatDate(iso: string): string {
  const d = new Date(iso);
  return d.toLocaleDateString('ru-RU', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}

/** Format date + time */
export function formatDateTime(iso: string): string {
  const d = new Date(iso);
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
}

/** Days until date */
export function daysLeft(iso: string): number {
  const ms = new Date(iso).getTime() - Date.now();
  return Math.max(0, Math.ceil(ms / (1000 * 60 * 60 * 24)));
}

/** Category label */
export const CATEGORY_LABELS: Record<CommunityCategory, string> = {
  business: '💼 Бизнес',
  education: '📚 Образование',
  fitness: '🏋️ Фитнес',
  tech: '💻 IT',
  services: '⚖️ Услуги',
};

/** Category gradient */
export const CATEGORY_GRADIENTS: Record<CommunityCategory, string> = {
  business: 'linear-gradient(135deg, #F5C84A, #FF6F8E)',
  education: 'linear-gradient(135deg, #4ECDC4, #9FD8B5)',
  fitness: 'linear-gradient(135deg, #FF6F8E, #FFB199)',
  tech: 'linear-gradient(135deg, #667EEA, #9FD8B5)',
  services: 'linear-gradient(135deg, #FFB199, #F5C84A)',
};

/** Category emoji */
export const CATEGORY_EMOJIS: Record<CommunityCategory, string> = {
  business: '💼',
  education: '📚',
  fitness: '🏋️',
  tech: '👨‍💻',
  services: '⚖️',
};

/** Download blob as file */
export function downloadBlob(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}

/** Truncate text */
export function truncate(str: string, max: number): string {
  if (str.length <= max) return str;
  return str.slice(0, max) + '…';
}
