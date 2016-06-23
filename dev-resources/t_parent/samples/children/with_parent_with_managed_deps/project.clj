(defproject child-with-parent-with-managed-deps "0.0.1"
  :description "Child project that references parent project with managed dependencies"
  :parent-project {:path "../../parents/with_managed_deps/project.clj"
                   :inherit [:managed-dependencies]})