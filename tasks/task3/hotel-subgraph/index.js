import { ApolloServer } from '@apollo/server';
import { startStandaloneServer } from '@apollo/server/standalone';
import { buildSubgraphSchema } from '@apollo/subgraph';
import gql from 'graphql-tag';
import { readFileSync } from 'fs';
import { resolvers } from './resolvers.js';
import { createHotelLoader } from './loaders.js';

const typeDefs = gql(readFileSync(new URL('./schema.graphql', import.meta.url), 'utf8'));

const server = new ApolloServer({
  schema: buildSubgraphSchema([{ typeDefs, resolvers }]),
  includeStacktraceInErrorResponses: false,
});

startStandaloneServer(server, {
  listen: { port: 4002 },
  context: async ({ req }) => ({ req, hotelLoader: createHotelLoader() }),
}).then(() => {
  console.log('✅ Hotel subgraph ready at http://localhost:4002/');
});
