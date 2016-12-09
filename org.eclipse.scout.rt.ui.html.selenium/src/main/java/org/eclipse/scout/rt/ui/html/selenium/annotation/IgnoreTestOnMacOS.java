package org.eclipse.scout.rt.ui.html.selenium.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.scout.rt.ui.html.selenium.util.SeleniumUtil;

/**
 * Marker for unit tests that should not be executed if {@link SeleniumUtil#isMacOS()} returns <code>true</code>.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface IgnoreTestOnMacOS {
}
