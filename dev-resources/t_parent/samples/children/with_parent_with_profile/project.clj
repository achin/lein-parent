(defproject child-with-parent-with-profiles "0.0.1"
  :description "Child project that references parent project with a profile"
  :parent-project {:path "../../parents/with_profiles/project.clj"
                   :inherit [[:profiles :foo]]})