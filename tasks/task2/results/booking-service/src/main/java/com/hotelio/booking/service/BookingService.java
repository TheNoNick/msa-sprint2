package com.hotelio.booking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelio.booking.client.MonolithClient;
import com.hotelio.booking.entity.Booking;
import com.hotelio.booking.entity.OutboxEvent;
import com.hotelio.booking.repository.BookingRepository;
import com.hotelio.booking.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookings;
    private final OutboxRepository outbox;
    private final MonolithClient monolith;
    private final ObjectMapper json;

    public BookingService(BookingRepository bookings, OutboxRepository outbox, MonolithClient monolith, ObjectMapper json) {
        this.bookings = bookings;
        this.outbox = outbox;
        this.monolith = monolith;
        this.json = json;
    }

    @Transactional(readOnly = true)
    public List<Booking> list(String userId) {
        return isBlank(userId) ? bookings.findAll() : bookings.findByUserId(userId);
    }

    @Transactional
    public Booking create(String userId, String hotelId, String promoCode) {
        if (isBlank(promoCode)) promoCode = null; // прокси монолита передаёт "" вместо null
        log.info("Creating booking: userId={}, hotelId={}, promoCode={}", userId, hotelId, promoCode);

        validateUser(userId);
        validateHotel(hotelId);

        double basePrice = resolveBasePrice(userId);
        double discount = resolvePromoDiscount(promoCode, userId);

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setHotelId(hotelId);
        booking.setPromoCode(promoCode);
        booking.setDiscountPercent(discount);
        booking.setPrice(basePrice - discount);
        booking.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MICROS));
        bookings.save(booking);

        outbox.save(new OutboxEvent(String.valueOf(booking.getId()), "BookingCreated", toEvent(booking)));
        log.info("Booking {} saved, BookingCreated added to outbox", booking.getId());
        return booking;
    }

    private void validateUser(String userId) {
        if (!monolith.isUserActive(userId)) throw new IllegalArgumentException("User is inactive");
        if (monolith.isUserBlacklisted(userId)) throw new IllegalArgumentException("User is blacklisted");
    }

    private void validateHotel(String hotelId) {
        if (!monolith.isHotelOperational(hotelId)) throw new IllegalArgumentException("Hotel is not operational");
        if (!monolith.isHotelTrusted(hotelId)) throw new IllegalArgumentException("Hotel is not trusted based on reviews");
        if (monolith.isHotelFullyBooked(hotelId)) throw new IllegalArgumentException("Hotel is fully booked");
    }

    private double resolveBasePrice(String userId) {
        return monolith.getUserStatus(userId)
                .map(status -> status.equalsIgnoreCase("VIP") ? 80.0 : 100.0)
                .orElse(100.0);
    }

    private double resolvePromoDiscount(String promoCode, String userId) {
        if (promoCode == null) return 0.0;
        return monolith.validatePromo(promoCode, userId).map(MonolithClient.Promo::discount).orElse(0.0);
    }

    private String toEvent(Booking b) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("eventType", "BookingCreated");
        event.put("occurredAt", b.getCreatedAt().toString());
        event.put("bookingId", b.getId());
        event.put("userId", b.getUserId());
        event.put("hotelId", b.getHotelId());
        event.put("promoCode", b.getPromoCode());
        event.put("discountPercent", b.getDiscountPercent());
        event.put("price", b.getPrice());
        try {
            return json.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
