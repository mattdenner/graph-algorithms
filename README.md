# graph-algorithms
## Introduction
This is a library for me to implement various graph, tree and collection oriented functions in [Clojure](http://clojure.org).

## Structures
`{:vertices '(), :edges '()}` is a representation of an empty graph or tree.  Vertices are symbols, edges are triples of `[start finish weight]`.

## Algorithms
* `(graph-algorithms/minimum-spanning-tree-by-kruskal graph)` finds the minimum spanning tree of a given graph using [Kruskal's algorithm](http://en.wikipedia.org/wiki/Kruskal%27s_algorithm)
* `(graph-algorithms/minimum-spanning-tree-by-prim graph)` finds the minimum spanning tree of a given graph using [Prim's algorithm](http://en.wikipedia.org/wiki/Prim%27s_algorithm)

## License

Copyright © 2013 Matthew Denner

Distributed under the Eclipse Public License, the same as Clojure.
