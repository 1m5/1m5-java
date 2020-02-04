# 1M5 Neo4J

## Background
Graph support was chosen as the need to traverse a large number of relationships
is apparent when networking within a peer-to-peer network. Finding the shortest
path based on a number of criteria can be helpful in maximizing efficient
communications and graphs are known to solve this problem the best.

Neo4J was chosen as the graph database as it is quite stable with a long history
of development and more focused on single database implementations vs large
corporate clusters, e.g. Janus graph.

## Neo4jDB
A wrapper around the Neo4J GraphDatabaseService for managing
its lifecycle within 1M5 and for providing common simple methods
of access.

## GraphEngine
GraphDatabaseService of Neo4J.

## Relationship Types
Relationship types used within the 1M5 implementation.
Feel free to extend this or use your own.

## Graph Util
Methods to simplify mapping between a Neo4J graph and a map.




