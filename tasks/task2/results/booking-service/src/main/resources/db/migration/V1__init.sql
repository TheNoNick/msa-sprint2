-- Схема совместима с таблицей booking монолита, чтобы данные переносились без преобразований
CREATE TABLE booking (
    id               BIGSERIAL PRIMARY KEY,
    user_id          VARCHAR(255),
    hotel_id         VARCHAR(255),
    promo_code       VARCHAR(255),
    discount_percent DOUBLE PRECISION,
    price            DOUBLE PRECISION NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_booking_user_id ON booking (user_id);

-- Transactional outbox: событие пишется в одной транзакции с бронированием
CREATE TABLE outbox (
    id           BIGSERIAL PRIMARY KEY,
    aggregate_id VARCHAR(64)  NOT NULL,
    event_type   VARCHAR(64)  NOT NULL,
    payload      TEXT         NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_outbox_unpublished ON outbox (id) WHERE published_at IS NULL;
