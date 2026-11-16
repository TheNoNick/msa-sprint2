import { getPromo, isPromoValidForUser } from './clients/monolithPromoClient.js';

// Кеш на время одного GraphQL-запроса: у нескольких бронирований часто один и тот же промокод.
// Справочник промокода кешируется по коду, проверка для пользователя — по паре код + пользователь.
export function createDiscountCache() {
  const promos = new Map();
  const results = new Map();

  const promo = (code) => {
    if (!promos.has(code)) promos.set(code, getPromo(code));
    return promos.get(code);
  };

  return (code, userId) => {
    const key = `${code}|${userId ?? ''}`;
    if (!results.has(key)) results.set(key, loadDiscountInfo(promo, code, userId));
    return results.get(key);
  };
}

async function loadDiscountInfo(promo, code, userId) {
  const p = await promo(code);
  if (!p) {
    return { code, isValid: false, originalDiscount: 0, finalDiscount: 0, vipOnly: false };
  }

  // Срок действия и VIP-ограничение проверяет монолит. Без пользователя считаем,
  // что VIP-промокод не подходит
  const isValid = userId ? await isPromoValidForUser(code, userId) : !p.expired && !p.vipOnly;

  return {
    code,
    isValid,
    originalDiscount: p.discount,
    finalDiscount: isValid ? p.discount : 0,
    description: p.description,
    expiresAt: p.validUntil,
    vipOnly: p.vipOnly,
  };
}
