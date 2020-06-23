(defproject child-with-parent-coords-and-pedantic "0.0.1"
  :description "Child project that references parent project via coords"
  :parent-project {:coords [lein-parent/parent-with-profile-plugin "0.0.1"]
                   :inherit [:pedantic?]}
  :pedantic? :abort)
