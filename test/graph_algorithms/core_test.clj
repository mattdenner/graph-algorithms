(ns graph-algorithms.core-test
  (:require [midje.sweet :refer :all]
            [graph-algorithms.core :refer :all]))

(defn tree-like [vertices & edges]
  (just {:vertices (just vertices :in-any-order) , :edges (just edges :in-any-order)}))

(defn minimum-spanning-tree-test [n f]
  (let [algorithm-details (str "Algorithm: " n)]
    (facts "Minimum spanning trees"
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
                  ))
    ))

(minimum-spanning-tree-test "Kruskal" minimum-spanning-tree-by-kruskal)
(minimum-spanning-tree-test "Prim" minimum-spanning-tree-by-prim)
