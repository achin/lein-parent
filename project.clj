(defproject lein-parent "0.2.2-SNAPSHOT"
  :description "Leiningen plugin for inheriting properties from a parent project"
  :url "https://github.com/achin/lein-parent"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :plugins [[lein-midje "3.1.3"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}})
