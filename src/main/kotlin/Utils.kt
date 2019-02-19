package com.datastax.opscenter.graphql

import com.google.common.base.Preconditions
import graphql.schema.DataFetchingEnvironment

fun <T> validateSource(env: DataFetchingEnvironment, expected: Class<T>):T {
	
	/* No method completion for Java types in Kotlin files?  Boooo! */
	val srcObject:Any = env.getSource()
	Preconditions.checkArgument(srcObject::class.java.isAssignableFrom(expected), 
			String.format("Object of type %s cannot be cast to class %s", srcObject::class.java, expected));
	return expected.cast(srcObject);
}
