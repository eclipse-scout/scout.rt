Due to classloading issues when using SVG in org.apache.batik 1.7 that is described in bug [1], the bundle
org.w3c.dom.svg (version 1.1.0) should have a dependency to the bundle org.w3c.dom.events (version 3.0.0).
Without the dependency the classes in the package "org.w3c.dom.events" are loaded from the JRE.
Batik 1.7 however requires the classes to be loaded from package located inside the bundle
org.w3c.dom.events (version 3.0.0). As a consequence a LinkageError appears.

This fragment to the bundle org.w3c.dom.svg is a workaround. It imports the package org.w3c.dom.events version [3.0.0,4.0.0)
and creates a dependency to the bundle org.w3c.dom.events. As a result, the bundle classloader of org.w3c.dom.svg is able
to load the classes from the correct location.

This fragment becomes unnecessary when the bug [1] gets fixed.

[1] https://bugs.eclipse.org/bugs/show_bug.cgi?id=421553
