package com.datastax.opscenter.graphql;

import com.datastax.driver.core.Session;

import graphql.schema.idl.RuntimeWiring;

/**
 * Builder impl for adding type wirings as needed.  Note that we don't have to add wirings for any props that
 * follow the JavaBeans getter conventions; by default the PropertyDataFetcher is used for props without an
 * otherwise defined DataFetcher impl.
 */
public class CassandraDriverWiringBuilder {

	private final RuntimeWiring.Builder builder;
	
	public CassandraDriverWiringBuilder() {

		builder = RuntimeWiring.newRuntimeWiring();
	}
	
	/* Add wirings for top-level query types */
	public CassandraDriverWiringBuilder query(Session session) {
		
		builder.type("QueryType", typeWiring -> typeWiring
				.dataFetcher("keyspace", StatefulDataFetchers.keyspaceMetadata(session)));
		return this;
	}
	
	/* Add wirings for keyspace types */
	public CassandraDriverWiringBuilder keyspace() {
		
		builder.type("Keyspace", typeWiring -> typeWiring
				.dataFetcher("tableCount", StatelessDataFetchers.keyspaceTableCount()));
		return this;
	}
	
	public RuntimeWiring build() {

		return this.builder.build();
	}
}
