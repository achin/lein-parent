(ns lein-parent.plugin
  (:require [leiningen.parent :as parent]
            [leiningen.core.project :as project]))

(def meta-merge #'project/meta-merge)

(defn middleware [project]
  (if-let [parent-project (:parent-project project)]
    (let [{:keys [path inherit]} parent-project]
      (meta-merge project (parent/parent-properties path inherit)))
    project))
