package com.datastax.opscenter.graphql

import com.datastax.driver.core.KeyspaceMetadata
import com.datastax.driver.core.Session
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import java.io.Reader

fun buildSchema(schemaSDLReader:Reader, session:Session):GraphQLSchema {
	/* TODO: if SchemaParser and SchemaGenerator are thread-safe t'would be nice to just have a single
	 * static instance of them */
	val typeRegistry = SchemaParser().parse(schemaSDLReader)
	val wiring = CassandraDriverWiringBuilder().query(session).keyspace().build()
	return SchemaGenerator().makeExecutableSchema(typeRegistry, wiring)
}

class CassandraDriverWiringBuilder {

	val delegate = RuntimeWiring.newRuntimeWiring()

	fun query(session:Session):CassandraDriverWiringBuilder {

		delegate.type("QueryType") { wiring ->
			wiring.dataFetcher("keyspace") { env ->
				
				/* Make the "name" param optional; if not supplied we should retrieve everybody */
				if (env.containsArgument("name")) {
					val ksName:Any = env.getArgument("name")
					listOf(session.getCluster().getMetadata().getKeyspace(ksName.toString()))
				}
				else { session.getCluster().getMetadata().getKeyspaces().toList() }
			}
		}
		return this
	}
	
	fun keyspace():CassandraDriverWiringBuilder {
		
		delegate.type("Keyspace") { wiring ->
			wiring.dataFetcher("tableCount") { env ->
				val srcObject:Any = env.getSource()
				if (srcObject is KeyspaceMetadata) {
					srcObject.getTables().size
				} else { -1 }
			}
		}
		return this
	}
	
	fun build():RuntimeWiring {
		return delegate.build()
	}
}