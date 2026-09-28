# MAX Bloom — Диаграммы (v2, актуальные)

> Заменяют соответствующие диаграммы в `07_08_09_architecture.md` и `10_database.md` — те основаны на несуществующем методе MAX API (`POST /chats/{chatId}/links`). Остальные диаграммы в тех файлах (state-диаграммы подписки/платежа, component diagram, deployment) актуальности не потеряли.

---

## 1. Sequence — Оплата и выдача доступа (актуальная механика)

```mermaid
sequenceDiagram
    actor User as 👤 Покупатель
    participant MiniApp as 📱 Mini App
    participant Backend as ☕ Backend
    participant YKassa as 💳 ЮKassa (sandbox)
    participant MaxAPI as 📡 MAX API
    participant DB as 🐘 PostgreSQL

    User->>MiniApp: Нажимает "Оплатить через СБП"
    MiniApp->>Backend: POST /api/v1/payments/create {plan_id, initData}
    Backend->>Backend: Верификация initData (HMAC-SHA256)
    Backend->>DB: INSERT payments (status=PENDING, idempotency_key=UUID)
    Backend->>YKassa: POST /v3/payments {amount, idempotency_key}
    YKassa-->>Backend: {id, status:"pending", confirmation.url}
    Backend-->>MiniApp: {payment_url}
    MiniApp->>User: Открывает подтверждение СБП
    User->>YKassa: Подтверждает оплату

    YKassa->>Backend: POST /webhook/yukassa {event:"payment.succeeded"}
    Backend->>DB: SELECT payments WHERE idempotency_key=... (проверка дубля)
    alt Дубль
        Backend-->>YKassa: 200 OK (игнор, ФЗ-161)
    end
    Backend->>DB: UPDATE payments SET status=SUCCEEDED
    Backend->>DB: INSERT subscriptions (status=ACTIVE, expires_at=NOW+period_days)

    Note over Backend,DB: communities.invite_link уже закэширован при bot_added,<br/>новый вызов к MAX API здесь не нужен

    Backend->>DB: INSERT invite_tokens (token=UUID, status=ACTIVE, expires_at=NOW+24h)
    Backend->>MaxAPI: POST /messages {user_id, text: "https://bloom.example/i/{token}"}
    MaxAPI->>User: 📩 Ссылка на доступ

    Backend-->>YKassa: 200 OK

    User->>Backend: GET /i/{token}
    alt Токен валиден (ACTIVE, не истёк)
        Backend->>DB: UPDATE invite_tokens SET status=USED, used_at=NOW
        Backend-->>User: 302 Redirect → communities.invite_link
        User->>MaxAPI: Переходит по ссылке, вступает в канал
    else Токен не найден / USED / истёк
        Backend-->>User: Страница "Ссылка устарела" + кнопка вернуться в бота
    end
```

---

## 2. Sequence — Ежедневный шедулер (кик + сверка участников)

```mermaid
sequenceDiagram
    participant Sched as ⏰ Scheduler (00:00)
    participant DB as 🐘 PostgreSQL
    participant MaxAPI as 📡 MAX API

    Note over Sched: Часть 1 — кик по истечению срока
    Sched->>DB: SELECT subscriptions WHERE expires_at < NOW() AND status=ACTIVE
    loop Для каждой истёкшей подписки
        Sched->>MaxAPI: DELETE /chats/{chatId}/members?user_id={id}
        Sched->>DB: UPDATE subscriptions SET status=EXPIRED
    end

    Note over Sched: Часть 2 — сверка участников (защита от утёкшей ссылки)
    Sched->>DB: SELECT DISTINCT community_id FROM communities
    loop Для каждого сообщества
        Sched->>MaxAPI: GET /chats/{chatId}/members
        MaxAPI-->>Sched: Список реальных участников
        Sched->>DB: SELECT user_id WHERE community_id=X AND status=ACTIVE
        loop Для каждого участника без активной подписки
            Sched->>MaxAPI: DELETE /chats/{chatId}/members?user_id={id}
        end
    end

    Note over Sched,MaxAPI: Ошибка вызова (бот потерял права) →<br/>лог + известное ограничение, не блокирует остальной проход
```

---

## 3. ER-диаграмма — обновлённая (с учётом дополнений к 10_database.md)

```mermaid
erDiagram
    users {
        bigserial id PK
        varchar max_user_id UK
        varchar username
        varchar first_name
        varchar role "метка для аналитики, не гейт доступа"
        boolean pdp_consent_given
        boolean self_employed_confirmed "НОВОЕ — декларация перед активацией тарифа"
        timestamptz created_at
    }

    communities {
        bigserial id PK
        bigint creator_id FK
        varchar max_chat_id UK
        varchar title
        text description
        varchar category
        text avatar_url
        text invite_link "НОВОЕ — кэш Chat.link"
        int subscribers_count
        timestamptz created_at
    }

    subscription_plans {
        bigserial id PK
        bigint community_id FK
        varchar title
        text description
        numeric price_rub
        int period_days
        boolean is_active
    }

    subscriptions {
        bigserial id PK
        bigint user_id FK
        bigint plan_id FK
        bigint community_id FK
        varchar status
        timestamptz starts_at
        timestamptz expires_at
    }

    payments {
        bigserial id PK
        varchar idempotency_key UK
        bigint user_id FK
        bigint plan_id FK
        numeric amount_rub
        numeric platform_fee_rub
        varchar status
        boolean receipt_sent
        timestamptz created_at
    }

    invite_tokens {
        bigserial id PK
        uuid token UK
        bigint subscription_id FK
        varchar status "ACTIVE / USED"
        timestamptz expires_at
        timestamptz created_at
        timestamptz used_at
    }

    users ||--o{ communities : "создаёт"
    communities ||--o{ subscription_plans : "имеет"
    subscription_plans ||--o{ subscriptions : "покупается как"
    subscription_plans ||--o{ payments : "оплачивается через"
    users ||--o{ subscriptions : "оформляет"
    users ||--o{ payments : "совершает"
    communities ||--o{ subscriptions : "даёт доступ к"
    subscriptions ||--o{ invite_tokens : "выпускает"
```
