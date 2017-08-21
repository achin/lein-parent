(defproject lein-parent "0.3.3-SNAPSHOT"
  :description "Leiningen plugin for inheriting properties from a parent project"
  :url "https://github.com/achin/lein-parent"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :dependencies [[com.cemerick/pomegranate "0.3.1"]
                 [org.codehaus.plexus/plexus-utils "3.0"]]
  :plugins [[lein-midje "3.1.3"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}})
