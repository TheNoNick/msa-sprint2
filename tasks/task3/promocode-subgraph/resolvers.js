export const resolvers = {
  Query: {
    validatePromoCode: (_, { code, userId }, { discountInfo }) => discountInfo(code, userId),
  },
  Booking: {
    // promoCode и userId приходят из booking-subgraph благодаря @requires
    discountPercent: async ({ promoCode, userId }, _, { discountInfo }) => {
      if (!promoCode) return 0;
      return (await discountInfo(promoCode, userId)).finalDiscount;
    },
    discountInfo: ({ promoCode, userId }, _, { discountInfo }) => {
      if (!promoCode) return null;
      return discountInfo(promoCode, userId);
    },
  },
};
