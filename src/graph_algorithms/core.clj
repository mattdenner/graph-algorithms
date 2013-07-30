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

; Here are some simple constants for graphs and paths
(def empty-tree {:vertices '(), :edges '()})
(def length-of-unknown-path (Long/MAX_VALUE))

(defprotocol GraphWalker
  "Functions that determine the shortest path between two vertices i and j in a graph return instances
  instances of this protocol."

  (distance-between [walker i j] "Returns the shortest distance between vertices i and j, length-of-unknown-path if there is no path")
  (path-between     [walker i j] "Returns the shortest path between vertices i and j, empty sequence if there is no path"))

(defprotocol Router
  "Routing between two vertices i and j in a graph can be controlled by an implementation of this protocol."

  (acceptable? [this value check-against] "Returns true if value is acceptable under the current check-against value")
  (operation   [this left right] "Returns the result of left and right put through the appropriate operation"))

(defn- router [acceptor operation]
  (reify Router
    (acceptable? [_ v c] (acceptor v c))
    (operation   [_ l r] (operation l r))))

(def ^{:doc "Router for shortest path"} shortest-path (router < +))
(def ^{:doc "Router for maximum flow"}  maximum-flow  (router > min))

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

(defn- all-triple-vertex-paths
  "Generates all possible vertex triples that describe a path from i to j through k"
  [vertices]
  (for [k vertices
        i (remove #(= % k) vertices)
        j (remove #(some (partial = %) [k i]) vertices)]
    [i k j]))

(defn- prepare-floyd-warshall [{vertices :vertices, edges :edges} router]
  (let [vertex-vertex-distance (apply hash-map (mapcat (fn [v] [[v,v] 0]) vertices))
        distances              (reduce (fn [dist [u v d]] (assoc dist [u,v] d)) vertex-vertex-distance edges)
        next-vertex            {}]
    (reduce
      (fn [[distances next-vertex :as current-state] [i k j]]
        (let [distance-between (fn [u v] (get distances [u,v]))
              d-i-j            (distance-between i j)
              d-i-k            (distance-between i k)
              d-k-j            (distance-between k j)]
          (cond
            (nil? d-i-k)                                              current-state
            (nil? d-k-j)                                              current-state
            (nil? d-i-j)                                              [(assoc distances [i,j] (operation router d-i-k d-k-j)) (assoc next-vertex [i,j] k)]
            (acceptable? router (operation router d-i-k d-k-j) d-i-j) [(assoc distances [i,j] (operation router d-i-k d-k-j)) (assoc next-vertex [i,j] k)]
            :default                                                  current-state
            )))
      [distances next-vertex]
      (all-triple-vertex-paths vertices))))

(defn floyd-warshall-graph-walker
  "Given a directed weighted connected graph this function returns a GraphWalker that will give the path
  information between two points, controlled by the Router.

  The algorithm generates two sparse matrices: one records the distance between vertices i and j, the other
  records a vertex k through which the path i-j passes."
  [graph router]
  (let [[distances next-vertex]   (prepare-floyd-warshall graph router)
        intermediate-path-between (fn myself [i j] (let [k (get next-vertex [i,j])] (if (nil? k) [] (concat (myself i k) [k] (myself k j)))))]
    (reify GraphWalker
      (distance-between [_ i j] (get distances [i,j] length-of-unknown-path))
      (path-between     [w i j] () (if (= (distance-between w i j) length-of-unknown-path) [] (concat [i] (intermediate-path-between i j) [j]))))
    ))
