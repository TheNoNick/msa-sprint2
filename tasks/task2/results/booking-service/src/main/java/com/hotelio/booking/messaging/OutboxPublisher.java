package com.hotelio.booking.messaging;

import com.hotelio.booking.entity.OutboxEvent;
import com.hotelio.booking.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * Раз в секунду забирает неотправленные события из outbox и публикует их в Kafka.
 * Если Kafka недоступна, события ждут в таблице и уйдут позже — бронирование от этого не страдает.
 * Гарантия доставки — at-least-once, потребители должны быть идемпотентны по eventId.
 */
@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxRepository outbox;
    private final KafkaTemplate<String, String> kafka;
    private final String topic;

    public OutboxPublisher(OutboxRepository outbox, KafkaTemplate<String, String> kafka, @Value("${booking.topic}") String topic) {
        this.outbox = outbox;
        this.kafka = kafka;
        this.topic = topic;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publish() {
        for (OutboxEvent event : outbox.findTop100ByPublishedAtIsNullOrderByIdAsc()) {
            try {
                kafka.send(topic, event.getAggregateId(), event.getPayload()).get(5, TimeUnit.SECONDS);
                event.markPublished();
                log.info("Published {} for booking {}", event.getEventType(), event.getAggregateId());
            } catch (Exception e) {
                log.warn("Kafka unavailable, outbox event {} will be retried: {}", event.getId(), e.getMessage());
                return;
            }
        }
    }
}
