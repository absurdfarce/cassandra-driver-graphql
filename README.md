# cassandra-driver-graphql
Some experiments with exposing Cassandra driver metadata via GraphQL

# Wiring impl

Trying out idea of implementing wiring via a builder.  The build can expose individual methods for each type, taking whatever params are necessary for the DataFetcher's for
that type as an argument.

# Custom DataFetchers

Original concept for custom DataFetchers was a set of inner class exposed via static methods.  This quickly became somewhat cumbersome when it became apparent that lambdas
should be able to work for most (all?) of the interesting cases here.  Setup the use of lambdas as inline defs of DataFetchers and it seemed to work well, at least for the
simple case of this example.

One interesting side effect to consider: this approach makes testing DataFetchers in isolation very difficult.  You can't instantiate them directly under this arrangement,
although you _might_ be able to instantiate the wiring and extract them from that.  It's not immediately clear how big an impediment this actually is since you should still
be able to test the fetchers in combination with the the wiring... and using this as the atomic building block for unit testing doesn't seem at all unreasonable.

# Introspection

Initial goal was to be able to expose some representation of schema as well as answering queries but GraphQL beat me to the punch via the introspection API.  Added a few
tests to get an idea of what this API looks like in practice.

# Execution strategies

TBD

# End-to-end via a basic servlet

So what does this look like in real life?

com.datastax.opscenter.graphql.servlet.BaseServer is a simple app which starts up a Jetty instance containing a simple HTTP servlet designed to field GraphQL requests and reply
with JSON-encoded answers.  Starting this server with a running Cassandra instance on the local node enables things like the following:

```
$ more foo.graphql
query {
   keyspace(name: "system") {
      tableCount
      tables {
         name
      }
   }
}
$ curl -d @foo.graphql --header "Content-Type: plain/text" http://localhost:8888/graphql/ | python -mjson.tool
{
    "keyspace": [
        {
            "tableCount": 17,
            "tables": [
                {
                    "name": "built_views"
                },
                {
                    "name": "sstable_activity"
                },
                {
                    "name": "views_builds_in_progress"
                },
                {
                    "name": "compaction_history"
                },
...
```