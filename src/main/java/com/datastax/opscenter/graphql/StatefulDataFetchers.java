package com.datastax.opscenter.graphql;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Session;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class StatefulDataFetchers {

	/* =============================== Top-level fetcher =============================== */
	private static class KeyspaceDataFetcher implements DataFetcher<KeyspaceMetadata> {

		private final Session session;
		
		public KeyspaceDataFetcher(Session session) {
			
			this.session = session;
		}
		
		@Override
		public KeyspaceMetadata get(DataFetchingEnvironment environment) throws Exception {
			
			String name = environment.getArgument("name");
			return session.getCluster().getMetadata().getKeyspace(name);
		}
	}
	
	public static DataFetcher<KeyspaceMetadata> keyspaceMetadata(Session session) { return new KeyspaceDataFetcher(session); }
}
