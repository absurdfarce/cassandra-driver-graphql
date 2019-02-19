package com.datastax.opscenter.graphql.servlet;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.opscenter.graphql.WiringKt;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

/**
 * Simple servlet to execute GraphQL queries and render the results
 */
public class BaseServlet extends HttpServlet {

	private static final long serialVersionUID = -2345239536453863505L;

	private final Gson gson;
	private GraphQL graphQL;

	public BaseServlet() {
		
		this.gson = new Gson();
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {

		try {
			
			/* TODO: a long-lived session like this isn't how you'd want to do this in a real app; ideally this would
			 * be some kind of session supplier which could apply behaviours to changing states.  Going with the basic
			 * approach here for sake of expediency. */
			String dseIP = config.getInitParameter("dseIP");
			Session session = Cluster.builder().addContactPoint(dseIP).build().connect();
			
			/* TODO: in a real app the schema file would be supplied via config.getServletContext().getResource() (or
			 * even statically generated via the programmatic API).  As above we're doing this now for sake of expediency. */
			String schemaFile = config.getInitParameter("schemaFile");
			GraphQLSchema schema = WiringKt.buildSchema(new FileReader(schemaFile), session);
			this.graphQL = GraphQL.newGraphQL(schema).build();
		}
		catch (IOException ioe) {
			throw new ServletException("Exception initializing base servlet", ioe);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String query = req.getReader().lines().collect(Collectors.joining());

		/* What, no InputStream/Reader support here graphql? */
		ExecutionInput executionInput = ExecutionInput.newExecutionInput().query(query).build();
		ExecutionResult result = graphQL.execute(executionInput);

		JsonWriter respWriter = new JsonWriter(new BufferedWriter(resp.getWriter()));
		this.gson.toJson(result.getData(), Map.class, respWriter);
		respWriter.flush();
	}
}
