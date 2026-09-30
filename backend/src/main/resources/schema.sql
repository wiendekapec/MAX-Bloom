CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    max_user_id VARCHAR(64) NOT NULL UNIQUE,
    username VARCHAR(128),
    first_name VARCHAR(128),
    role VARCHAR(32) NOT NULL DEFAULT 'USER',
    pdp_consent_given BOOLEAN NOT NULL DEFAULT FALSE,
    self_employed_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_max_user_id ON users(max_user_id);

CREATE TABLE IF NOT EXISTS communities (
    id BIGSERIAL PRIMARY KEY,
    creator_id BIGINT NOT NULL REFERENCES users(id),
    max_chat_id VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(64) NOT NULL,
    avatar_url TEXT,
    invite_link TEXT,
    subscribers_count INT NOT NULL DEFAULT 0,
    is_demo BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_communities_creator_id ON communities(creator_id);
CREATE INDEX IF NOT EXISTS idx_communities_max_chat_id ON communities(max_chat_id);

CREATE TABLE IF NOT EXISTS subscription_plans (
    id BIGSERIAL PRIMARY KEY,
    community_id BIGINT NOT NULL REFERENCES communities(id),
    title VARCHAR(128) NOT NULL,
    description TEXT,
    price_rub NUMERIC(12, 2) NOT NULL CHECK (price_rub >= 0.00),
    period_days INT NOT NULL CHECK (period_days >= 0 AND period_days <= 365),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_subscription_plans_community_id ON subscription_plans(community_id);

CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    plan_id BIGINT NOT NULL REFERENCES subscription_plans(id),
    community_id BIGINT NOT NULL REFERENCES communities(id),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELED')),
    starts_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_community_id ON subscriptions(community_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_plan_id ON subscriptions(plan_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status_expires ON subscriptions(status, expires_at);

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key UUID NOT NULL UNIQUE,
    yukassa_payment_id VARCHAR(64),
    user_id BIGINT NOT NULL REFERENCES users(id),
    plan_id BIGINT NOT NULL REFERENCES subscription_plans(id),
    amount_rub NUMERIC(12, 2) NOT NULL CHECK (amount_rub > 0.00),
    platform_fee_rub NUMERIC(12, 2) NOT NULL DEFAULT 0.00 CHECK (platform_fee_rub >= 0.00),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUCCEEDED', 'CANCELED')),
    receipt_sent BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_payments_idempotency_key ON payments(idempotency_key);
CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_plan_id ON payments(plan_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_yukassa_id ON payments(yukassa_payment_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_payments_user_plan_pending ON payments(user_id, plan_id) WHERE status = 'PENDING';

CREATE TABLE IF NOT EXISTS invite_tokens (
    id BIGSERIAL PRIMARY KEY,
    token UUID NOT NULL UNIQUE,
    subscription_id BIGINT NOT NULL REFERENCES subscriptions(id),
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'USED', 'EXPIRED', 'REVOKED')),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    used_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_invite_tokens_token ON invite_tokens(token);
CREATE INDEX IF NOT EXISTS idx_invite_tokens_subscription_id ON invite_tokens(subscription_id);
CREATE INDEX IF NOT EXISTS idx_invite_tokens_status_expires ON invite_tokens(status, expires_at);

CREATE TABLE IF NOT EXISTS bot_sessions (
    id BIGSERIAL PRIMARY KEY,
    max_user_id VARCHAR(64) NOT NULL UNIQUE,
    state VARCHAR(64) NOT NULL DEFAULT 'ONBOARDING_INFO',
    draft_json TEXT,
    pending_chat_id VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_bot_sessions_max_user_id ON bot_sessions(max_user_id);
CREATE INDEX IF NOT EXISTS idx_bot_sessions_pending_chat ON bot_sessions(pending_chat_id) WHERE pending_chat_id IS NOT NULL;
