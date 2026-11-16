import { listBookings } from './clients/bookingGrpcClient.js';
import { assertCanReadBookings, currentUser } from './acl.js';

export const resolvers = {
  Query: {
    bookingsByUser: async (_, { userId }, { req }) => {
      assertCanReadBookings(req, userId);
      const bookings = await listBookings(userId);
      console.log(`[booking] bookingsByUser(${userId}) caller=${currentUser(req)} -> ${bookings.length} bookings`);
      return bookings;
    },
  },
  Booking: {
    // Бронирование по id запрашивает другой сабграф. Ищем только среди бронирований
    // текущего пользователя — так ACL работает и для ссылок между сабграфами.
    __resolveReference: async ({ id }, { req }) => {
      const caller = currentUser(req);
      if (!caller) return null;
      const bookings = await listBookings(caller);
      return bookings.find((b) => b.id === id) || null;
    },
    hotel: (booking) => ({ __typename: 'Hotel', id: booking.hotelId }),
  },
};
