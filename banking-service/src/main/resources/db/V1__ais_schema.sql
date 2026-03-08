create extension if not exists pgcrypto;

create table if not exists ais_accounts (
    id uuid primary key default gen_random_uuid(),
    psu_id varchar(64) not null,
    account_id varchar(34) not null unique,
    name varchar(70) not null,
    type varchar(10) not null,
    currency char(3) not null,
    bank_code varchar(12) not null,
    status varchar(10) not null default 'active',
    created_at timestamptz not null default now()
    );
create index if not exists idx_ais_accounts_psu on ais_accounts(psu_id);

create table if not exists ais_balances (
    id uuid primary key default gen_random_uuid(),
    account_id varchar(34) not null references ais_accounts(account_id),
    available_value numeric(20,2) not null,
    currency char(3) not null,
    as_of timestamptz not null default now(),
    unique(account_id)
    );

create table if not exists ais_transactions (
    id uuid primary key default gen_random_uuid(),
    account_id varchar(34) not null references ais_accounts(account_id),
    instruction_identification varchar(70) not null unique,
    value_date timestamptz not null,
    amount_value numeric(20,2) not null,
    amount_currency char(3) not null,
    balance_value numeric(20,2),
    balance_currency char(3),
    credit_debit_indicator varchar(4) not null, -- CRDT/DBIT
    reversal_indicator boolean not null default false,
    debtor_name varchar(70),
    debtor_account_id varchar(34),
    creditor_name varchar(70),
    creditor_account_id varchar(34),
    additional_transaction_information varchar(255)
    );
create index if not exists idx_ais_tx_acc_date on ais_transactions(account_id, value_date desc);