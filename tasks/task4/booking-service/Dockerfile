# Сборка
FROM golang:1.21-alpine AS build
WORKDIR /src
COPY go.mod ./
COPY *.go ./
RUN CGO_ENABLED=0 go build -ldflags="-s -w" -o /booking-service .

# Unit-тесты: docker build --target test booking-service
FROM build AS test
RUN CGO_ENABLED=0 go test -v ./...

# Итоговый образ: только бинарник и curl для отладки
FROM alpine:3.20
RUN apk add --no-cache curl && adduser -D -u 10001 app
COPY --from=build /booking-service /usr/local/bin/booking-service
USER app
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=2s CMD curl -fs http://localhost:8080/ping || exit 1
CMD ["booking-service"]
