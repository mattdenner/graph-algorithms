(ns graph-algorithms.core)

; Support functions
(defn- weight-comparator [left right]
  (< (last left) (last right)))

; These functions support operations on a forest of trees
(defn- find-tree-for-vertex-in-forest [v]
  (fn [forest]
    (some (fn [t] (when (some #(= v %) (:vertices t)) t)) forest)))

(defn- remove-trees-from-forest [& trees]
  (fn [forest]
    (remove (fn [t] (some #(= t %) trees)) forest)))

; These functions are the basic manipulators of trees
(defn- merge-trees [left right]
  {:vertices (concat (:vertices left) (:vertices right)) :edges (concat (:edges left) (:edges right))})

(defn- add-branch [tree branch]
  {:vertices (:vertices tree), :edges (cons branch (:edges tree))})

(defn- add-vertex [tree vertex]
  (if (nil? vertex) tree {:vertices (cons vertex (:vertices tree)), :edges (:edges tree)}))

(defn- edge-out-of [{vertices :vertices}]
  (let [out-of (fn [u v] (and (some #(= u %) vertices) (not (some #(= v %) vertices))))]
    (fn [[u v d]]
      (cond
        (out-of u v) [v [u v d]]
        (out-of v u) [u [u v d]]))
    ))

; And here's the empty tree
(def empty-tree
  {:vertices '(), :edges '()})

(defn minimum-spanning-tree-by-kruskal
  "Given a connected weighted undirected graph this function returns the minimum spanning tree determined by
  applying Kruskal's algorithm.

  Essentially this involves putting every vertex of the graph into it's own tree, and then joining up the
  trees using the smallest edge between them.  Any edges that start and end in the same tree are discarded.
  It's complete when all of the edges have been used or discarded."
  [{vertices :vertices, edges :edges}]
  (loop [sorted-edges (sort weight-comparator edges)
         vertex-trees (map (partial add-vertex empty-tree) vertices)]
    (if (empty? sorted-edges)
      (or (first vertex-trees) empty-tree)
      (let [[[u v d] & to-walk] sorted-edges
            tree-with-u         ((find-tree-for-vertex-in-forest u) vertex-trees)
            tree-with-v         ((find-tree-for-vertex-in-forest v) vertex-trees)]
        (if (= tree-with-u tree-with-v)
          (recur to-walk vertex-trees)
          (recur to-walk
                 (conj
                   ((remove-trees-from-forest tree-with-u tree-with-v) vertex-trees)
                   (add-branch (merge-trees tree-with-u tree-with-v) [u v d])))
        ))
    )))

(defn minimum-spanning-tree-by-prim
  "Given a connected weighted undirected graph this function returns the minimum spanning tree determined
  by applying Prim's algorithm.

  The algorithm is pretty simple: pick any vertex to start from, then find the shortest edge from it to
  another vertex and add that to your tree. Repeat this process of picking the shortest edge from any of
  the vertices in the tree being built.  When all vertices from the original graph are in the tree you
  have the minimum spanning tree."
  [{vertices :vertices, edges :edges}]
  (loop [sorted-edges (sort weight-comparator edges)
         current-tree (add-vertex empty-tree (first vertices))]
    (if (empty? sorted-edges)
      current-tree
      (if (empty? (clojure.set/difference (set vertices) (set (:vertices current-tree))))
        current-tree
        (let [[vertex edge] (some (edge-out-of current-tree) sorted-edges)]
          (recur
            (remove #(= edge %) sorted-edges)
            (add-branch (add-vertex current-tree vertex) edge)))))))
