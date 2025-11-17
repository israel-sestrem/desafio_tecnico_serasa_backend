-- Enable uuid if you want default uuid generation in DB (optional)
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE branch (
    id     UUID PRIMARY KEY,
    code   VARCHAR(20)  NOT NULL UNIQUE,
    name   VARCHAR(120) NOT NULL,
    city   VARCHAR(80) NOT NULL,
    state  VARCHAR(2) NOT NULL
);

CREATE TABLE truck (
    id              UUID PRIMARY KEY,
    license_plate   VARCHAR(10) NOT NULL UNIQUE,
    tare_weight_kg  NUMERIC(15,2) NOT NULL,
    model           VARCHAR(80) NOT NULL,
    active          BOOLEAN NOT NULL
);

CREATE TABLE grain_type (
    id                      UUID PRIMARY KEY,
    name                    VARCHAR(120) NOT NULL UNIQUE,
    purchase_price_per_ton  NUMERIC(15,2) NOT NULL,
    min_margin              NUMERIC(5,4) NOT NULL,
    max_margin              NUMERIC(5,4) NOT NULL
);

CREATE TABLE scale (
    id           UUID PRIMARY KEY,
    branch_id    UUID NOT NULL,
    external_id  VARCHAR(50) NOT NULL UNIQUE,
    description  VARCHAR(100),
    api_token    VARCHAR(120),
    active       BOOLEAN NOT NULL,
    CONSTRAINT fk_scale_branch FOREIGN KEY (branch_id) REFERENCES branch (id)
);

CREATE TABLE transport_transaction (
    id                       UUID PRIMARY KEY,
    truck_id                 UUID NOT NULL,
    branch_id                UUID NOT NULL,
    grain_type_id            UUID NOT NULL,
    start_timestamp          TIMESTAMP NOT NULL,
    end_timestamp            TIMESTAMP,
    applied_margin           NUMERIC(5,4),
    purchase_price_per_ton   NUMERIC(15,2),
    sale_price_per_ton       NUMERIC(15,2),
    total_net_weight_kg      NUMERIC(15,3),
    total_load_cost          NUMERIC(15,2),
    total_estimated_revenue  NUMERIC(15,2),
    estimated_profit         NUMERIC(15,2),
    CONSTRAINT fk_transport_transaction_truck FOREIGN KEY (truck_id) REFERENCES truck (id),
    CONSTRAINT fk_transport_transaction_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_transport_transaction_grain_type FOREIGN KEY (grain_type_id) REFERENCES grain_type (id)
);

CREATE TABLE weighing (
    id                        UUID PRIMARY KEY,
    scale_id                  UUID NOT NULL,
    grain_type_id             UUID NOT NULL,
    truck_id                  UUID,
    transport_transaction_id  UUID,
    license_plate             VARCHAR(10) NOT NULL,
    gross_weight_kg           NUMERIC(15,3) NOT NULL,
    tare_weight_kg            NUMERIC(15,3) NOT NULL,
    net_weight_kg             NUMERIC(15,3) NOT NULL,
    weighing_timestamp        TIMESTAMP NOT NULL,
    load_cost                 NUMERIC(15,2),
    weighing_type             VARCHAR(20),
    CONSTRAINT fk_weighing_scale FOREIGN KEY (scale_id) REFERENCES scale (id),
    CONSTRAINT fk_weighing_grain_type FOREIGN KEY (grain_type_id) REFERENCES grain_type (id),
    CONSTRAINT fk_weighing_truck FOREIGN KEY (truck_id) REFERENCES truck (id),
    CONSTRAINT fk_weighing_transport_transaction FOREIGN KEY (transport_transaction_id) REFERENCES transport_transaction (id)
);

CREATE INDEX idx_weighing_branch ON weighing (scale_id);
CREATE INDEX idx_weighing_truck ON weighing (truck_id);
CREATE INDEX idx_weighing_grain_type ON weighing (grain_type_id);
CREATE INDEX idx_weighing_timestamp ON weighing (weighing_timestamp);