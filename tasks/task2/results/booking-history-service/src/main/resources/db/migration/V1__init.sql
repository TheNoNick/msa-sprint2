CREATE TABLE booking_history (
    event_id         UUID PRIMARY KEY, -- повторное событие не создаст дубль
    booking_id       BIGINT NOT NULL,
    user_id          VARCHAR(255) NOT NULL,
    hotel_id         VARCHAR(255) NOT NULL,
    promo_code       VARCHAR(255),
    discount_percent DOUBLE PRECISION,
    price            DOUBLE PRECISION NOT NULL,
    booked_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    received_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_history_user ON booking_history (user_id);
CREATE INDEX idx_history_hotel ON booking_history (hotel_id);
CREATE INDEX idx_history_booked_at ON booking_history (booked_at);
