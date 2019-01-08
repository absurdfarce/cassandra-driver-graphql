package com.datastax.opscenter.graphql;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Session;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

public class BaseGraphQLTest {
	
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
	
	private static class TableCountDataFetcher implements DataFetcher<Integer> {

		@Override
		public Integer get(DataFetchingEnvironment environment) throws Exception {
			
			KeyspaceMetadata metadata = (KeyspaceMetadata)environment.getSource();
			return metadata.getTables().size();
		}
	}
	
	private RuntimeWiring buildRuntimeWiring(Session session) {
		return RuntimeWiring.newRuntimeWiring()
				.type("QueryType", typeWiring -> typeWiring
						.dataFetcher("keyspace", new KeyspaceDataFetcher(session)))
				.type("Keyspace", typeWiring -> typeWiring
						.dataFetcher("tableCount", new TableCountDataFetcher()))
				.build();
	}
	
	private GraphQLSchema buildSchema(Session session) {
		
		SchemaParser schemaParser = new SchemaParser();
		SchemaGenerator schemaGenerator = new SchemaGenerator();

		TypeDefinitionRegistry typeRegistry = schemaParser.parse(new File("cluster.graphqls"));
		RuntimeWiring wiring = buildRuntimeWiring(session);
		return schemaGenerator.makeExecutableSchema(typeRegistry, wiring);
	}
	
	@Test
	public void baseTest() throws Exception {

		Session session = Cluster.builder().addContactPoint("127.0.0.1").build().connect();
		try {

			GraphQLSchema schema = buildSchema(session);
			GraphQL graphQL = GraphQL.newGraphQL(schema).build();
			
			ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("query { keyspace(name: \"system\") { tableCount tables { name } } }").build();
			ExecutionResult executionResult = graphQL.execute(executionInput);
			Object data = executionResult.getData();
			System.out.println("Data: " + data);
			List<GraphQLError> errors = executionResult.getErrors();
			System.out.println("Errors: " + errors);
		}
		finally { session.close(); }
	}
}
