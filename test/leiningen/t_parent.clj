(ns leiningen.t-parent
  (:use [midje.sweet])
  (:require [leiningen.parent :as p]
            [lein-parent.plugin :as plugin]
            [clojure.test :refer :all]
            [leiningen.core.project :as project]
            [leiningen.install :as install])
  (:import (java.io FileNotFoundException)))

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
    (install/install (project/read (parent-path "with_properties")))
    (let [project (read-child-project "with_parent_coords")]
      (is (= "foo" (:foo project)))))
  (testing "Error thrown if non-existent coords provided"
    (is (thrown-with-msg? Exception #"Could not find artifact lein-parent:does-not-exist"
                         (read-child-project "with_invalid_parent_coords"))))
  (testing "parent projects can be loaded by coordinates and contain dev plugins"
    (install/install (project/read (parent-path "with_profile_plugin")))
    (let [project (read-child-project "with_parent_profile_plugin")]
      (is (= [['venantius/ultra "0.5.2"]] (get-in project [:profiles :dev :plugins]))))))

(deftest inherited-values-test
  (testing "managed_dependencies can be inherited from parent"
    (let [project (read-child-project "with_parent_with_managed_deps")]
      (is (= [['clj-time "0.5.1"] ['ring/ring-codec "1.0.1"]]
             (:managed-dependencies project)))))

  (testing "profiles can be inherited from parent"
    (testing "when the profile is activated"
      ;; with-profiles calls the set-profiles function to 'activate' selected profiles
      (let [project (project/set-profiles (read-child-project "with_parent_with_profile") [:foo])]
        (is (= "bar" (:bar project)))))
    (testing "when the profile is not activated, the profile is still available in the project"
      ;; with-profiles calls the set-profiles function to 'activate' selected profiles
      (let [project (read-child-project "with_parent_with_profile")]
        (is (nil? (:bar project)))
        (is (= "bar" (get-in project [:profiles :foo :bar]))))))

  (testing "all properties can be inherited from parent"
    (let [project (read-child-project "with_parent_inherit_all")]
      (is (= {:foo "foo" :bar "bar"}
             (select-keys project [:foo :bar])))))

  (testing "child has higher priority in merges"
    (let [project (read-child-project "with_parent_with_aliases")]
      (is (= {"my-alias"   ["child"]
              "my-alias-2" ["parent"]}
             (select-keys (:aliases project) ["my-alias" "my-alias-2"])))))

  (testing "parent has higher priority than lein default values"
    ; Value inherited from parent
    (let [project (read-child-project "with_parent_path")]
      (is (= :abort (:pedantic? project))))
    ; No inheritance, should be provided as a Lein default
    (let [project (read-child-project "with_parent_coords")]
      (is (= (:pedantic? project/defaults) (:pedantic? project))))
    (let [project (read-child-project "with_parent_coords_and_pedantic")]
      ; Not provided by parent, but provided by child
      (is (= :abort (:pedantic? project))))))