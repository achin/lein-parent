(defproject child-with-parent-path "0.0.1"
  :description "Child project that references parent project via path"
  :parent-project {:path "../../parents/with_properties/project.clj"
                   :inherit [:foo :pedantic?]})
