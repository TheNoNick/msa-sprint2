package com.hotelio.history.service;

import com.hotelio.history.entity.BookingHistory;
import com.hotelio.history.entity.BookingStats;
import com.hotelio.history.repository.BookingHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** История и статистика бронирований. Считается по своей базе, боевые БД не трогаются. */
@Service
public class BookingHistoryService {

    private static final Logger log = LoggerFactory.getLogger(BookingHistoryService.class);

    private final BookingHistoryRepository repository;

    public BookingHistoryService(BookingHistoryRepository repository) {
        this.repository = repository;
    }

    public void record(BookingHistory history) {
        boolean saved = repository.saveIfAbsent(history);
        log.info("BookingCreated for booking {} {}", history.bookingId(), saved ? "saved" : "skipped (duplicate)");
    }

    public List<BookingHistory> history(String userId) {
        return userId == null ? repository.findAll() : repository.findByUserId(userId);
    }

    public List<BookingStats> statsByUser() {
        return repository.statsByUser();
    }

    public List<BookingStats> statsByHotel() {
        return repository.statsByHotel();
    }

    public List<BookingStats> statsByDay() {
        return repository.statsByDay();
    }
}
