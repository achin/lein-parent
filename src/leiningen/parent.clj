(ns leiningen.parent
  (:require [clojure.pprint :as pp]
            [leiningen.core.project :as project]
            [leiningen.core.main :as main]
            [cemerick.pomegranate.aether :as aether])
  (:import (java.util.zip ZipFile)
           (java.io InputStreamReader)))

(defn ensure-sequence
  [x]
  (if (sequential? x) x (vector x)))

(defn select-keys-in
  "Returns a map containing only those entries or sub-entries in m whose key
  path is in ksseq. Similar to select-keys except each value in ksseq is either
  a single key or sequence of keys."
  [m ksseq]
  (->> ksseq
       (map ensure-sequence)
       (map (juxt identity (partial get-in m)))
       (reduce (partial apply assoc-in) {})))

(defn filter-deps
  "props is a map of project properties, deps is a sequence of desired
   dependency names. Returns props with only the depednencies from deps
   included."
  [props deps]
  (if (and deps (:dependencies props))
    (let [deps (-> deps ensure-sequence set)
          filter-fn (fn [d] (contains? deps (first d)))]
      (update-in props [:dependencies] (partial filter filter-fn)))
    props))

(defn is-absolute?
  [path]
  (.isAbsolute (java.io.File. path)))

(defn make-absolute
  [root path]
  (.getAbsolutePath (java.io.File. root path)))

(defn resolve-path
  [root path]
  (if (is-absolute? path)
    path
    (make-absolute root path)))

(defn resolve-project-from-coords
  [coords {:keys [repositories offline?]}]
  (let [resolved-parent-artifact (first (first (aether/resolve-dependencies
                                                 :coordinates [coords]
                                                 :repositories repositories
                                                 :offline? offline?)))
        artifact-jar (:file (meta resolved-parent-artifact))
        artifact-zip (ZipFile. artifact-jar)
        project-clj-path (format "META-INF/leiningen/%s/project.clj" (first coords))]
    (project/init-project (project/read (InputStreamReader. (.getInputStream
                                          artifact-zip
                                          (.getEntry artifact-zip project-clj-path)))))))

(defn get-parent-project
  [project {:keys [path coords]}]
  (cond
    coords
    (resolve-project-from-coords
      coords
      project)

    path
    (let [path (resolve-path (:root project) path)]
      (project/init-project (project/read path)))

    :else
    (main/warn "WARNING: :parent-project does not specify :coords or :path, so no parent project will be loaded.")))

(defn parent-properties
  [proj ks]
  (select-keys-in proj ks))

(defn inherited-properties
  [project]
  (when-let [parent-project (:parent-project project)]
    (let [{:keys [inherit only-deps]} parent-project]
      (-> (get-parent-project project parent-project)
          (parent-properties inherit)
          (filter-deps only-deps)))))

(defn parent
  "Show project properties inherited from parent project

Your project may have a parent project. Specify a parent in your project.clj as
follows.

:parent-project {:path \"../project.clj\"
                 :inherit [:dependencies :repositories [:profiles :dev]]
                 :only-deps [org.clojure/tools-logging com.example/whatever}"
  [project & args]
  (if-let [inherited (inherited-properties project)]
    (do (printf "Inheriting properties %s from %s\n\n"
                (get-in project [:parent-project :inherit])
                (get-in project [:parent-project :coords]
                        (get-in project [:parent-project :path])))
        (pp/pprint inherited))
    (println "No parent project specified")))
