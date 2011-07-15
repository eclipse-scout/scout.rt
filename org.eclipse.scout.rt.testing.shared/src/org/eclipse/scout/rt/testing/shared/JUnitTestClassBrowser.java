/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.shared;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.osgi.BundleInspector;
import org.eclipse.scout.commons.runtime.BundleBrowser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;

/**
 * Collects JUnit Test classes. By default the the following rules are used:
 * <ol>
 * <li>A JUnit test is expected to be the outer most class in a java file (i.e. inner classes are not scanned)</li>
 * <li>By default all bundles scanned for tests (can be changed by specifying inclusion and exclusion regular expression
 * patterns or by overriding {@link #acceptBundle(Bundle)})</li>
 * <li>By default JUnit test classes must contain <em>Test</em> in their class names (can be changed by specifying
 * inclusion and exclusion regular expression patterns or by overriding {@link #acceptClassName(Bundle, String)}).</li>
 * <li>JUnit test classes must have a class-level annotation <code>@RunWith</code> or at least one method that is
 * annotated with <code>@Test</code> (can be changed by overriding {@link #acceptClass(Bundle, Class)})</li>
 * </ol>
 * <p>
 */
public class JUnitTestClassBrowser {

  private static final String REGEX_MARKER = "regex:";
  private static final Pattern[] DEFAULT_CLASS_NAME_INCLUDE_PATTERNS = new Pattern[]{Pattern.compile(".*Test.*")};

  private Pattern[] m_bundleNameIncludePatterns;
  private Pattern[] m_bundleNameExcludePatterns;
  private Pattern[] m_classNameIncludePatterns;
  private Pattern[] m_classNameExcludePatterns;

  public JUnitTestClassBrowser() {
    setClassNameIncludePatterns(DEFAULT_CLASS_NAME_INCLUDE_PATTERNS);
  }

  public Pattern[] getBundleNameIncludePatterns() {
    return m_bundleNameIncludePatterns;
  }

  public void setBundleNameIncludePatterns(Pattern[] bundleNameIncludePatterns) {
    m_bundleNameIncludePatterns = bundleNameIncludePatterns;
  }

  public Pattern[] getBundleNameExcludePatterns() {
    return m_bundleNameExcludePatterns;
  }

  public void setBundleNameExcludePatterns(Pattern[] bundleNameExcludePatterns) {
    m_bundleNameExcludePatterns = bundleNameExcludePatterns;
  }

  public Pattern[] getClassNameIncludePatterns() {
    return m_classNameIncludePatterns;
  }

  public void setClassNameIncludePatterns(Pattern[] classNameIncludePatterns) {
    if (classNameIncludePatterns == null || classNameIncludePatterns.length == 0) {
      m_classNameIncludePatterns = DEFAULT_CLASS_NAME_INCLUDE_PATTERNS;
    }
    else {
      m_classNameIncludePatterns = classNameIncludePatterns;
    }
  }

  public Pattern[] getClassNameExcludePatterns() {
    return m_classNameExcludePatterns;
  }

  public void setClassNameExcludePatterns(Pattern[] classNameExcludePatterns) {
    m_classNameExcludePatterns = classNameExcludePatterns;
  }

  /**
   * @return Returns a list of all classes accepted by the following three filter methods:
   *         <ol>
   *         <li>{@link #acceptBundle(Bundle)}</li>
   *         <li>{@link #acceptClassName(Bundle, String)}</li>
   *         <li>{@link #acceptClass(Bundle, Class)}</li>
   *         </ol>
   */
  @SuppressWarnings("restriction")
  public List<Class<?>> collectAllJUnitTestClasses() {
    boolean dev = Platform.inDevelopmentMode();
    if (dev) {
      System.out.println("In -dev mode: only the test(s) marked with @DevTestMarker are run as a convenience (all tests if no such annotation is found)");
    }
    List<Class<?>> junitTestClasses = new ArrayList<Class<?>>();
    List<Class<?>> devClasses = new ArrayList<Class<?>>();
    for (Bundle bundle : Activator.getDefault().getBundle().getBundleContext().getBundles()) {
      // check if bundle is searched for tests
      if (!acceptBundle(bundle)) {
        continue;
      }

      String[] classNames;
      try {
        BundleBrowser bundleBrowser = new BundleBrowser(bundle.getSymbolicName(), "");
        classNames = bundleBrowser.getClasses(false, true);
      }
      catch (Exception e1) {
        System.err.println(e1);
        continue;
      }
      // filter
      for (String className : classNames) {
        // fast pre-check
        if (!acceptClassName(bundle, className)) {
          continue;
        }
        try {
          Class<?> c = BundleInspector.getHostBundle(bundle).loadClass(className);
          if (acceptClass(bundle, c)) {
            // add it
            if (dev && c.getAnnotation(DevTestMarker.class) != null) {
              devClasses.add(c);
            }
            junitTestClasses.add(c);
          }
        }
        catch (Throwable t) {
          // nop
        }
      }
    }
    if (!devClasses.isEmpty()) {
      return devClasses;
    }
    return junitTestClasses;
  }

