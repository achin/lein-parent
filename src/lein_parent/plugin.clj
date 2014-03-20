(ns lein-parent.plugin
  (:require [leiningen.parent :as parent]
            [leiningen.core.project :as project]))

(def meta-merge #'project/meta-merge)

(defn middleware [project]
  (let [{parent-proj-path :project inherit :inherit} (:parent project)]
    (meta-merge project (parent/parent-properties parent-proj-path inherit))))
