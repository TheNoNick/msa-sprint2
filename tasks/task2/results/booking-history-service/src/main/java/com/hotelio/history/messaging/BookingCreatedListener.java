package com.hotelio.history.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelio.history.entity.BookingHistory;
import com.hotelio.history.service.BookingHistoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Слушает BookingCreated из Kafka.
 * Kafka может доставить событие повторно, поэтому сохранение идемпотентно по eventId.
 */
@Component
public class BookingCreatedListener {

    private final BookingHistoryService historyService;
    private final ObjectMapper json;

    public BookingCreatedListener(BookingHistoryService historyService, ObjectMapper json) {
        this.historyService = historyService;
        this.json = json;
    }

    @KafkaListener(topics = "${booking.topic}")
    public void onEvent(String payload) throws Exception {
        JsonNode e = json.readTree(payload);
        if (!"BookingCreated".equals(e.path("eventType").asText())) return;

        historyService.record(new BookingHistory(
                UUID.fromString(e.get("eventId").asText()),
                e.get("bookingId").asLong(),
                e.get("userId").asText(),
                e.get("hotelId").asText(),
                e.hasNonNull("promoCode") ? e.get("promoCode").asText() : null,
                e.path("discountPercent").asDouble(),
                e.get("price").asDouble(),
                Instant.parse(e.get("occurredAt").asText())));
    }
}
