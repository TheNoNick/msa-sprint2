import { ApolloServer } from '@apollo/server';
import { startStandaloneServer } from '@apollo/server/standalone';
import { buildSubgraphSchema } from '@apollo/subgraph';
import gql from 'graphql-tag';
import { readFileSync } from 'fs';
import { resolvers } from './resolvers.js';
import { createDiscountCache } from './discounts.js';

const typeDefs = gql(readFileSync(new URL('./schema.graphql', import.meta.url), 'utf8'));

const server = new ApolloServer({
  schema: buildSubgraphSchema([{ typeDefs, resolvers }]),
  includeStacktraceInErrorResponses: false,
});

startStandaloneServer(server, {
  listen: { port: 4003 },
  context: async ({ req }) => ({ req, discountInfo: createDiscountCache() }),
}).then(() => {
  console.log('✅ Promocode subgraph ready at http://localhost:4003/');
});
