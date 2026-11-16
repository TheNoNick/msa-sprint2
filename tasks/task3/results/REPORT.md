# Задание 3. GraphQL Federation для личного кабинета

## Что сделано

- **`booking-subgraph`** — бронирования пользователя берутся по gRPC из `booking-service` (задание 2). ACL: смотреть можно только свои бронирования, пользователь определяется по заголовку `userid`. Без заголовка или с чужим `userId` возвращается ошибка `FORBIDDEN`.
- **`hotel-subgraph`** — отели берутся по REST из монолита (`/api/hotels/{id}`). Есть `hotelsByIds` и `__resolveReference`. N+1 решён через DataLoader: все отели из запроса собираются в одну пачку, повторяющиеся id загружаются один раз.
- **`promocode-subgraph`** (новый) — промокоды вынесены в отдельный сабграф и берутся по REST из монолита. Поле `Booking.discountPercent` переехало сюда через `@override(from: "booking-subgraph")`, добавлено `discountInfo` через `@requires(fields: "promoCode userId")`. Запрос `validatePromoCode(code, userId)`.
- **`gateway`** собирает три сабграфа и пробрасывает заголовок `userid`. Это пришлось сделать вручную: вопреки подсказке в задании, Apollo Gateway сам заголовки не передаёт.

## Как запустить

```bash
cd tasks/task2 && docker compose up -d     # монолит и booking-service — источники данных
cd ../task3 && docker compose up -d --build
```

Playground: http://localhost:4000/, заголовок `userid: test-user-2`.

```graphql
query GetUserBookings {
  bookingsByUser(userId: "test-user-2") {
    id
    promoCode
    hotel { name city }
    discountPercent
    discountInfo { isValid originalDiscount finalDiscount description }
  }
}
```

С заголовком `userid: test-user-3` или без него этот же запрос вернёт `FORBIDDEN`.

## Что стоит знать

- У отеля в монолите нет `name` и `stars`, поэтому `name` берётся из описания, а `stars` — это округлённый рейтинг.
- В монолите нет API для нескольких отелей сразу, поэтому внутри пачки DataLoader запрашивает уникальные отели параллельно. Когда появится hotel-service с батч-эндпоинтом, поменяется одна функция в `loaders.js`.
- `discountPercent` теперь показывает актуальную скидку: если промокод истёк, будет 0. Скидка, сохранённая при бронировании, по-прежнему хранится в `booking-service`.
- Запроса `activePromoCodes` нет: монолит не умеет отдавать список промокодов.

## Файлы

| Файл | Что внутри |
| :- | :- |
| `screenshot-success.png` | Успешный вызов `bookingsByUser` со своим `userid` |
| `screenshot-acl-deny.png` | Deny по ACL: запрос чужих бронирований → `FORBIDDEN` |
| `docker-ps.txt` | Запущенные контейнеры |
| `success-response.txt` | Успешный запрос со своим `userid` |
| `acl-deny-response.txt` | Отказ по ACL при запросе чужих бронирований |
| `booking-subgraph.log` | Логи booking-subgraph после двух запросов: успех и отказ |
| `hotel-subgraph.log` | Логи hotel-subgraph: два бронирования в одном отеле — одна загрузка |
