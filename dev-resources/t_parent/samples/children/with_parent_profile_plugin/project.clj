(defproject child-with-parent-profile-plugin "0.0.1"
  :description "Child project that references parent project with profile plugins"
  :dependencies [[medley "1.0.0"]]
  :parent-project {:coords  [lein-parent/parent-with-profile-plugin "0.0.1"]
                   :inherit [:dependencies [:profiles :dev]]})
