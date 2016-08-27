(defproject child-with-invalid-parent-path "0.0.1"
  :description "Child project that specifies an invalid path for a parent project"
  :parent-project {:path "../../parents/does_not_exist/project.clj"
                   :inherit [:foo]})