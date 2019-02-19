package com.datastax.opscenter.graphql;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Session;
import com.google.common.collect.ImmutableList;

import graphql.schema.idl.RuntimeWiring;

/**
 * Builder impl for adding type wirings as needed.  Note that we don't have to add wirings for any props that
 * follow the JavaBeans getter conventions; by default the PropertyDataFetcher is used for props without an
 * otherwise defined DataFetcher impl.
 *
 * The root idea being exercised here is that DataFetchers implemented by lambdas at wiring construction time
 * can be useful by closing over necessary state, in this case sessions (or, more realistically, probably clusters).
 */
public class CassandraDriverWiringBuilder {

	private final RuntimeWiring.Builder builder;
	
	public CassandraDriverWiringBuilder() {

		builder = RuntimeWiring.newRuntimeWiring();
	}
	
	/* Add wirings for top-level query types */
	public CassandraDriverWiringBuilder query(Session session) {
		
		builder.type("QueryType", typeWiring -> typeWiring
				.dataFetcher("keyspace", env -> {

					/* Make the "name" param optional; if not supplied we should retrieve everybody */
					if (env.containsArgument("name")) {

						String name = env.getArgument("name");
						return ImmutableList.of(session.getCluster().getMetadata().getKeyspace(name));
					}
					else {

						return ImmutableList.copyOf(session.getCluster().getMetadata().getKeyspaces());
					}
				}));
		return this;
	}
	
	/* Add wirings for keyspace types */
	public CassandraDriverWiringBuilder keyspace() {
		
		builder.type("Keyspace", typeWiring -> typeWiring
				.dataFetcher("tableCount", env -> {

					return UtilsKt.validateSource(env, KeyspaceMetadata.class).getTables().size();
				}));
		return this;
	}
	
	public RuntimeWiring build() {

		return this.builder.build();
	}
}
