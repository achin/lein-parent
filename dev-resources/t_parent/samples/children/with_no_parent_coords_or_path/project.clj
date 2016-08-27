(defproject child-with-no-parent-coords-or-path "0.0.1"
  :description "Child project that doesn't specifies path or coords for parent"
  :parent-project {:inherit [:foo]})