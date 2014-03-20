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
  [path inherit]
  (let [parent-proj (project/init-project (project/read path))]
    (select-keys-in parent-proj inherit)))

(defn parent
  "Show project properties inherited from parent project

Your project may have a parent project. Specify a parent in your project.clj as
follows.

:parent-project {:path \"../project.clj\"
                 :inherit [:dependencies :repositories [:profiles :dev]]}"
  [project & args]
  (if-let [parent-project (:parent-project project)]
    (let [{:keys [path inherit]} parent-project]
      (printf "Inheriting properties %s from %s\n\n" inherit path)
      (pp/pprint (parent-properties path inherit)))
    (println "No parent project specified")))
