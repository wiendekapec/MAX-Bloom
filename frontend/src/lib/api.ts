import axios from 'axios';
import { getInitData } from './maxBridge';

export type PlanPeriod = 7 | 30 | 90 | 0;

export interface SubscriptionPlan {
  id: number;
  communityId: number;
  title: string;
  description?: string;
  priceRub: number;
  periodDays: PlanPeriod;
  isActive: boolean;
  subscribersCount?: number;
}

export interface Community {
  id: number;
  creatorId: number;
  title: string;
  description: string;
  category: CommunityCategory;
  avatarUrl?: string;
  subscribersCount: number;
  plans: SubscriptionPlan[];
  isDemo?: boolean;
}

export type CommunityCategory =
  | 'business'
  | 'education'
  | 'fitness'
  | 'tech'
  | 'services';

export interface DashboardData {
  communityId: number;
  communityTitle: string;
  revenueTotal: number;
  revenueMonth: number;
  activeSubscribers: number;
  plans: SubscriptionPlanExtended[];
  recentPayments: Payment[];
}

export interface SubscriptionPlanExtended extends SubscriptionPlan {
  subscribersCount: number;
  revenueTotal: number;
}

export type PaymentStatus = 'PENDING' | 'SUCCEEDED' | 'CANCELED';

export interface Payment {
  id: number;
  idempotencyKey: string;
  userId: number;
  planId: number;
  planTitle: string;
  communityTitle: string;
  amountRub: number;
  platformFeeRub: number;
  status: PaymentStatus;
  receiptSent: boolean;
  createdAt: string;
}

export interface CreatePaymentResponse {
  paymentId: string;
  confirmationUrl: string;
}

export interface PaymentStatusResponse {
  paymentId: string;
  status: PaymentStatus;
  inviteUrl?: string;
  expiresAt?: string;
}

export type SubscriptionStatus = 'ACTIVE' | 'EXPIRED' | 'CANCELED';

export interface Subscription {
  id: number;
  communityId: number;
  communityTitle: string;
  planId: number;
  planTitle: string;
  priceRub: number;
  status: SubscriptionStatus;
  startsAt: string;
  expiresAt: string;
  daysLeft: number;
}

export interface CreatePlanRequest {
  title: string;
  description?: string;
  priceRub: number;
  periodDays: PlanPeriod;
  selfEmployedConfirmed: boolean;
}

export interface RegisterBusinessRequest {
  title: string;
  description: string;
  category: CommunityCategory;
}

const baseURL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1';

export const apiClient = axios.create({
  baseURL,
  timeout: 15_000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  const initData = getInitData();
  if (initData) {
    config.headers['X-Init-Data'] = initData;
  }
  return config;
});

apiClient.interceptors.response.use(
  (res) => res,
  (error) => {
    const problemJson = error?.response?.data;
    const msg =
      problemJson?.detail ??
      problemJson?.title ??
      error.message ??
      'Произошла ошибка';
    const code = problemJson?.code ?? error?.response?.status?.toString() ?? 'UNKNOWN';
    return Promise.reject({ message: msg, code, raw: error });
  }
);

export const api = {
  getCommunities: (category?: CommunityCategory) =>
    apiClient.get<Community[]>('/communities', {
      params: category ? { category } : undefined,
    }),

  getCommunity: (id: number) =>
    apiClient.get<Community>(`/communities/${id}`),

  createPayment: (planId: number) =>
    apiClient.post<CreatePaymentResponse>('/payments', { planId }),

  getPaymentStatus: (paymentId: string) =>
    apiClient.get<PaymentStatusResponse>(`/payments/${paymentId}/status`),

  getMySubscriptions: () =>
    apiClient.get<Subscription[]>('/subscriptions/my'),

  reissueInvite: (subscriptionId: number) =>
    apiClient.post<{ inviteUrl: string }>(`/subscriptions/${subscriptionId}/invite`),

  getDashboard: () =>
    apiClient.get<DashboardData>('/business/dashboard'),

  getMyPlans: () =>
    apiClient.get<SubscriptionPlanExtended[]>('/business/plans'),

  createPlan: (data: CreatePlanRequest) =>
    apiClient.post<SubscriptionPlan>('/business/plans', data),

  togglePlan: (planId: number, isActive: boolean) =>
    apiClient.patch<SubscriptionPlan>(`/business/plans/${planId}`, { isActive }),

  deletePlan: (planId: number) =>
    apiClient.delete(`/business/plans/${planId}`),

  exportExcel: () =>
    apiClient.get('/business/export', { responseType: 'blob' }),

  registerBusiness: (data: RegisterBusinessRequest) =>
    apiClient.post<Community>('/business/register', data),

  confirmSelfEmployed: () =>
    apiClient.post('/business/self-employed-confirm'),
};

