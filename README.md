# graph-algorithms
## Introduction
This is a library for me to implement various graph, tree and collection oriented functions in [Clojure](http://clojure.org).

## Structures
`{:vertices '(), :edges '()}` is a representation of an empty graph or tree.  Vertices are symbols, edges are triples of `[start finish weight]`.

## Algorithms
### Minimum Spanning Trees
A minimum spanning tree of a weighted graph is one such that all vertices are connected and the weights of the edges are minimised.
* `(graph-algorithms/minimum-spanning-tree-by-kruskal graph)` uses [Kruskal's algorithm](http://en.wikipedia.org/wiki/Kruskal%27s_algorithm);
* `(graph-algorithms/minimum-spanning-tree-by-prim graph)` uses [Prim's algorithm](http://en.wikipedia.org/wiki/Prim%27s_algorithm).

### Path Finding
There are various different types of paths through a weighted directed graph between two points which can be controlled via routers:
* `graph-algorithms/shortest-path` defines the shortest path between two vertices (edges cost);
* `graph-algorithms/maximum-flow` defines the maximum flow that can be made between two vertices (edges limit).

With these the following can determine the routes through graphs, all return `GraphWalker`:
* `(graph-algorithms/floyd-warshall-graph-walker graph router)` uses [Floyd-Warshall algorithm](http://en.wikipedia.org/wiki/Floyd%E2%80%93Warshall_algorithm).

## License

Copyright © 2013 Matthew Denner

Distributed under the Eclipse Public License, the same as Clojure.
