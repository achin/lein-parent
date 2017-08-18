(ns lein-parent.plugin
  (:require [leiningen.parent :as parent]
            [leiningen.core.project :as project]))

(def meta-merge #'project/meta-merge)

(defn middleware [project]
  (if-let [inherited (parent/inherited-properties project)]
    (with-meta (meta-merge project inherited)
               (update (meta project) :profiles merge (:profiles inherited)))
    project))
