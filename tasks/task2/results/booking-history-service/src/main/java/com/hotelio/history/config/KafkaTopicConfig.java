package com.hotelio.history.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Топик объявлен и здесь, и в booking-service с одинаковыми параметрами:
 * кто бы ни стартовал первым, топик будет создан до подписки consumer'а с нужным числом партиций.
 */
@Configuration
public class KafkaTopicConfig {

    @Bean
    NewTopic bookingEventsTopic(@Value("${booking.topic}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }
}