export const DEMO_COMMUNITIES: Community[] = [
  {
    id: 1,
    creatorId: 0,
    title: 'IT Mentor Club',
    description: 'Разборы задач, код-ревью и закрытые воркшопы для разработчиков. Практика, а не теория.',
    category: 'tech',
    subscribersCount: 214,
    isDemo: true,
    plans: [
      { id: 1, communityId: 1, title: 'Базовый', description: 'Доступ ко всем материалам', priceRub: 990, periodDays: 30, isActive: true },
      { id: 2, communityId: 1, title: 'Pro с менторством', description: '+ 1 созвон-разбор в месяц', priceRub: 2990, periodDays: 30, isActive: true },
    ],
  },
  {
    id: 2,
    creatorId: 0,
    title: 'Школа ЕГЭ/ОГЭ',
    description: 'Репетиторство по математике, физике и информатике. Гарантия повышения балла или возврат.',
    category: 'education',
    subscribersCount: 87,
    isDemo: true,
    plans: [
      { id: 3, communityId: 2, title: 'Математика', description: 'Разборы + задачники', priceRub: 1490, periodDays: 30, isActive: true },
      { id: 4, communityId: 2, title: 'Всё включено', description: 'Все предметы + чат с преподавателем', priceRub: 3490, periodDays: 30, isActive: true },
    ],
  },
  {
    id: 3,
    creatorId: 0,
    title: 'Фитнес Дома',
    description: 'Онлайн-тренировки без оборудования. 20 минут в день — реальный результат за месяц.',
    category: 'fitness',
    subscribersCount: 456,
    isDemo: true,
    plans: [
      { id: 5, communityId: 3, title: 'Старт', description: '3 тренировки в неделю', priceRub: 690, periodDays: 30, isActive: true },
      { id: 6, communityId: 3, title: 'Интенсив', description: 'Ежедневные тренировки + питание', priceRub: 1290, periodDays: 30, isActive: true },
    ],
  },
  {
    id: 4,
    creatorId: 0,
    title: 'Юрист для ИП',
    description: 'Консультации по налогам, договорам и спорам с контрагентами. Отвечаю за 2 часа.',
    category: 'services',
    subscribersCount: 63,
    isDemo: true,
    plans: [
      { id: 7, communityId: 4, title: 'Разовая консультация', description: '60 минут + документ', priceRub: 2500, periodDays: 0, isActive: true },
      { id: 8, communityId: 4, title: 'Абонемент', description: 'До 5 консультаций в месяц', priceRub: 8900, periodDays: 30, isActive: true },
    ],
  },
  {
    id: 5,
    creatorId: 0,
    title: 'Бизнес-Завтраки',
    description: 'Закрытый клуб предпринимателей. Еженедельные встречи, нетворкинг, совместные проекты.',
    category: 'business',
    subscribersCount: 41,
    isDemo: true,
    plans: [
      { id: 9, communityId: 5, title: 'Участник', description: 'Встречи + чат', priceRub: 4900, periodDays: 30, isActive: true },
      { id: 10, communityId: 5, title: 'VIP', description: '+ менторинг и партнёрства', priceRub: 14900, periodDays: 30, isActive: true },
    ],
  },
];

export const DEMO_DASHBOARD: DashboardData = {
  communityId: 1,
  communityTitle: 'IT Mentor Club',
  revenueTotal: 284_400,
  revenueMonth: 18_400,
  activeSubscribers: 23,
  plans: [
    { id: 1, communityId: 1, title: 'Базовый', priceRub: 990, periodDays: 30, isActive: true, subscribersCount: 14, revenueTotal: 112_860 },
    { id: 2, communityId: 1, title: 'Pro с менторством', priceRub: 2990, periodDays: 30, isActive: true, subscribersCount: 9, revenueTotal: 171_540 },
  ],
  recentPayments: [
    { id: 1, idempotencyKey: 'k1', userId: 101, planId: 2, planTitle: 'Pro с менторством', communityTitle: 'IT Mentor Club', amountRub: 2990, platformFeeRub: 89.7, status: 'SUCCEEDED', receiptSent: true, createdAt: new Date(Date.now() - 1 * 3600 * 1000).toISOString() },
    { id: 2, idempotencyKey: 'k2', userId: 102, planId: 1, planTitle: 'Базовый', communityTitle: 'IT Mentor Club', amountRub: 990, platformFeeRub: 29.7, status: 'SUCCEEDED', receiptSent: true, createdAt: new Date(Date.now() - 26 * 3600 * 1000).toISOString() },
    { id: 3, idempotencyKey: 'k3', userId: 103, planId: 1, planTitle: 'Базовый', communityTitle: 'IT Mentor Club', amountRub: 990, platformFeeRub: 29.7, status: 'SUCCEEDED', receiptSent: true, createdAt: new Date(Date.now() - 50 * 3600 * 1000).toISOString() },
    { id: 4, idempotencyKey: 'k4', userId: 104, planId: 2, planTitle: 'Pro с менторством', communityTitle: 'IT Mentor Club', amountRub: 2990, platformFeeRub: 89.7, status: 'CANCELED', receiptSent: false, createdAt: new Date(Date.now() - 72 * 3600 * 1000).toISOString() },
  ],
};
