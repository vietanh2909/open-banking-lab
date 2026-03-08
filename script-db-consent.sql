-- =========================================================
-- Consent Management System (CMS) - PostgreSQL DDL (Full)
-- Target: Postgres 13+ (works well on 14/15/16)
-- Notes:
--   - consent record id: consents.id
--   - consentId TTL 300s (decoupled/OTP): consent_challenges.consent_id
--   - Idempotency: idempotency_keys (Request-ID based)
--   - Callback reliability: callback_outbox + callback_attempts
-- =========================================================

-- ---------- Extensions ----------
create extension if not exists "uuid-ossp";
create extension if not exists pgcrypto;

-- ---------- Schema (optional) ----------
-- create schema if not exists cms;
-- set search_path to cms, public;

-- =========================================================
-- 1) TPP master
-- =========================================================
create table if not exists tpps (
  tpp_id              varchar(15) primary key,
  name                varchar(200) not null,
  status              varchar(20)  not null,
  callback_url        text,
  callback_auth_type  varchar(30),      -- e.g. MTLS | OAUTH2 | NONE (your choice)
  callback_kid        varchar(100),      -- key id used for JWS sign/verify if needed
  callback_pubkey_pem text,              -- optional: public key of TPP for verification / trust
  created_at          timestamptz not null default now(),
  updated_at          timestamptz not null default now(),

  constraint ck_tpps_status check (status in ('ACTIVE','SUSPENDED'))
);

create index if not exists ix_tpps_status on tpps(status);

-- =========================================================
-- 2) Consent records (source of truth)
-- =========================================================
create table if not exists consents (
  id                 uuid primary key default gen_random_uuid(),

  consent_type        varchar(10)  not null,  -- AIS | PIS | EWLTS
  status              varchar(30)  not null,  -- record-level status (NOT consentId TTL)
  -- AIS: CREATED|AUTHN_PENDING|AUTHORISED|ACTIVE|REJECTED|REVOKED|EXPIRED
  -- PIS/EWLTS: CREATED|AWAITING_AUTH|AUTHORISED|REJECTED|CANCELLED|COMPLETED|EXPIRED

  tpp_id              varchar(15)  not null,
  provider_id         varchar(8),            -- Bank/provider id
  client_id           varchar(50)  not null, -- OAuth client id in IAM/Keycloak
  psu_id              varchar(128),          -- nullable before PSU login

  scope_text          text not null,         -- "AIS" | "PIS" | "EWLTS" or full scope string
  purpose             text,
  created_by_actor    varchar(20) not null,  -- TPP|PSU|BANK_SYSTEM|ADMIN

  request_id          varchar(60),           -- trace Request-ID
  request_datetime    timestamptz,           -- trace Request-DateTime (RFC3339)

  created_at          timestamptz not null default now(),
  updated_at          timestamptz not null default now(),

  -- For AIS long validity (consent validity window), NOT for consentId TTL
  valid_from          timestamptz,
  valid_until         timestamptz,

  authorised_at       timestamptz,
  revoked_at          timestamptz,
  cancelled_at        timestamptz,

  reason_code         varchar(50),
  reason_detail       text,

  version             bigint not null default 0,

  constraint fk_consents_tpp foreign key (tpp_id) references tpps(tpp_id),

  constraint ck_consents_type check (consent_type in ('AIS','PIS','EWLTS')),
  constraint ck_consents_actor check (created_by_actor in ('TPP','PSU','BANK_SYSTEM','ADMIN')),

  -- Record-level status set (union; you can tighten per-type in app layer)
  constraint ck_consents_status check (status in (
    'CREATED','APPROVED','REJECTED',
    'REVOKED','EXPIRED'
  ))
);

create index if not exists ix_consents_type_status on consents(consent_type, status);
create index if not exists ix_consents_tpp_type_status on consents(tpp_id, consent_type, status);
create index if not exists ix_consents_psu_type_status on consents(psu_id, consent_type, status);
create index if not exists ix_consents_created_at on consents(created_at);

