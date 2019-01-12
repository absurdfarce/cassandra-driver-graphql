package com.datastax.opscenter.graphql;

import java.io.Reader;

import com.datastax.driver.core.Session;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

public class CassandraDriverSchemaFactory {

	public static GraphQLSchema buildSchema(Reader schemaSDLReader, Session session) {
		
		/* TODO: if SchemaParser and SchemaGenerator are thread-safe t'would be nice to just have a single
		 * static instance of them */
		TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(schemaSDLReader);
		RuntimeWiring wiring = new CassandraDriverWiringBuilder().query(session).keyspace().build();
		return new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
	}
}
