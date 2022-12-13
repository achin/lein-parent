(ns leiningen.t-parent
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer :all]
            [lein-parent.plugin :as plugin]
            leiningen.core.main
            [leiningen.core.project :as project]
            [leiningen.install :as install]
            [leiningen.parent :as p]
            [midje.sweet :refer :all])
  (:import java.io.FileNotFoundException))

(def m {:a 1
        :b 2
        :c 3
        :d {4 :red
            5 :green
            6 :blue}
        :e {7 {:white :apple
               :black :orange}}})

(fact "about select-keys-in"
  (p/select-keys-in m [:a])          => (just {:a 1})
  (p/select-keys-in m [:a :b :c])    => (just {:a 1
                                               :b 2
                                               :c 3})
  (p/select-keys-in m [:d])          => (just {:d {4 :red
                                                   5 :green
                                                   6 :blue}})
  (p/select-keys-in m [[:d 4]])      => (just {:d {4 :red}})
  (p/select-keys-in m [[:d 4] [:e]]) => (just {:d {4 :red}
                                               :e {7 {:white :apple
                                                      :black :orange}}})
  (p/select-keys-in m [:a [:d 4]])   => (just {:a 1
                                               :d {4 :red}}))

;; Test helpers

(defn delete-files-recursively
  [f1]
  (when (.isDirectory (io/file f1))
    (doseq [f2 (.listFiles (io/file f1))]
      (delete-files-recursively f2)))
  (io/delete-file f1))

(def test-ground-path "test/test_ground/")

(defn delete-test-ground
  []
  (when (.exists (io/file test-ground-path))
    (delete-files-recursively test-ground-path)))

