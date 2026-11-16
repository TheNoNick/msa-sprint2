package com.hotelio.history.controller;

import com.hotelio.history.entity.BookingHistory;
import com.hotelio.history.entity.BookingStats;
import com.hotelio.history.service.BookingHistoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StatisticsController {

    private final BookingHistoryService historyService;

    public StatisticsController(BookingHistoryService historyService) {
        this.historyService = historyService;
    }

    // GET /api/history?userId=...
    @GetMapping("/history")
    public List<BookingHistory> history(@RequestParam(required = false) String userId) {
        return historyService.history(userId);
    }

    // GET /api/stats/users
    @GetMapping("/stats/users")
    public List<BookingStats> byUser() {
        return historyService.statsByUser();
    }

    // GET /api/stats/hotels
    @GetMapping("/stats/hotels")
    public List<BookingStats> byHotel() {
        return historyService.statsByHotel();
    }

    // GET /api/stats/days
    @GetMapping("/stats/days")
    public List<BookingStats> byDay() {
        return historyService.statsByDay();
    }
}
