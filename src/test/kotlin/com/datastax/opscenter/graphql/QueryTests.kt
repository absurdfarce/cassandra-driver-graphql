package com.datastax.opscenter.graphql

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.Metadata
import com.datastax.driver.core.Session
import com.datastax.driver.core.TableMetadata
import com.google.gson.Gson
import graphql.ExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import org.junit.Test
import java.io.FileReader
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class QueryTests() {
	
	fun runTest(query:String, clz:(result:ExecutionResult, session:Session) -> Unit) {
		
		val session = Cluster.builder().addContactPoint("127.0.0.1").build().connect()
		try {

			val schema = buildSchema(FileReader("cluster.graphqls"), session)
			val graphQL = GraphQL.newGraphQL(schema).build()

			val executionInput = ExecutionInput.newExecutionInput().query(query).build()
			val result = graphQL.execute(executionInput)

			clz(result, session)
		}
		finally { session.close() }
	}

	@Test fun testAllTheThingsOneKeyspace() {
		
		val query = """
query { 
   keyspace(name: "system") { 
      tableCount 
      tables { 
         name
      }
   }
}
"""
		runTest(query) { result, session ->

			val errors = result.errors
			if (errors !is List<*>) { fail("Query errors not of type List") }
			assertTrue(errors.isEmpty())
			
			val data:Any = result.getData()
			if (data !is Map<*,*>) { fail("Returned data not of type Map") }
				
			assertFalse(data.isEmpty())
			val keyspaces = data.get("keyspace")
			if (keyspaces !is List<*>) { fail("Keyspace data not of type List") }

			assertFalse(keyspaces.isEmpty())
			val keyspaceData = keyspaces.first()
			if (keyspaceData !is Map<*,*>) { fail("Keyspace entry not of type Map") }
						
			assertNotNull(keyspaceData)
			assertEquals(2, keyspaceData.keys.size)
			assertEquals(16, keyspaceData.get("tableCount"))

			val tableData = keyspaceData.get("tables")
			if (tableData !is List<*>) { fail("Table data entry not of type List") }
			assertEquals(16, tableData.size)

			val driverTableNames = session.cluster.metadata.getKeyspace("system").tables.fold(emptySet()) { acc:Set<String>,newval:Any? ->
				if (newval is TableMetadata) {
					acc + newval.name
				} else { acc }				
			} 
			val graphqlTableNames = tableData.fold(emptySet()) { acc:Set<String>,newval:Any? ->
				
				if (newval is Map<*,*>) {
					acc + newval.get("name").toString()
				} else { acc }				
			} 
			assertEquals(driverTableNames, graphqlTableNames)
		}
	}
	
	@Test fun testOnlySomeSubelementsOneKeyspace() {
		
		val query = """
query { 
   keyspace(name: "system") { 
      tableCount 
   }
}
"""
		
		runTest(query) { result, _ ->
			
			val errors = result.errors
			assertTrue(errors.isEmpty())
			
			val data:Any = result.getData()
			if (data !is Map<*,*>) { fail("Returned data not of type Map") }
			assertFalse(data.isEmpty())
			
			val keyspaces = data.get("keyspace")
			if (keyspaces !is List<*>) { fail("Keyspace data not of type List") }
			
			val keyspaceData = keyspaces.first()
			if (keyspaceData !is Map<*,*>) { fail("Keyspace entry not of type Map") }
			assertNotNull(keyspaceData)
			assertEquals(1, keyspaceData.keys.size)			
			assertEquals(16, keyspaceData.get("tableCount"))
		}
	}

	@Test fun testAllKeyspace() {

		val query = """
query {
   keyspace {
      name
      tableCount
   }
}
"""

		runTest(query) { result, session ->

			val errors = result.errors
			assertTrue(errors.isEmpty())

			val data:Any = result.getData()
			if (data !is Map<*,*>) { fail("Returned data not of type Map") }
			assertFalse(data.isEmpty())

			val keyspaces = data.get("keyspace")
			if (keyspaces !is List<*>) { fail("Keyspace data not of type List") }
			
			// My test instance has 16 keyspaces, YMMV
			assertEquals(16, keyspaces.size)
			
			for (keyspace in keyspaces) {
				
				if (keyspace !is Map<*,*>) { fail("Keyspace entry not of type Map") }
				assertEquals(
					session.cluster.metadata.getKeyspace(Metadata.quoteIfNecessary(keyspace.get("name").toString())).tables.size,
					keyspace.get("tableCount"))
			}
		}
	}

	/* Tests below aren't legit unit tests... just monkeying around with the introspection interface */
	@Test fun testIntrospectionAllTypes() {

		val query = """
{
   __schema {
      types {
         name
      }
   }
}
"""

		runTest(query) { result, _ ->
			val data:Any = result.getData()
			if (data !is Map<*,*>) { fail("Returned data not of type Map") }
			println(Gson().toJson(data))
		}
	}

	@Test fun testIntrospectionTypeDetails() {

		val query = """
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

		runTest(query) { result, _ ->
			val data:Any = result.getData()
			if (data !is Map<*,*>) { fail("Returned data not of type Map") }
			println(Gson().toJson(data))
		}
	}
}