-- Optional idempotency at consent record creation level
create unique index if not exists uq_consents_tpp_request
on consents(tpp_id, consent_type, request_id)
where request_id is not null;

-- =========================================================
-- 3) AIS consent - account permissions mapping
-- =========================================================
create table if not exists consent_accounts (
  id           bigserial primary key,
  consent_id   uuid not null,
  account_id   varchar(34) not null,
  permissions  jsonb not null default '{}'::jsonb,
  created_at   timestamptz not null default now(),

  constraint fk_consent_accounts_consent
    foreign key (consent_id) references consents(id) on delete cascade
);

create unique index if not exists uq_consent_accounts on consent_accounts(consent_id, account_id);
create index if not exists ix_consent_accounts_consent on consent_accounts(consent_id);

-- =========================================================
-- 4) PIS payment records (paymentId level)
-- =========================================================
create table if not exists pis_payments (
  payment_id                 varchar(35) primary key,

  consent_id                 uuid not null,      -- FK to consents.id (record id)
  tpp_id                     varchar(15) not null,

  instruction_identification varchar(50) not null, -- TPP provided
  consent_status             varchar(20) not null, -- AWAITING_AUTH|AUTHORISED|REJECTED|CANCEL
  payment_status             varchar(4)  not null, -- ISO20022 4 chars
  status_datetime            timestamptz not null default now(),

  requested_execution_date   date,
  debtor_info                jsonb,
  creditor_info              jsonb,
  instructed_amount          jsonb,               -- store as {amount,currency} as in spec
  remittance_information     jsonb,
  additional_info            jsonb,

  created_at                 timestamptz not null default now(),
  updated_at                 timestamptz not null default now(),
  version                    bigint not null default 0,

  constraint fk_pis_payments_consent
    foreign key (consent_id) references consents(id) on delete restrict,
  constraint fk_pis_payments_tpp
    foreign key (tpp_id) references tpps(tpp_id),

  constraint ck_pis_consent_status check (consent_status in ('AWAITING_AUTH','AUTHORISED','REJECTED','CANCEL')),
  constraint ck_pis_payment_status_len check (char_length(payment_status) = 4)
);

create unique index if not exists uq_pis_tpp_instruction
on pis_payments(tpp_id, instruction_identification);

create index if not exists ix_pis_consent on pis_payments(consent_id);
create index if not exists ix_pis_consent_status on pis_payments(consent_status);
create index if not exists ix_pis_status_datetime on pis_payments(status_datetime);

-- =========================================================
-- 5) EWLTS cash-in records (paymentId level)
-- =========================================================
create table if not exists ewlts_cash_in (
  payment_id        varchar(35) primary key,

  consent_id        uuid not null,      -- FK to consents.id
  tpp_id            varchar(15) not null,

  ewallet_token     varchar(30) not null,
  authen_type       varchar(10) not null, -- OTP|DECOUPLED|NONE

  payment_status    varchar(4)  not null,
  status_datetime   timestamptz not null default now(),

  amount_info       jsonb,
  payer_info        jsonb,
  payee_info        jsonb,
  additional_info   jsonb,

  created_at        timestamptz not null default now(),
  updated_at        timestamptz not null default now(),
  version           bigint not null default 0,

  constraint fk_ewlts_cash_in_consent
    foreign key (consent_id) references consents(id) on delete restrict,
  constraint fk_ewlts_cash_in_tpp
    foreign key (tpp_id) references tpps(tpp_id),

  constraint ck_ewlts_authen_type check (authen_type in ('OTP','DECOUPLED','NONE')),
  constraint ck_ewlts_payment_status_len check (char_length(payment_status) = 4)
);

create index if not exists ix_ewlts_cash_in_consent on ewlts_cash_in(consent_id);
create index if not exists ix_ewlts_cash_in_token on ewlts_cash_in(ewallet_token);
create index if not exists ix_ewlts_cash_in_status_dt on ewlts_cash_in(status_datetime);

