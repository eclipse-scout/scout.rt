package org.eclipse.scout.rt.rest.doc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom name for method arguments. Used by {@link ApiDocGenerator} when generating method signatures. Because
 * parameter names are not always accessible via reflection, this annotation allows to assign human-readable names to
 * individual parameters.
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiDocParam {

  String value();
}
