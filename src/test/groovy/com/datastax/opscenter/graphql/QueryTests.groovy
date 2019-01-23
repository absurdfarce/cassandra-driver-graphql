package com.datastax.opscenter.graphql

import static org.junit.Assert.*

import org.junit.Test

import com.datastax.driver.core.Cluster

import graphql.ExecutionInput
import graphql.GraphQL

class QueryTests {
	
	def runTest(query, session) {
		
		def schema = CassandraDriverSchemaFactory.buildSchema(new FileReader("cluster.graphqls"), session);
		def graphQL = GraphQL.newGraphQL(schema).build();

		def executionInput = ExecutionInput.newExecutionInput().query(query).build();
		graphQL.execute(executionInput);
	}

	@Test
	public void testAllTheThings() {
		
		def query = """
query { 
   keyspace(name: \"system\") { 
      tableCount 
      tables { 
         name
      }
   }
}
"""
		
		def session = Cluster.builder().addContactPoint("127.0.0.1").build().connect();
		try { 
			
			def result = runTest(query, session)
			
			def errors = result.errors
			assertTrue(errors.isEmpty())
			
			def data = result.data
			assertTrue(data instanceof Map)
			assertFalse(data.isEmpty())
			def keyspaceData = data.keyspace
			assertNotNull(keyspaceData)
			assertTrue(keyspaceData.keySet().size() == 2)
			assertTrue(keyspaceData.tableCount == 16)
			assertTrue(keyspaceData.tables instanceof List)
			assertTrue(keyspaceData.tables.size() == 16)
			
			def driverTableNames = session.cluster.metadata.getKeyspace('system').tables.collect { it.name }.toSet()
			def graphqlTableNames = keyspaceData.tables.collect() { it.name }.toSet()
			assertTrue(driverTableNames.equals(graphqlTableNames))
		}
		finally { session.close() }
	}
	
	@Test
	public void testOnlySomeSubelements() {
		
		def query = """
query { 
   keyspace(name: \"system\") { 
      tableCount 
   }
}
"""
		
		def session = Cluster.builder().addContactPoint("127.0.0.1").build().connect();
		try { 
			
			def result = runTest(query, session)
			
			def errors = result.errors
			assertTrue(errors.isEmpty())
			
			def data = result.data
			assertTrue(data instanceof Map)
			assertFalse(data.isEmpty())
			def keyspaceData = data.keyspace
			assertNotNull(keyspaceData)
			assertTrue(keyspaceData.keySet().size() == 1)
			assertTrue(keyspaceData.tableCount == 16)
		}
		finally { session.close() }
	}
}
