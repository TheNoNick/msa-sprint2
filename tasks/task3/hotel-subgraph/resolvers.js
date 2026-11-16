export const resolvers = {
  Hotel: {
    // Gateway присылает все ссылки на отели одним запросом _entities,
    // DataLoader склеивает их в одну пачку вместо N отдельных загрузок
    __resolveReference: ({ id }, { hotelLoader }) => hotelLoader.load(id),
  },
  Query: {
    hotelsByIds: (_, { ids }, { hotelLoader }) => hotelLoader.loadMany(ids),
  },
};
