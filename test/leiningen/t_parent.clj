(ns leiningen.t-parent
  (:use [midje.sweet])
  (:require [leiningen.parent :as p]))

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
