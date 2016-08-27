(defproject child-with-invalid-parent-coords "0.0.1"
  :description "Child project that specifies invalid coords for a parent project"
  :parent-project {:coords [lein-parent/does-not-exist "0.0.1"]
                   :inherit [:foo]})