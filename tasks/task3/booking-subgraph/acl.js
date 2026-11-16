import { GraphQLError } from 'graphql';

// Пока нет API Gateway с аутентификацией, пользователя определяем по заголовку userid,
// который gateway пробрасывает в сабграфы.
export function currentUser(req) {
  return req.headers['userid'] || null;
}

// Пользователь видит только свои бронирования
export function assertCanReadBookings(req, userId) {
  const caller = currentUser(req);
  if (!caller) {
    console.warn(`[ACL] DENY bookingsByUser(${userId}): нет заголовка userid`);
    throw forbidden('Не передан заголовок userid');
  }
  if (caller !== userId) {
    console.warn(`[ACL] DENY bookingsByUser(${userId}): caller=${caller}`);
    throw forbidden('Нельзя смотреть чужие бронирования');
  }
}

function forbidden(message) {
  return new GraphQLError(message, { extensions: { code: 'FORBIDDEN' } });
}
