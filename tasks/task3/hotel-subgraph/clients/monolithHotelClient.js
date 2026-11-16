// REST-клиент к отелям монолита
const MONOLITH_URL = process.env.MONOLITH_URL || 'http://localhost:8080';

export async function getHotel(id) {
  console.log(`[hotel] REST GET /api/hotels/${id}`);
  const res = await fetch(`${MONOLITH_URL}/api/hotels/${encodeURIComponent(id)}`, {
    signal: AbortSignal.timeout(3000),
  });
  if (res.status === 404) return null;
  if (!res.ok) throw new Error(`Monolith responded ${res.status} for hotel ${id}`);
  return toHotel(await res.json());
}

// В монолите у отеля нет name и stars: берём описание и округлённый рейтинг
function toHotel(h) {
  return {
    id: h.id,
    name: h.description,
    city: h.city,
    stars: Math.round(h.rating),
    rating: h.rating,
    operational: h.operational,
    fullyBooked: h.fullyBooked,
  };
}
