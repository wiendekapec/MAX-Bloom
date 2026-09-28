# MAX Bloom — Frontend (Mini App)

React + Vite мини-приложение для платформы MAX.

## Быстрый старт

```bash
cp .env.example .env
npm install
npm run dev        # http://localhost:5173
```

## Переменные окружения

| Переменная | Описание | Пример |
|---|---|---|
| `VITE_API_BASE_URL` | Base URL бэкенда | `/api/v1` |
| `VITE_DEV_INIT_DATA` | initData-заглушка для локальной разработки | см. `.env.example` |

## Структура

```
src/
├── lib/
│   ├── maxBridge.ts   # Обёртка MAX SDK (заглушки вне MAX)
│   ├── api.ts         # Axios-клиент + все эндпоинты + демо-данные
│   └── helpers.ts     # Форматирование дат, рублей, периодов
├── contexts/
│   ├── NavContext.tsx  # Stack-based навигация без URL
│   └── ToastContext.tsx
├── components/
│   ├── ui.tsx         # Shared компоненты
│   └── ErrorBoundary.tsx
├── screens/
│   ├── CreatorDashboard.tsx   # Дашборд крейтора (главный экран)
│   ├── NewPlanScreen.tsx      # Создание тарифа
│   ├── CatalogScreen.tsx      # Каталог (демо-данные)
│   ├── CommunityScreen.tsx    # Карточка сообщества
│   ├── CheckoutScreen.tsx     # Чекаут / сводка
│   ├── WaitingScreen.tsx      # Ожидание / polling платежа
│   ├── SuccessScreen.tsx      # Успешная оплата
│   ├── PaymentErrorScreen.tsx # Ошибка оплаты (рабочая ветка)
│   ├── MySubscriptionsScreen  # Мои подписки
│   └── TokenExpiredPage.tsx   # Устаревший invite-токен
└── index.css          # Design system (CSS-переменные, glassmorphism)
```

## Точки входа (deep links из бота)

| URL / hash | Экран |
|---|---|
| `/` | Каталог (по умолчанию) |
| `/?start=dashboard` | Кабинет крейтора |
| `/?start=plan_42` | Карточка тарифа 42 |
| `/expired` | Страница устаревшего токена |

## Заметки для бэкендера

- **`X-Init-Data`** — заголовок на каждом запросе, строка `initData` от MAX SDK, бэкенд проверяет HMAC-SHA256
- **`/api/v1/*`** — все API-эндпоинты, Vite проксирует на `localhost:8080` в dev-режиме
- **Демо-данные** — при 4xx/5xx от `/communities` и `/business/dashboard` фронт падает в `DEMO_COMMUNITIES` / `DEMO_DASHBOARD` из `api.ts` — бэкенд возвращает пустой массив если нет данных
- **openapi.yaml** — полный контракт API в корне пакета

## Сборка продакшена

```bash
npm run build   # dist/
```

Статика раздаётся nginx, пример конфига — в `docker-compose.yml` в корне проекта.
