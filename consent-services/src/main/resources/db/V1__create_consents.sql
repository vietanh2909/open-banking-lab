create table if not exists consents (
    consent_id  varchar(64) primary key,
    psu_id      varchar(128) not null,
    client_id   varchar(128) not null,
    scopes      text not null,
    status      varchar(32) not null,
    expires_at  timestamptz null,
    created_at  timestamptz not null,
    updated_at  timestamptz not null,
    approved_at timestamptz null,
    revoked_at  timestamptz null
    );

create index if not exists idx_consents_psu_client on consents(psu_id, client_id);
