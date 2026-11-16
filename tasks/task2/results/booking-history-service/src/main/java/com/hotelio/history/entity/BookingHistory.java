package com.hotelio.history.entity;

import java.time.Instant;
import java.util.UUID;

/** Запись истории: одно событие BookingCreated. */
public record BookingHistory(
        UUID eventId,
        long bookingId,
        String userId,
        String hotelId,
        String promoCode,
        double discountPercent,
        double price,
        Instant bookedAt
) {
}
