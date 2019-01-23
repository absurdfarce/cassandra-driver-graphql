package com.datastax.opscenter.graphql;

import com.google.common.base.Preconditions;

import graphql.schema.DataFetchingEnvironment;

public class Utils {

	public static <T> T validateSource(DataFetchingEnvironment env, Class<T> expected) {
				
		Object srcObject = env.getSource();
		Preconditions.checkArgument(srcObject.getClass().isAssignableFrom(expected), 
				String.format("Object of type %s cannot be cast to class %s", srcObject.getClass(), expected));
		return expected.cast(srcObject);
	}
}
