# lein-parent

A Leiningen plugin for inheriting properties from a parent project.

This plugin is useful when you have a series of related projects that all share
some set of properties and want to avoid duplicating the values across
projects. e.g. you have several projects that all share the same Maven private
repository information.

lein-parent is the conceptual reverse of
[lein-sub](https://github.com/kumarshantanu/lein-sub).

## Usage

Use this for project-level plugins:

Put `[lein-parent "0.2.0"]` into the `:plugins` vector of your project.clj.

Specify a parent in your project.clj and which properites to inherit from it as
follows.

    :parent-project {:path \"../project.clj\"
                     :inherit [:dependencies :repositories [:profiles :dev]]}

Inherited properites may be either keywords or sequences of keywords. These values
are used to select which properties from your parent to merge into your project.
To see the actual values of these properties, run:

    $ lein parent

## License

Copyright © 2014 Alex Chin

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
