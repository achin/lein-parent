(defproject child-with-parent-with-aliases "0.0.1"
  :description "Child project that references parent project with aliases"
  :parent-project {:path    "../../parents/with_aliases/project.clj"
                   :inherit [:aliases]}
  :aliases {"my-alias" ["child"]})
