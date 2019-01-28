package com.datastax.opscenter.graphql.servlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Minimalist HTTP server to drive our graphql servlet
 */
public class BaseServer {

	public static void main(String[] args) {

		try {
			
			Server server = new Server(8888);
			
			ContextHandlerCollection contexts = new ContextHandlerCollection();
			server.setHandler(contexts);
			
			ServletContextHandler graphqlContext = new ServletContextHandler(contexts, "/graphql", ServletContextHandler.SESSIONS);
			ServletHolder graphqlHolder = new ServletHolder(new BaseServlet());
			
			/* Since these vals are setup progammtically we could get them from anywhere in a real-world app */
			graphqlHolder.setInitParameter("dseIP", "127.0.0.1");
			graphqlHolder.setInitParameter("schemaFile", "cluster.graphqls");
			
			graphqlContext.addServlet(graphqlHolder, "/");
			
			server.start();
		}
		catch (Exception e) {
			System.out.println("Exception for you sir");
			e.printStackTrace();
		}
	}
}