(defn spit-project
  "Generates a 'project.clj' file that can be read by Leiningen."
  [project-name project-map]
  (let [version "0.0.1"
        full-name (symbol (str (symbol (str *ns*) project-name)))
        coords [full-name version]
        filename (-> (str/replace project-name #"-" "_"))
        filepath (io/file test-ground-path filename "project.clj")]
    (io/make-parents filepath)
    (->> (reduce into project-map)
         (into ['defproject full-name version])
         (seq)
         (spit filepath))
    {:filename filename
     :filepath filepath
     :coords coords}))

(defn with-install
  "Installs project such that it can be used with :coords"
  [spat]
  (binding [leiningen.core.main/*info* false]
    (install/install (project/read (str (:filepath spat))))
    spat))

(defn read-project
  [spat]
  (-> (:filepath spat)
      str
      project/read
      plugin/middleware))

(defn relative
  "Shorthand to obtain the relative path to the project."
  [spat]
  (str "../" (:filename spat) "/project.clj"))


;; Start tests

(deftest parent-loading
  (testing "Error thrown if non-existent path provided"
    (delete-test-ground)
    (try
      (let [parent (spit-project "parent" {:foo "foo"})
            child-project (->> {:parent-project {:path    (str (relative parent) "-nonexistent")}}
                               (spit-project "child")
                               (read-project))])
      ;; should not get here!
      (is (true? false) "Exception should have been thrown by call to 'read-project'!")
      (catch Exception e
        (is (instance? FileNotFoundException (.getCause e))))))


  (testing "Error thrown if non-existent coords provided"
    (delete-test-ground)
    (is (thrown-with-msg? Exception #"Could not find artifact lein-parent:does-not-exist"
                          (let [parent (spit-project "parent" {:foo "foo"})
                                child-project (->> {:parent-project {:coords  '[lein-parent/does-not-exist "0.0.1"]}}
                                                   (spit-project "child")
                                                   (read-project))]))))

  (testing "parent can be loaded by path"
    (delete-test-ground)
    (let [parent (spit-project "parent" {:foo "foo"})
          child-project (->> {:parent-project {:path    (relative parent)
                                               :inherit [:foo]}}
                             (spit-project "child")
                             (read-project))]
      (is (= "foo" (:foo child-project)))))

  (testing "parent can be loaded by coordinates"
    (delete-test-ground)
    (let [parent (-> (spit-project "parent" {:foo "foo"})
                     (with-install))
          child-project (->> {:parent-project {:coords  (:coords parent)
                                               :inherit [:foo]}}
                             (spit-project "child")
                             (read-project))]
      (is (= "foo" (:foo child-project))))))

(deftest parent-profiles
  (delete-test-ground)
  (let [parent (spit-project "parent" {:profiles {:foo {:bar "bar"}}})
        child-project (->> {:parent-project {:path    (relative parent)
                                             :inherit [[:profiles]]}}
                           (spit-project "child")
                           (read-project))]
    (testing "properties are merged when the profile is activated"
      ;; with-profiles calls the set-profiles function to 'activate' selected profiles
      (is (= "bar" (:bar (project/set-profiles child-project [:foo])))))

    (testing "when the profile is not activated, the profile is still available in the project"
      (is (nil? (:bar child-project)))
      (is (= "bar" (get-in child-project [:profiles :foo :bar]))))))

(deftest inherited-properties
  (testing "properties inherited can be ALL"
    (delete-test-ground)
    (let [parent-properties {:profiles  {:foo {:bar "bar"}}
                             :prop1     "value1"
                             :prop2     "value2"
                             :pedantic? :abort}
          parent (spit-project "parent" parent-properties)
          child-project (->> {:parent-project {:path    (relative parent)
                                               :inherit [:leiningen.parent/all]}}
                             (spit-project "child")
                             (read-project))]
      (is (every? #(= (get child-project %)
                      (get parent-properties %)) (keys parent-properties)))))

  (testing "managed_dependencies can be inherited from parent"
    (delete-test-ground)
    (let [parent (spit-project "parent" {:managed-dependencies '[[clj-time "0.5.1"]
                                                                 [ring/ring-codec "1.0.1"]]})
          child-project (->> {:parent-project {:path    (relative parent)
                                               :inherit [:managed-dependencies]}}
                             (spit-project "child")
                             (read-project))]
      (is (= '[[clj-time "0.5.1"] [ring/ring-codec "1.0.1"]]
             (:managed-dependencies child-project)))))

  (testing "child has higher priority in merges"
    (delete-test-ground)
    (let [parent (spit-project "parent" {:aliases {"my-alias"   ["parent"]
                                                   "my-alias-2" ["parent"]}})
          child-project (->> {:parent-project {:path    (relative parent)
                                               :inherit [:aliases]}
                              :aliases        {"my-alias" ["child"]}}
                             (spit-project "child")
                             (read-project))]
      (is (= {"my-alias"   ["child"]
              "my-alias-2" ["parent"]}
             (select-keys (:aliases child-project) ["my-alias" "my-alias-2"])))))

  (testing "plugins are merged"
    (delete-test-ground)
    (let [parent (spit-project "parent" {:plugins '[[venantius/ultra "0.5.2"]]})
          child-project (->> {:parent-project {:path    (relative parent)
                                               :inherit [:plugins]}
                              :plugins        '[[lein-pprint/lein-pprint "1.3.2"]]}
                             (spit-project "child")
                             (read-project))]
      (is (some #{'[venantius/ultra "0.5.2"]} (:plugins child-project)))
      (is (some #{'[lein-pprint/lein-pprint "1.3.2"]} (:plugins child-project)))))

  (testing "handle lein defaults"
    (testing "with value inherited from parent"
      (delete-test-ground)
      (let [parent (spit-project "parent" {:pedantic? :parent})
            child-project (->> {:parent-project {:path    (relative parent)
                                                 :inherit [:pedantic?]}}
                               (spit-project "child")
                               (read-project))]
        (is (= :parent (:pedantic? child-project)))))

    (testing "with value provided by child"
      (delete-test-ground)
      (let [parent (spit-project "parent" {:pedantic? :parent})
            child-project (->> {:parent-project {:path    (relative parent)
                                                 :inherit [:pedantic?]}
                                :pedantic?      :child}
                               (spit-project "child")
                               (read-project))]
        (is (= :child (:pedantic? child-project)))))

    (testing "without inheritance"
      (delete-test-ground)
      (let [parent (spit-project "parent" {})
            child-project (->> {:parent-project {:path    (relative parent)
                                                 :inherit []}}
                               (spit-project "child")
                               (read-project))]
        (is (= (:pedantic? project/defaults) (:pedantic? child-project)))))))
