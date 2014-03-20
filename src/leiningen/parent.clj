(ns leiningen.parent
  (:require [clojure.pprint :as pp]
            [leiningen.core.project :as project]))

(defn select-keys-in
  "Returns a map containing only those entries or sub-entries in m whose key
  path is in ksseq. Similar to select-keys except each value in ksseq is either
  a single key or sequence of keys."
  [m ksseq]
  (letfn [(ensure-sequence [x]
            (if (sequential? x) x (vector x))) ]
    (->> ksseq
      (map ensure-sequence)
      (map (juxt identity (partial get-in m)))
      (reduce (partial apply assoc-in) {}))))

(defn parent-properties
  [parent-proj-path inherit]
  (let [parent-proj (project/init-project (project/read parent-proj-path))]
    (select-keys-in parent-proj inherit)))

(defn parent
  "Show project properties inherited from parent project

Your project may have a parent project. Specify a parent in your project.clj as
follows.

:parent {:project \"../project.clj\"
         :inherit [:dependencies :repositories [:profiles :dev]]}"
  [project & args]
  (let [{parent-proj-path :project inherit :inherit} (:parent project)]
    (printf "Inheriting properties %s from %s\n\n" inherit parent-proj-path)
    (pp/pprint (parent-properties parent-proj-path inherit))))
