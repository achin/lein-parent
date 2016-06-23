(defproject child-with-parent-path "0.0.1"
  :description "Child project that references parent project via coords"
  :parent-project {:coords [lein-parent/parent-with-foo-property "0.0.1"]
                   :inherit [:foo]})