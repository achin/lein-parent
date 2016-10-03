# lein-parent

A Leiningen plugin for inheriting properties from a parent project.

This plugin is useful when you have a series of related projects that all share
some set of properties and want to avoid duplicating the values across
projects. e.g. you have several projects that all share the same Maven private
repository information.

lein-parent is the conceptual reverse of
[lein-sub](https://github.com/kumarshantanu/lein-sub).

## Usage

Put `[lein-parent "0.3.1"]` into the `:plugins` vector of your project.clj.

Specify a parent in your project.clj and which properties to inherit from it as
follows.

```clj
:parent-project {:path "../project.clj"
                 :inherit [:dependencies :repositories [:profiles :dev]]
                 :only-deps [org.clojure/clojure com.stuartsierra/component]}
```

As of lein-parent 0.3.0 and leiningen 2.7.0, you can also reference a parent
project by its lein/maven coords, like this:

```clj
:parent-project {:coords [org.foo/clojure-parent-project "1.0.0"]
                 :inherit [:managed-dependencies]}
```

Inherited properties may be either keywords or sequences of
keywords. These values are used to select which properties from your
parent to merge into your project. To select only specific
dependencies, specify the first part of the dependency atoms in a
sequence with :only-deps.  To see the actual values of these
properties, run:

    $ lein parent

## Managed Dependencies

Leiningen 2.7.0 introduced support for a `:managed-dependencies` property in
your leiningen project file.  Using this property, you can define version numbers
in the `:managed-dependencies` section without causing the dependencies to be
realized / enforced.  Then, you can omit the version number from the `:dependencies`
section and just specify the dependency group id / artifact id.  This will cause
the dependency to be realized, and the version will be inherited from the
`:managed-dependencies` section.

When combined with `lein-parent`, this allows you to `:inherit` the
`:managed-dependencies` section from a parent project and share the version
numbers for common dependencies across many child projects.  This can be very
powerful in reducing the burden of dealing with transitive dependency version
conflicts across multiple projects.

For example:

```clj
(defproject superfun/myparent "1.0.0"
   :managed-dependencies [[clj-time "0.12.0"]
                            [me.raynes/fs "1.4.6"]
                            [ring/ring-codec "1.0.1"]])

(defproject superfun/kid-a "1.0.0-SNAPSHOT"
   :parent-project [:coords [superfun/myparent "1.0.0"]
                    :inherit [:managed-dependencies]]
   :dependencies [[clj-time]
                  [me.raynes/fs]])

(defproject superfun/kid-b "1.0.0-SNAPSHOT"
 :parent-project [:coords [superfun/myparent "1.0.0"]
                  :inherit [:managed-dependencies]]
 :dependencies [[clj-time]
                [ring/ring-codec]])
```

For more information, see [leiningen's docs on Managed Dependencies](https://github.com/technomancy/leiningen/blob/master/doc/MANAGED_DEPS.md)

## License

Copyright © 2014 Alex Chin

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
