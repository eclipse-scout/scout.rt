imo, 26.08.2014
This bundle contains third-party utilities for js and css compiling, minimizing.
Some of the libraries involved require patched versions of javascript and logging.
yuicompressor for example just includes another version of org.mozilla.javascript!

Since we do not want to pollute the normal classpath with these (dirty) libraries,
this project here includes the potentially dirty libraries as normal files, not in
the classpath.

Running minimizing and compile of js, css is then accomplished by creating a separate
classloader with these jars.