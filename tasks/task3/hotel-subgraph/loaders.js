import DataLoader from 'dataloader';
import { getHotel } from './clients/monolithHotelClient.js';

// Новый DataLoader на каждый GraphQL-запрос:
// - все id, запрошенные за один тик, собираются в одну пачку (batch);
// - повторяющиеся id загружаются один раз, результат кешируется до конца запроса.
// В монолите нет эндпоинта для нескольких отелей сразу, поэтому внутри пачки
// уникальные id запрашиваются параллельно. Когда появится hotel-service с батч-API,
// поменяется только эта функция.
export function createHotelLoader() {
  return new DataLoader(async (ids) => {
    console.log(`[hotel] batch load ${ids.length} hotel(s): ${ids.join(', ')}`);
    return Promise.all(ids.map(getHotel));
  });
}
