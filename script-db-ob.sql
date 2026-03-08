CREATE TABLE IF NOT EXISTS ewlts_cash_in (
  payment_id        varchar(35) PRIMARY KEY,     -- trả cho TPP
  request_id        varchar(60),                 -- Request-ID (idempotency)
  tpp_id            varchar(15) NOT NULL,
  provider_id       varchar(12),
  client_id         varchar(50),

  psu_id            varchar(64),                 -- tương thích với ais_accounts.psu_id (varchar64)
  debtor_account_id varchar(34) NOT NULL,        -- FK -> ais_accounts.account_id
  ewallet_token     varchar(30) NOT NULL,

  amount            numeric(20,2) NOT NULL,      -- match scale của ais_balances.available_value (20,2)
  currency          char(3) NOT NULL,

  status            varchar(30) NOT NULL,        -- OTP_PENDING|REJECTED|COMPLETED|FAILED|EXPIRED
  otp_required      boolean NOT NULL DEFAULT true,

  otp_verified_at   timestamptz,
  completed_at      timestamptz,
  expired_at        timestamptz,                 -- now + 300s

  reason_code       varchar(50),
  reason_detail     text,

  created_at        timestamptz NOT NULL DEFAULT now(),
  updated_at        timestamptz NOT NULL DEFAULT now(),
  version           bigint NOT NULL DEFAULT 0,

  CONSTRAINT fk_ewlts_debtor_account
    FOREIGN KEY (debtor_account_id) REFERENCES ais_accounts(account_id),

  CONSTRAINT ck_ewlts_status CHECK (status IN ('OTP_PENDING','REJECTED','COMPLETED','FAILED','EXPIRED')),
  CONSTRAINT ck_ewlts_amount_pos CHECK (amount > 0),
  CONSTRAINT ck_ewlts_currency_len CHECK (char_length(currency)=3)
);

-- chống tạo trùng do retry init: cùng tpp_id + request_id -> 1 payment
CREATE UNIQUE INDEX IF NOT EXISTS uq_ewlts_tpp_request
  ON ewlts_cash_in(tpp_id, request_id)
  WHERE request_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS ix_ewlts_debtor ON ewlts_cash_in(debtor_account_id);
CREATE INDEX IF NOT EXISTS ix_ewlts_psu_status ON ewlts_cash_in(psu_id, status);
CREATE INDEX IF NOT EXISTS ix_ewlts_created_at ON ewlts_cash_in(created_at);

CREATE TABLE IF NOT EXISTS ewlts_cash_in_otp (
  payment_id      varchar(35) PRIMARY KEY,
  otp_hash        varchar(128) NOT NULL,
  otp_expires_at  timestamptz NOT NULL,
  attempt_count   int NOT NULL DEFAULT 0,
  max_attempts    int NOT NULL DEFAULT 5,
  verified        boolean NOT NULL DEFAULT false,
  verified_at     timestamptz,

  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now(),

  CONSTRAINT fk_ewlts_otp_payment
    FOREIGN KEY (payment_id) REFERENCES ewlts_cash_in(payment_id)
    ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_ewlts_otp_expires ON ewlts_cash_in_otp(otp_expires_at);

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS ewlts_ledger_entries (
  entry_id     uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  payment_id   varchar(35) NOT NULL,
  account_id   varchar(34) NOT NULL,       -- FK -> ais_accounts.account_id
  direction    varchar(10) NOT NULL,       -- DEBIT|CREDIT
  amount       numeric(20,2) NOT NULL,
  currency     char(3) NOT NULL,
  created_at   timestamptz NOT NULL DEFAULT now(),

  CONSTRAINT fk_ewlts_ledger_account
    FOREIGN KEY (account_id) REFERENCES ais_accounts(account_id),

  CONSTRAINT ck_ewlts_ledger_dir CHECK (direction IN ('DEBIT','CREDIT')),
  CONSTRAINT ck_ewlts_ledger_amt CHECK (amount > 0)
);

-- ensure exactly-once debit
CREATE UNIQUE INDEX IF NOT EXISTS uq_ewlts_ledger_debit_once
  ON ewlts_ledger_entries(payment_id, direction)
  WHERE direction='DEBIT';