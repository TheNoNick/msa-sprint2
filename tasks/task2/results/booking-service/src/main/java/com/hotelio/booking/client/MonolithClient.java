package com.hotelio.booking.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * Данные пользователей, отелей, отзывов и промокодов пока живут в монолите.
 * Берём их через его REST API.
 */
@Component
public class MonolithClient {

    private final RestClient http;

    public MonolithClient(@Value("${monolith.url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(1000);
        factory.setReadTimeout(2000);
        this.http = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public boolean isUserActive(String userId) {
        return getBoolean("/api/users/{id}/active", userId);
    }

    public boolean isUserBlacklisted(String userId) {
        return getBoolean("/api/users/{id}/blacklisted", userId);
    }

    public Optional<String> getUserStatus(String userId) {
        try {
            return Optional.ofNullable(http.get().uri("/api/users/{id}/status", userId).retrieve().body(String.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    public boolean isHotelOperational(String hotelId) {
        return getBoolean("/api/hotels/{id}/operational", hotelId);
    }

    public boolean isHotelFullyBooked(String hotelId) {
        return getBoolean("/api/hotels/{id}/fully-booked", hotelId);
    }

    public boolean isHotelTrusted(String hotelId) {
        return getBoolean("/api/reviews/hotel/{id}/trusted", hotelId);
    }

    /** Возвращает промокод, если он действителен для пользователя. Монолит отвечает 400 на неподходящий код. */
    public Optional<Promo> validatePromo(String code, String userId) {
        try {
            Promo promo = http.post()
                    .uri(b -> b.path("/api/promos/validate").queryParam("code", code).queryParam("userId", userId).build())
                    .retrieve()
                    .body(Promo.class);
            return Optional.ofNullable(promo);
        } catch (HttpClientErrorException.BadRequest e) {
            return Optional.empty();
        }
    }

    private boolean getBoolean(String path, String id) {
        return Boolean.TRUE.equals(http.get().uri(path, id).retrieve().body(Boolean.class));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Promo(String code, double discount) {
    }
}
