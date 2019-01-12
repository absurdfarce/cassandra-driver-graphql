package com.datastax.opscenter.graphql;

import java.io.FileReader;
import java.util.List;

import org.junit.Test;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.schema.GraphQLSchema;

public class BaseGraphQLTest {
			
	@Test
	public void baseTest() throws Exception {

		Session session = Cluster.builder().addContactPoint("127.0.0.1").build().connect();
		try {

			GraphQLSchema schema = CassandraDriverSchemaFactory.buildSchema(new FileReader("cluster.graphqls"), session);
			GraphQL graphQL = GraphQL.newGraphQL(schema).build();
			
			ExecutionInput executionInput = ExecutionInput.newExecutionInput().query("query { keyspace(name: \"system\") { tableCount tables { name } } }").build();
			ExecutionResult executionResult = graphQL.execute(executionInput);
			Object data = executionResult.getData();
			System.out.println("Data: " + data);
			System.out.println("Data class: " + data.getClass().toString());
			List<GraphQLError> errors = executionResult.getErrors();
			System.out.println("Errors: " + errors);
		}
		finally { session.close(); }
	}
}
