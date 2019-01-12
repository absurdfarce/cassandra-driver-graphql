package com.datastax.opscenter.graphql;

import com.datastax.driver.core.KeyspaceMetadata;
import com.google.common.base.Preconditions;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

/**
 * DataFetcher implementations which are truly stateless
 */
public class StatelessDataFetchers {

	/* =============================== KeyspaceMetadata fetchers =============================== */
	private static class TableCountDataFetcher implements DataFetcher<Integer> {

		@Override
		public Integer get(DataFetchingEnvironment environment) throws Exception {
			
			Object metadataObject = environment.getSource();
			Preconditions.checkArgument(metadataObject instanceof KeyspaceMetadata, "Input to TableCountDataFetcher should be a KeyspaceMetadata object");
			return ((KeyspaceMetadata)metadataObject).getTables().size();
		}
	}
	
	private static DataFetcher<Integer> tableCountFetcher = new TableCountDataFetcher();
	
	public static DataFetcher<Integer> keyspaceTableCount() { return tableCountFetcher; }
}
