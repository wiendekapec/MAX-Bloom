# 🌸 MAX Bloom — Платформа Продаж и Монетизации для Малого Бизнеса в MAX
> **Девиз:** *«Расцветая — крепнем»*  
> **Суть:** Импортонезависимый инструмент продаж подписок на закрытый контент, платные сообщества и консультации для микробизнеса и экспертов в мессенджере MAX.
> 
> **Трек**: «Эффективный бизнес» (VK Education, Минобрнауки РФ, ООО «MAX»)  
> **Срок проведения**: Онлайн-этап с 15 по 30 сентября 2026 г. | Финал: 29 октября 2026 г. (г. Казань)

---

## 📜 Манифест MAX Bloom

> *Мы верим, что малый бизнес и независимые авторы — это сердце цифровой экономики России. В эпоху перемен и блокировок отечественные предприниматели не должны зависеть от серых схем или неподъемных комиссий зарубежных платформ.*  
> 
> *Наша миссия — дать каждому эксперту, репетитору, тренеру и сообществу доступ к нативным, легальным и мгновенным продажам в один клик прямо внутри национального мессенджера MAX.*  
> 
> 🌸 **Расцветая — крепнем.**

---

## 🎯 1. Проблема Рынка и Бизнес-Позиционирование

### 🚨 Проблема в РФ:
После ухода и ограничений зарубежных платформ (**Patreon, Boosty с комиссиями до 15-20%, Instagram, барьеры Telegram Tribute / Tonkeeper**):
- **Малый бизнес потерял регулярные продажи (MRR)**: репетиторы, фитнес-тренеры, онлайн-школы, закрытые бизнес-клубы не могут удобно продавать подписки.
- **Юридический ад и риски**: прием оплат на личные карты грозит блокировками по 115-ФЗ и штрафами налоговой за отсутствие чеков (ФЗ-54).
- **Ручной труд**: предприниматели вручную сверяют чеки, присылают ссылки в чаты и вручную удаляют неплательщиков.

### 💡 Решение MAX Bloom:
1. **Подписки на закрытые сообщества/каналы**: автоматический прием оплат через СБП/ЮKassa + выдача одноразового доступа ботом + авто-кик при неоплате.
2. **Продажа разовых услуг и консультаций**: покупка слотов, приватных созвонов, методичек в 1 клик.
3. **100% легальность в РФ**: автоматическая фискализация (электронный чек по ФЗ-54), защита персональных данных (ФЗ-152), эквайринг через НСПК/СБП (ФЗ-161).
4. **Комиссия всего 5%**: в 2-3 раза выгоднее сторонних сервисов.

---

## 💼 2. Финансовая Модель и Юнит-Экономика

### Агентская схема (ст. 1005 ГК РФ):
- MAX Bloom выступает **Агентом/Маркетплейсом**, а предприниматель — **Принципалом**.
- Налогом облагается **только агентская комиссия 5%**, а не весь оборот (GMV).
- При УСН «Доходы» (6%) налог государству составляет **всего 0.3% от оборота**.

### Юнит-экономика транзакции на 1 000 ₽:
* Сумма покупки: **1 000 ₽**
* Комиссия сервиса (5%): **+50 ₽**
* Эквайринг СБП (0.7%): **-7 ₽**
* Налог УСН 6% с комиссии: **-3 ₽**
* Чек ФЗ-54 (ОФД): **-1 ₽**
* **Чистая прибыль платформы: +39 ₽ (Маржинальность 78%)**

---

## 🏆 3. Официальные Критерии Жюри и Как Мы Забираем Баллы

Итоговая оценка онлайн-этапа: **60% Техническая + 40% Продуктовая + 0.15 балла Платформенный бонус**.

### 📊 А. Техническая оценка (Вес 60% онлайн / 40% финал)

| Критерий | Вес | Как MAX Bloom забирает 100% баллов |
| :--- | :---: | :--- |
| **1. Работоспособность и полнота MVP** | **30%** | **Сквозной рабочий сценарий**: Создание тарифа -> Оплата СБП в Mini App -> Бот MAX выдает доступ в закрытый чат. |
| **2. Корректность интеграций** | **20%** | Связка **MAX Mini App <-> Spring Boot REST API <-> MAX Bot API <-> ЮKassa Webhooks**. Валидация подписи `initData` (HMAC-SHA256). |
| **3. Архитектура и качество кода** | **20%** | Чистая слоистая архитектура, Java 21 + Spring Boot 3.x, Docker Compose, индексированный PostgreSQL. |
| **4. Безопасность и ФЗ РФ** | **10%** | **ФЗ-152** (обезличенный `max_user_id`), **ФЗ-54** (чеки ОФД через ЮKassa receipt), **ФЗ-161** (`Idempotency-Key` от повторных списаний). |
| **5. Стабильность и обработка ошибок** | **10%** | Global Exception Handler (`@ControllerAdvice`, RFC 7807), Error Boundaries во фронтенде, безопасные транзакции `@Transactional`. |
| **6. Документация и комплектность** | **10%** | Полный `README.md`, запуск одной командой `docker compose up`, Swagger UI (`/swagger-ui.html`), видео-демо 2 мин. |
| **🎁 Платформенный Бонус** | **+0.15 б.** | Нативный MAX Mini App SDK (Haptic feedback, ThemeParams, MainButton), Bot Deep Linking (`/start product_42`). |

