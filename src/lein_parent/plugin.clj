(ns lein-parent.plugin
  (:require [leiningen.parent :as parent]
            [leiningen.core.project :as project]))

(def meta-merge #'project/meta-merge)

(defn middleware [project]
  (if-let [inherited (parent/inherited-properties project)]
    (let [project (parent/handle-lein-defaults project)]
      (with-meta (meta-merge inherited project)
        (update (meta project) :profiles #(merge (:profiles inherited) %))))
    project))