(ns leiningen.t-parent
  (:use [midje.sweet])
  (:require [leiningen.parent :as p]
            [lein-parent.plugin :as plugin]
            [clojure.test :refer :all]
            [leiningen.core.project :as project]
            [leiningen.install :as install])
  (:import (java.io FileNotFoundException)
           (org.sonatype.aether.resolution ArtifactResolutionException)))

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


(defn test-proj-path
  [kind proj-name]
  (str "./dev-resources/t_parent/samples/" kind "/" proj-name "/project.clj"))

(defn child-path
  [proj-name]
  (test-proj-path "children" proj-name))

(defn parent-path
  [proj-name]
  (test-proj-path "parents" proj-name))

(defn read-child-project
  [proj-name]
  (-> proj-name
      child-path
      project/read
      plugin/middleware))

(deftest parent-project-specification-test
  (testing "Error thrown if neither path nor coords provided"
    (is (thrown-with-msg? IllegalArgumentException #"must include either 'coords' or 'path'"
          (read-child-project "with_no_parent_coords_or_path"))))
  (testing "parent projects can be loaded by path"
    (let [project (read-child-project "with_parent_path")]
      (is (= "foo" (:foo project)))))
  (testing "Error thrown if non-existent path provided"
    (try
      (read-child-project "with_invalid_parent_path")
      ;; should not get here!
      (is (true? false) "Exception should have been thrown by call to 'read-project'!")
      (catch Exception e
        (is (instance? FileNotFoundException (.getCause e))))))
  (testing "parent projects can be loaded by coordinates"
    (install/install (project/read (parent-path "with_foo_property")))
    (let [project (read-child-project "with_parent_coords")]
      (is (= "foo" (:foo project)))))
  (testing "Error thrown if non-existent coords provided"
    (is (thrown? ArtifactResolutionException
          (read-child-project "with_invalid_parent_coords")))))

(deftest inherited-values-test
  (testing "managed_dependencies can be inherited from parent"
    (let [project (read-child-project "with_parent_with_managed_deps")]
      (is (= [['clj-time "0.5.1"] ['ring/ring-codec "1.0.1"]]
             (:managed-dependencies project))))))