---

### 💼 Б. Продуктовая оценка (Вес 40% онлайн / 60% финал)

| Критерий | Вес | Наше позиционирование («Эффективный бизнес») |
| :--- | :---: | :--- |
| **1. Потенциал масштабирования** | **35%** (25% финал) | **Бизнес-модель 5% комиссии**: целевой рынок — 2.5 млн самозанятых и микробизнесов РФ. Юнит-экономика масштабируется на любые сферы. |
| **2. Пользовательская ценность** | **25%** | Предприниматель начинает продавать в MAX за 3 минуты без найма разработчиков. Покупатель платит в 1 клик по СБП. |
| **3. UX/UI и удобство** | **20%** | **Glassmorphism UI** (темно-фиолетовая эстетика, неоновые акценты, микро-анимации, виброотклик, mobile-first). |
| **4. Сценарий внедрения (GTM)** | **15%** | Программа «Быстрый старт»: 0% комиссии в первые 30 дней для новых бизнесов, готовые промо-виджеты для каналов. |
| **5. Защита и командная работа** | **10%** | Четкое распределение: Лидер (продукт/питч), Lead Dev (код/архитектура), Head of Sales (юнит-экономика и метрики). |

---

## 🎯 4. Минимальный Набор Функций (Core MVP Scope)

```
[Витрина бизнесов/услуг] ──▶ [Карточка тарифа/услуги] ──▶ [Оплата СБП в 1 клик] ──▶ [MAX Бот выдает ссылку/доступ]
```

1. **Кабинет предпринимателя (Creator / Business Panel)**: Создание тарифа/услуги, привязка MAX-канала/чата, дашборд выручки.
2. **Витрина малого бизнеса (Discovery Marketplace)**: Каталог проверенных клубов, школ и услуг с фильтром по категориям.
3. **Чекаут и Пейволл (1-Click Checkout)**: Модалка покупки (СБП / Карта / Тестовая демо-оплата в 1 клик).
4. **Бот-контроллер доступа (MAX Bot Access Manager)**: Генерация одноразовых инвайт-ссылок и шедулер проверки подписок.

---

## 📅 5. Дорожная Карта до Старта Хакатона (25 августа – 15 сентября 2026)

- **💻 Lead Developer**: Каркас Java 21 Spring Boot 3 + PostgreSQL + Docker Compose; каркас React 18 Glassmorphism UI; эмуляторы MAX Bot API и ЮKassa.
- **👑 Product Lead / CEO**: 5 реалистичных демо-кейсов малого бизнеса РФ (*«IT Mentor Club»*, *«Школа ЕГЭ/ОГЭ»*, *«Фитнес Дома»*, *«Юрист для ИП»*, *«Бизнес-Завтраки»*); скрипт видео-демо.
- **📈 Head of Analytics & Sales**: Юнит-экономика 5%, презентация Pitch Deck под критерии жюри, GTM-стратегия.

---

## 🗄 6. Схема Базы Данных (PostgreSQL DDL)

```sql
-- 1. Пользователи и предприниматели (ФЗ-152)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    max_user_id VARCHAR(64) UNIQUE NOT NULL,
    username VARCHAR(64),
    first_name VARCHAR(128),
    role VARCHAR(32) NOT NULL DEFAULT 'USER', -- 'USER', 'BUSINESS', 'ADMIN'
    pdp_consent_given BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Бизнесы / Сообщества / Проекты
CREATE TABLE communities (
    id BIGSERIAL PRIMARY KEY,
    creator_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    max_chat_id VARCHAR(64) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(64) NOT NULL DEFAULT 'business', -- 'education', 'fitness', 'tech', 'business', 'services'
    avatar_url TEXT,
    subscribers_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Тарифы и Услуги
CREATE TABLE subscription_plans (
    id BIGSERIAL PRIMARY KEY,
    community_id BIGINT NOT NULL REFERENCES communities(id) ON DELETE CASCADE,
    title VARCHAR(128) NOT NULL,
    description TEXT,
    price_rub NUMERIC(10, 2) NOT NULL,
    period_days INT NOT NULL DEFAULT 30, -- 30 (подписка) или 0 (разовая покупка)
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 4. Активные подписки и доступы клиентов
CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_id BIGINT NOT NULL REFERENCES subscription_plans(id),
    community_id BIGINT NOT NULL REFERENCES communities(id),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'EXPIRED'
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- 5. Платежи и чеки (ФЗ-54 / ФЗ-161)
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(128) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id),
    plan_id BIGINT NOT NULL REFERENCES subscription_plans(id),
    amount_rub NUMERIC(10, 2) NOT NULL,
    platform_fee_rub NUMERIC(10, 2) NOT NULL, -- 5% комиссия платформы
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'SUCCEEDED', 'CANCELED'
    receipt_sent BOOLEAN NOT NULL DEFAULT TRUE, -- Подтверждение чека по ФЗ-54
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

---

## ⚡️ 7. Быстрый Старт Разработки в Новом Чате / Репозитории

Для старта в новом чате отправьте этот файл со следующим запросом:
> *"Разверни проект MAX Bloom по спецификации MAX_BLOOM_SPEC.md: создай структуру бэкенда на Java 21 Spring Boot 3 и фронтенд на React Vite с Glassmorphism UI, настроенной БД PostgreSQL и 5 готовыми демо-кейсами малого бизнеса."*
