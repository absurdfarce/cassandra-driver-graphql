package com.datastax.opscenter.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

/**
 * Only exists to mark {@link DataFetcher} as a functional interface
 *
 * @param <T> the type of the object returned
 */
@FunctionalInterface
public interface LambdaDataFetcher<T> extends DataFetcher<T> {

	public T get(DataFetchingEnvironment env);
}
