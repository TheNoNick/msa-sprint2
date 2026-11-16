package com.hotelio.history.entity;

/** Агрегат статистики: ключ группировки (пользователь, отель или день), число бронирований, выручка и средний чек. */
public record BookingStats(String key, long bookings, double revenue, double avgPrice) {
}
