package com.datastax.opscenter.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@FunctionalInterface
public interface LambdaDataFetcher<T> extends DataFetcher<T> {

	public T get(DataFetchingEnvironment env);
}