-- =========================================================
-- 6) consentId challenges (TTL 300s) for decoupled/OTP
--    - This table holds TT64 consentId (short-lived, one-time)
-- =========================================================
create table if not exists consent_challenges (
  consent_id      uuid primary key, -- TT64 consentId

  challenge_type  varchar(30) not null, -- PIS_DECOUPLED | EWLTS_DECOUPLED | EWLTS_OTP
  payment_id      varchar(35) not null,
  ewallet_token   varchar(30),          -- required for EWLTS_*
  consent_status  varchar(20) not null, -- AUTHORISED|REJECTED|CANCEL

  expires_at      timestamptz not null, -- now()+300s
  consumed_at     timestamptz,          -- set when used in submit

  created_at      timestamptz not null default now(),
  updated_at      timestamptz not null default now(),
  version         bigint not null default 0,

  constraint ck_challenge_type check (challenge_type in ('PIS_DECOUPLED','EWLTS_DECOUPLED','EWLTS_OTP')),
  constraint ck_challenge_status check (consent_status in ('AUTHORISED','REJECTED','CANCEL')),

  -- Require ewallet_token for EWLTS challenges
  constraint ck_challenge_ewlts_token_required check (
    (challenge_type = 'PIS_DECOUPLED' and ewallet_token is null)
    or
    (challenge_type in ('EWLTS_DECOUPLED','EWLTS_OTP') and ewallet_token is not null)
  )
);

-- Uniqueness: only one active challenge per payment (per type)
create unique index if not exists uq_challenge_pis_payment
on consent_challenges(challenge_type, payment_id)
where challenge_type = 'PIS_DECOUPLED';

create unique index if not exists uq_challenge_ewlts_payment_token
on consent_challenges(challenge_type, payment_id, ewallet_token)
where challenge_type in ('EWLTS_DECOUPLED','EWLTS_OTP');

create index if not exists ix_challenge_payment on consent_challenges(payment_id);
create index if not exists ix_challenge_expires on consent_challenges(expires_at);
create index if not exists ix_challenge_consumed on consent_challenges(consumed_at);

-- =========================================================
-- 7) Idempotency keys (Request-ID based)
-- =========================================================
create table if not exists idempotency_keys (
  request_id      varchar(60) primary key,
  api_scope       varchar(40) not null,      -- PIS_CREATE|PIS_SUBMIT|GET_CONSENT|EWLTS_VERIFY_OTP|...
  tpp_id          varchar(15),
  request_hash    char(64) not null,         -- sha256 of canonical request
  response_code   integer not null,
  response_body   jsonb,
  created_at      timestamptz not null default now(),
  expires_at      timestamptz not null,

  constraint fk_idemp_tpp foreign key (tpp_id) references tpps(tpp_id),
  constraint ck_idemp_scope check (char_length(api_scope) > 0)
);

create index if not exists ix_idemp_expires on idempotency_keys(expires_at);
create index if not exists ix_idemp_tpp_scope on idempotency_keys(tpp_id, api_scope);

-- =========================================================
-- 8) Consent events (audit trail)
-- =========================================================
create table if not exists consent_events (
  event_id      uuid primary key default gen_random_uuid(),
  consent_id    uuid not null,

  event_type    varchar(40) not null,
  occurred_at   timestamptz not null default now(),

  request_id    varchar(60),
  actor         varchar(20) not null,        -- TPP|PSU|BANK_SYSTEM|ADMIN
  payload       jsonb,

  published     boolean not null default false,
  published_at  timestamptz,

  constraint fk_events_consent
    foreign key (consent_id) references consents(id) on delete cascade,

  constraint ck_events_actor check (actor in ('TPP','PSU','BANK_SYSTEM','ADMIN'))
);

create index if not exists ix_events_consent_time
on consent_events(consent_id, occurred_at desc);

create index if not exists ix_events_published
on consent_events(published, occurred_at);

