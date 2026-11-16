import grpc from '@grpc/grpc-js';
import protoLoader from '@grpc/proto-loader';
import { fileURLToPath } from 'url';

// gRPC-клиент к booking-service из задания 2
const PROTO_PATH = fileURLToPath(new URL('../proto/booking.proto', import.meta.url));
const BOOKING_SERVICE_URL = process.env.BOOKING_SERVICE_URL || 'localhost:9090';

const definition = protoLoader.loadSync(PROTO_PATH, { keepCase: true, defaults: true });
const { booking } = grpc.loadPackageDefinition(definition);
const client = new booking.BookingService(BOOKING_SERVICE_URL, grpc.credentials.createInsecure());

export function listBookings(userId) {
  return new Promise((resolve, reject) => {
    client.ListBookings({ user_id: userId }, { deadline: Date.now() + 3000 }, (err, res) => {
      if (err) return reject(err);
      resolve(res.bookings.map(toBooking));
    });
  });
}

function toBooking(b) {
  return {
    id: b.id,
    userId: b.user_id,
    hotelId: b.hotel_id,
    promoCode: b.promo_code || null, // в gRPC нет null, пустая строка = промокода нет
    discountPercent: b.discount_percent,
    price: b.price,
    createdAt: b.created_at || null,
  };
}
