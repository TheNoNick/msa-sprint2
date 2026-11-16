// REST-клиент к промокодам монолита
const MONOLITH_URL = process.env.MONOLITH_URL || 'http://localhost:8080';

export async function getPromo(code) {
  console.log(`[promo] REST GET /api/promos/${code}`);
  const res = await call(`/api/promos/${encodeURIComponent(code)}`);
  if (res.status === 404) return null;
  if (!res.ok) throw new Error(`Monolith responded ${res.status} for promo ${code}`);
  return res.json();
}

// Монолит сам проверяет срок действия и VIP-ограничение: 200 — подходит, 400 — нет
export async function isPromoValidForUser(code, userId) {
  console.log(`[promo] REST POST /api/promos/validate code=${code} userId=${userId}`);
  const query = new URLSearchParams({ code, userId });
  const res = await call(`/api/promos/validate?${query}`, { method: 'POST' });
  if (res.status === 400) return false;
  if (!res.ok) throw new Error(`Monolith responded ${res.status} on promo validation`);
  return true;
}

function call(path, options = {}) {
  return fetch(`${MONOLITH_URL}${path}`, { ...options, signal: AbortSignal.timeout(3000) });
}