  /**
   * The default implementation checks that the given bundle's name is included in the list of bundle name include
   * patterns and that its name is not excluded by the bundle name exclude patterns.
   * 
   * @return Returns <code>true</code> if the given bundle meets the requirements to be scanned for JUnit tests.
   *         <code>false</code> otherwise.
   * @see #setBundleNameIncludePatterns(Pattern[])
   * @see #setBundleNameExcludePatterns(Pattern[])
   */
  protected boolean acceptBundle(Bundle bundle) {
    return !Platform.isFragment(bundle)
        && accept(bundle.getSymbolicName(), getBundleNameIncludePatterns(), getBundleNameExcludePatterns());
  }

  /**
   * The default implementation checks that the given class's name is included in the list of class name include
   * patterns and that its name is not excluded by the class name exclude patterns.
   * 
   * @param bundle
   *          The class's hosting bundle
   * @param className
   * @return Returns <code>true</code> if the given class name meets the requirements to be scanned for JUnit tests.
   *         <code>false</code> otherwise.
   * @see #setClassNameIncludePatterns(Pattern[])
   * @see #setClassNameExcludePatterns(Pattern[])
   */
  protected boolean acceptClassName(Bundle bundle, String className) {
    return accept(className, getClassNameIncludePatterns(), getClassNameExcludePatterns());
  }

  /**
   * This default implementation checks that the given class is not abstract and that it has either a {@link RunWith}
   * class-level annotation or at least one of its methods is annotated with {@link Test}.
   * 
   * @param bundle
   *          The class's hosting bundle
   * @param c
   * @return Returns <code>true</code> if the class hosts a JUnit test methods. <code>false</code> otherwise.
   */
  protected boolean acceptClass(Bundle bundle, Class<?> c) {
    if ((c.getModifiers() & Modifier.ABSTRACT) != 0) {
      return false;
    }

    if (junit.framework.Test.class.isAssignableFrom(c)) {
      // JUnit 3 support
      return true;
    }
    if (c.getAnnotation(RunWith.class) != null) {
      // class-level check is ok
      return true;
    }
    else {
      // look for annotated Test methods
      Method[] methods = c.getMethods();
      if (methods != null) {
        for (Method m : methods) {
          if (m.getAnnotation(Test.class) != null) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Checks if the given string is included in the list of include patterns and that it is not excluded by the list of
   * exclude patterns. If the include or exclude pattern list is null or empty, the string is assumed to be included and
   * not excluded, respectively.
   * 
   * @param s
   * @param includePatterns
   * @param excludePatterns
   * @return
   */
  public static boolean accept(String s, Pattern[] includePatterns, Pattern[] excludePatterns) {
    if (s == null) {
      return false;
    }
    boolean included = true;
    boolean excluded = false;
    if (includePatterns != null) {
      included = false;
      for (Pattern p : includePatterns) {
        if (p.matcher(s).matches()) {
          included = true;
          break;
        }
      }
    }
    if (included && excludePatterns != null) {
      for (Pattern p : excludePatterns) {
        if (p.matcher(s).matches()) {
          excluded = true;
          break;
        }
      }
    }
    return included && !excluded;
  }

  /**
   * Parses a comma-separated list of filter patterns. A filter pattern is either a wildcard pattern or a regular
   * expression. Latter must be prefixed by <em>regex:</em>
   * 
   * @param filter
   * @return
   */
  public static Pattern[] parseFilterPatterns(String filter) {
    if (filter == null) {
      return null;
    }
    List<Pattern> patterns = new ArrayList<Pattern>();
    for (String f : filter.split(",")) {
      f = f.trim();
      if (f.length() > 0) {
        try {
          f = toRegexPattern(f);
          Pattern pattern = Pattern.compile(f);
          patterns.add(pattern);
        }
        catch (Exception e) {
          System.err.println("invalid bundle filter pattern: " + e);
        }
      }
    }
    if (patterns.isEmpty()) {
      return null;
    }
    return patterns.toArray(new Pattern[patterns.size()]);
  }

  /**
   * Transforms the given string into a regular expression pattern. The string is assumed to be a wildcard pattern or
   * already a regular expression pattern. The latter must be prefixed by <em>regex:</em>.
   * 
   * @param s
   * @return
   */
  public static String toRegexPattern(String s) {
    if (s == null) {
      return null;
    }
    String pattern = s.trim();
    if (pattern.startsWith(REGEX_MARKER)) {
      return pattern.substring(REGEX_MARKER.length());
    }
    pattern = pattern.replaceAll("[.]", "\\\\.");
    pattern = pattern.replaceAll("[*]", ".*");
    pattern = pattern.replaceAll("[?]", ".");
    return pattern;
  }
}
