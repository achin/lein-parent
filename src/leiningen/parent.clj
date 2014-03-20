(ns leiningen.parent)

(defn select-keys-in
  "Returns a map containing only those entries or sub-entries in m whose key
  path is in ksseq. Similar to select-keys except each value in ksseq is either
  a single key or sequence of keys."
  [m ksseq]
  (letfn [(ensure-vector [x]
            (if (sequential? x) x (vector x))) ]
    (->> ksseq
      (map ensure-vector)
      (map (juxt identity (partial get-in m)))
      (reduce (partial apply assoc-in) {}))))

(defn parent
  "Show project properties inherited from parent project

Your project may have a parent project. Specify a parent in your project.clj as
follows.

:parent {:project \"../project.clj\"
         :merge [:dependencies :repositories [:profiles :dev]]}"
  [project & args]
  )