-- =========================================================
-- 9) Callback outbox (push update-consent to TPP with retry)
-- =========================================================
create table if not exists callback_outbox (
  event_id        uuid primary key default gen_random_uuid(),
  event_type      varchar(40) not null,    -- PIS_UPDATE_CONSENT | EWLTS_UPDATE_CONSENT_CASH_IN ...
  tpp_id          varchar(15) not null,

  target_url      text not null,
  request_id      varchar(60) not null,    -- use event_id::text or uuid, up to you
  payload         jsonb not null,
  jws_signature   text,                    -- detached JWS if you store it

  status          varchar(20) not null default 'NEW', -- NEW|SENT|FAILED|DEAD
  retry_count     integer not null default 0,
  next_retry_at   timestamptz not null default now(),

  last_error      text,
  created_at      timestamptz not null default now(),
  updated_at      timestamptz not null default now(),

  constraint fk_outbox_tpp foreign key (tpp_id) references tpps(tpp_id),
  constraint ck_outbox_status check (status in ('NEW','SENT','FAILED','DEAD'))
);

create index if not exists ix_outbox_due
on callback_outbox(status, next_retry_at);

create index if not exists ix_outbox_tpp
on callback_outbox(tpp_id, created_at desc);

-- Optional: prevent duplicate outbox for same logical payload (best-effort)
-- create unique index uq_outbox_reqid on callback_outbox(request_id);

create table if not exists callback_attempts (
  event_id      uuid not null,
  attempt_no    integer not null,
  http_status   integer,
  error         text,
  duration_ms   integer,
  created_at    timestamptz not null default now(),

  primary key (event_id, attempt_no),
  constraint fk_attempts_outbox foreign key (event_id)
    references callback_outbox(event_id) on delete cascade
);

-- =========================================================
-- 10) Admin users & actions
-- =========================================================
create table if not exists admin_users (
  id         integer generated always as identity primary key,
  username   varchar(120) not null unique,
  role       varchar(30)  not null,
  status     varchar(20)  not null,
  created_at timestamptz  not null default now(),

  constraint ck_admin_role check (role in ('CONSENT_ADMIN','CONSENT_AUDITOR','SUPER_ADMIN')),
  constraint ck_admin_status check (status in ('ACTIVE','DISABLED'))
);

create table if not exists admin_actions (
  action_id     uuid primary key default gen_random_uuid(),
  admin_user_id integer not null,

  action_type   varchar(40) not null, -- FORCE_REVOKE|FORCE_CANCEL|TPP_SUSPEND|TPP_UNSUSPEND|UPDATE_CALLBACK_URL|REPLAY_EVENT
  target_type   varchar(20) not null, -- CONSENT|TPP|EVENT
  target_id     varchar(80) not null, -- consents.id (uuid text) or tpp_id or event_id

  reason        text not null,
  request_id    varchar(60),
  payload       jsonb,                -- before/after, extra info
  created_at    timestamptz not null default now(),

  constraint fk_admin_actions_user foreign key (admin_user_id)
    references admin_users(id) on delete restrict,

  constraint ck_admin_action_type check (action_type in (
    'FORCE_REVOKE','FORCE_CANCEL','TPP_SUSPEND','TPP_UNSUSPEND',
    'UPDATE_CALLBACK_URL','REPLAY_EVENT'
  )),
  constraint ck_admin_target_type check (target_type in ('CONSENT','TPP','EVENT'))
);

create index if not exists ix_admin_actions_created_at on admin_actions(created_at);
create index if not exists ix_admin_actions_target on admin_actions(target_type, target_id);

-- =========================================================
-- 11) Helpful cleanup indexes (optional)
-- =========================================================
-- For TTL cleanup jobs
-- delete from consent_challenges where expires_at < now() and consumed_at is not null; (policy)
-- delete from idempotency_keys where expires_at < now();

-- =========================================================
-- End of DDL
-- =========================================================