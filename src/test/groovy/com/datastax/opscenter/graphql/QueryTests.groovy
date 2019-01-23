package com.datastax.opscenter.graphql

import static org.junit.Assert.*

import org.junit.Test

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.Metadata
import com.google.gson.Gson

import graphql.ExecutionInput
import graphql.GraphQL

class QueryTests {
	
	def runTest(query, clz) {
		
		def session = Cluster.builder().addContactPoint("127.0.0.1").build().connect()
		try {

			def schema = CassandraDriverSchemaFactory.buildSchema(new FileReader("cluster.graphqls"), session)
			def graphQL = GraphQL.newGraphQL(schema).build()

			def executionInput = ExecutionInput.newExecutionInput().query(query).build()
			def result = graphQL.execute(executionInput)

			clz(result, session)
		}
		finally { session.close() }
	}

	@Test
	public void testAllTheThingsOneKeyspace() {
		
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
		
		runTest(query) { result, session ->
			
			def errors = result.errors
			assertTrue(errors.isEmpty())
			
			def data = result.data
			assertTrue(data instanceof Map)
			assertFalse(data.isEmpty())
			def keyspaceData = data.keyspace[0]
			assertNotNull(keyspaceData)
			assertEquals(2, keyspaceData.keySet().size())
			assertEquals(16, keyspaceData.tableCount)
			assertTrue(keyspaceData.tables instanceof List)
			assertEquals(16, keyspaceData.tables.size())
			
			def driverTableNames = session.cluster.metadata.getKeyspace('system').tables.collect { it.name }.toSet()
			def graphqlTableNames = keyspaceData.tables.collect() { it.name }.toSet()
			assertTrue(driverTableNames.equals(graphqlTableNames))
		}
	}
	
	@Test
	public void testOnlySomeSubelementsOneKeyspace() {
		
		def query = """
query { 
   keyspace(name: \"system\") { 
      tableCount 
   }
}
"""
		
		runTest(query) { result, session ->
			
			def errors = result.errors
			assertTrue(errors.isEmpty())
			
			def data = result.data
			assertTrue(data instanceof Map)
			assertFalse(data.isEmpty())
			assertEquals(1, data.keyspace.size())
			def keyspaceData = data.keyspace[0]
			assertNotNull(keyspaceData)
			assertEquals(1, keyspaceData.keySet().size())
			assertEquals(16, keyspaceData.tableCount)
		}
	}

	@Test
	public void testAllKeyspace() {

		def query = """
query {
   keyspace {
      name
      tableCount
   }
}
"""

		runTest(query) { result, session ->

			def errors = result.errors
			assertTrue(errors.isEmpty())

			def data = result.data
			assertTrue(data instanceof Map)
			assertFalse(data.isEmpty())

			// My test instance has 20 keyspaces, YMMV
			assertEquals(20, data.keyspace.size())

			data.keyspace.each {
				assertEquals(
					session.cluster.metadata.getKeyspace(Metadata.quoteIfNecessary(it.name)).tables.size(),
					it.tableCount)
			}
		}
	}

	/* Tests below aren't legit unit tests... just monkeying around with the introspection interface */
	@Test
	public void testIntrospectionAllTypes() {

		def query = """
{
   __schema {
      types {
         name
      }
   }
}
"""

		runTest(query) { result, session ->
			println new Gson().toJson(result.data)
		}
	}

	@Test
	public void testIntrospectionTypeDetails() {

		def query = """
{
   __type(name:"Keyspace") {
      name
      fields {
         name
         type {
            name
            kind
         }
      }
   }
}
"""

		runTest(query) { result, session ->
			println new Gson().toJson(result.data)
		}
	}
}
