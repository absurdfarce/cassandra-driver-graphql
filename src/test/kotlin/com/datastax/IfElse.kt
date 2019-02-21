package com.datastax

import org.junit.Test
import kotlin.test.assertEquals

/* Noticed while working on the GraphQL code I noticed that Kotlin didn't appear to be returning a value for
 * some DataFetchers if we only used an if-block.  Turns out Kotlin requires an else block when "if" is used
 * as an expression, which is entirely reasonable.  Ideally I'd get a stronger warning 
 */
class IfElseTests() {
	
	@Test fun testIfElse() {
		val foo = (1..4).map { entry ->
			if ((entry % 2) == 0) {
				entry
			} else { 0 }
		}
		assertEquals(listOf(0,2,0,4), foo)
	}
	
	/* Lack of else block for the if below means we get back Unit here.  I do get a warning saying that "entry"
     * is unused but that's not exactly the same as saying "you're trying to use this as an expression but you're
     " missing something" */
	@Test fun testIfOnly() {
		val foo:List<Any> = (1..4).map { entry ->
			if ((entry % 2) == 0) {
				entry
			}
		}
		assertEquals(listOf(Unit,Unit,Unit,Unit), foo)
	}
}