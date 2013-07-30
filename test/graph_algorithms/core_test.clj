(ns graph-algorithms.core-test
  (:require [midje.sweet :refer :all]
            [graph-algorithms.core :refer :all]))

(defn tree-like [vertices & edges]
  (just {:vertices (just vertices :in-any-order) , :edges (just edges :in-any-order)}))

(defn minimum-spanning-tree-test [n f]
  (let [algorithm-details (str "Algorithm: " n)]
    (facts algorithm-details
           (fact "simple graphs give obvious results"
                 (f {:vertices (), :edges ()})                         => {:vertices (), :edges ()}
                 (f {:vertices '(A), :edges ()})                       => {:vertices '(A), :edges ()}
                 (f {:vertices '(A B), :edges '( [A B 1] )})           => (tree-like '(A B) '[A B 1])
                 (f {:vertices '(A B C), :edges '( [A B 1] [B C 1] )}) => (tree-like '(A B C) '[A B 1] '[B C 1])
                 (f {:vertices '(A B C), :edges '( [A B 1] [A C 1] )}) => (tree-like '(A B C) '[A B 1] '[A C 1]))

           (fact "obvious graphs give obvious results"
                 (f {:vertices '(A B),   :edges '( [A B 1] [B A 2] )})         => (tree-like '(A B) '[A B 1])
                 (f {:vertices '(A B C), :edges '( [A B 1] [B C 1] [A C 3] )}) => (tree-like '(A B C) '[A B 1] '[B C 1])
                 (f {:vertices '(A B C), :edges '( [A B 1] [B C 3] [A C 1] )}) => (tree-like '(A B C) '[A B 1] '[A C 1])
                 (f {:vertices '(A B C), :edges '( [A B 3] [B C 1] [A C 1] )}) => (tree-like '(A B C) '[B C 1] '[A C 1]))

           (fact "the example graph from Wikipedia"
                 (f {:vertices '(A B C D E F G), :edges '( [A D 5] [A B 7] [B C 8] [B E 7] [B D 9] [C E 5] [D E 15] [D F 6] [E F 8] [E G 9] [F G 11] )}) =>
                   (tree-like '(A B C D E F G) '[A D 5] '[C E 5] '[D F 6] '[A B 7] '[B E 7] '[E G 9]))
           )))

(facts "Minimum spanning trees"
       (minimum-spanning-tree-test "Kruskal" minimum-spanning-tree-by-kruskal)
       (minimum-spanning-tree-test "Prim" minimum-spanning-tree-by-prim)
       )

(defn walker-check [walker i j d path]
  (fact "distance" (distance-between walker i j) => d)
  (fact "path" (path-between walker i j) => path))

(defn edges->graph [edges]
  {:vertices (set (mapcat (fn [[i j _]] [i j]) edges)), :edges edges})

(defn path-test [n f]
  (let [algorithm-details (str "Algorithm: " n)]
    (facts algorithm-details
           (facts "Shortest path"
                  (fact "simple graphs give obvious results"
                        (walker-check (f (edges->graph '()) shortest-path)                  'A 'B length-of-unknown-path '())
                        (walker-check (f {:vertices '(A), :edges '()} shortest-path)        'A 'B length-of-unknown-path '())
                        (walker-check (f (edges->graph '( [A B 1] )) shortest-path)         'A 'B 1 '(A B))
                        (walker-check (f (edges->graph '( [A B 1] [B C 1] )) shortest-path) 'A 'C 2 '(A B C)))

                  (fact "obvious graphs give obvious results"
                        (walker-check (f (edges->graph '( [A B 1] [B C 1] [A C 3] )) shortest-path) 'A 'C 2 '(A B C))
                        (walker-check (f (edges->graph '( [A B 1] [B C 1] [A C 1] )) shortest-path) 'A 'C 1 '(A C)))

                  (fact "the example graph from Wikipedia"
                        (let [w (f (edges->graph '( [A C -2] [B A 4] [B C 3] [C D 2] [D B -1])) shortest-path)]
                          (walker-check w 'A 'B -1 '(A C D B))
                          (walker-check w 'D 'A  3 '(D B A))
                          ))
                  )

           (facts "Maximum flow"
                  (fact "simple graphs give obvious results"
                        (walker-check (f (edges->graph '()) maximum-flow)                  'A 'B length-of-unknown-path '())
                        (walker-check (f {:vertices '(A), :edges '()} maximum-flow)        'A 'B length-of-unknown-path '())
                        (walker-check (f (edges->graph '( [A B 1] )) maximum-flow)         'A 'B 1 '(A B))
                        (walker-check (f (edges->graph '( [A B 1] [B C 1] )) maximum-flow) 'A 'C 1 '(A B C)))

                  (fact "obvious graphs give obvious results"
                        (walker-check (f (edges->graph '( [A B 1] [B C 1] [A C 3] )) maximum-flow) 'A 'C 3 '(A C))
                        (walker-check (f (edges->graph '( [A B 2] [B C 2] [A C 1] )) maximum-flow) 'A 'C 2 '(A B C)))

                  (fact "the example graph from Wikipedia"
                        (let [w (f (edges->graph '( [A C -2] [B A 4] [B C 3] [C D 2] [D B -1])) maximum-flow)]
                          (walker-check w 'A 'B -2 '(A C D B))
                          (walker-check w 'D 'A -1 '(D B A))
                          ))
                  )
           )))

(facts "Paths through graphs"
       (path-test "Floyd-Warshall" floyd-warshall-graph-walker)
       )
