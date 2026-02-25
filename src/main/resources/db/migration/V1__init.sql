CREATE TYPE status AS ENUM ('PENDING', 'AUTHORIZED', 'FAILED', 'COMPLETED');
CREATE TYPE operation_type AS ENUM ('BUY');

CREATE TABLE authorizations
(
    id                      UUID PRIMARY KEY,
    external_transaction_id VARCHAR(255)   NOT NULL UNIQUE,
    account_id              UUID           NOT NULL,
    card_id                 UUID           NOT NULL,
    amount_ars              NUMERIC(20, 2) NOT NULL,
    status                  status         NOT NULL,
    created_at              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE trades_outbox
(
    id               UUID PRIMARY KEY,
    authorization_id UUID           NOT NULL UNIQUE,
    wallet_id        VARCHAR(255)   NOT NULL,
    quote_currency   VARCHAR(255)   NOT NULL,
    amount           NUMERIC(20, 8) NOT NULL,
    operation_type   operation_type NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_trades_authorization
        FOREIGN KEY (authorization_id)
            REFERENCES authorizations (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_auth_account_id ON authorizations (account_id);
CREATE INDEX idx_auth_status_created ON authorizations (status, created_at DESC);