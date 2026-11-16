package com.hotelio.history.repository;

import com.hotelio.history.entity.BookingHistory;
import com.hotelio.history.entity.BookingStats;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Repository
public class BookingHistoryRepository {

    private static final String SELECT = """
            SELECT event_id, booking_id, user_id, hotel_id, promo_code, discount_percent, price, booked_at
            FROM booking_history
            """;

    private static final RowMapper<BookingHistory> HISTORY = (rs, i) -> new BookingHistory(
            rs.getObject("event_id", UUID.class),
            rs.getLong("booking_id"),
            rs.getString("user_id"),
            rs.getString("hotel_id"),
            rs.getString("promo_code"),
            rs.getDouble("discount_percent"),
            rs.getDouble("price"),
            rs.getTimestamp("booked_at").toInstant());

    private static final RowMapper<BookingStats> STATS = (rs, i) -> new BookingStats(
            rs.getString("key"),
            rs.getLong("bookings"),
            rs.getDouble("revenue"),
            rs.getDouble("avg_price"));

    private final JdbcTemplate jdbc;

    public BookingHistoryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Возвращает false, если событие с таким eventId уже сохранено (повторная доставка). */
    public boolean saveIfAbsent(BookingHistory h) {
        return jdbc.update("""
                INSERT INTO booking_history
                    (event_id, booking_id, user_id, hotel_id, promo_code, discount_percent, price, booked_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (event_id) DO NOTHING
                """,
                h.eventId(), h.bookingId(), h.userId(), h.hotelId(), h.promoCode(),
                h.discountPercent(), h.price(), Timestamp.from(h.bookedAt())) == 1;
    }

    public List<BookingHistory> findAll() {
        return jdbc.query(SELECT + " ORDER BY booked_at", HISTORY);
    }

    public List<BookingHistory> findByUserId(String userId) {
        return jdbc.query(SELECT + " WHERE user_id = ? ORDER BY booked_at", HISTORY, userId);
    }

    public List<BookingStats> statsByUser() {
        return stats("user_id");
    }

    public List<BookingStats> statsByHotel() {
        return stats("hotel_id");
    }

    public List<BookingStats> statsByDay() {
        return stats("CAST(CAST(booked_at AS DATE) AS VARCHAR)");
    }

    // groupBy — только константы из этого класса, пользовательский ввод сюда не попадает
    private List<BookingStats> stats(String groupBy) {
        return jdbc.query("""
                SELECT %s AS key, COUNT(*) AS bookings, SUM(price) AS revenue, AVG(price) AS avg_price
                FROM booking_history GROUP BY 1 ORDER BY 1
                """.formatted(groupBy), STATS);
    }
}
