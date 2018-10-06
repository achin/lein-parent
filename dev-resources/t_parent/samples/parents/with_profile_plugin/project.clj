(defproject lein-parent/parent-with-profile-plugin "0.0.1"
  :description "Parent project that provides profile plugins"
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :exclusions [org.clojure/clojure
               org.clojure/clojurescript]
  :profiles {:dev {:plugins [[venantius/ultra "0.5.2"]]}})
