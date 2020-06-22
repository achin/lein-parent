(defproject child-with-parent-with-properties "0.0.1"
  :description "Child project that references parent project with properties"
  :parent-project {:path    "../../parents/with_properties/project.clj"
                   :inherit [:leiningen.parent/all]})
