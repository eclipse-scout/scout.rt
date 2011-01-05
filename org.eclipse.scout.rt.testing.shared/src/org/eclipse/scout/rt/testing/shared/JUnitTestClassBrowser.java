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

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.osgi.BundleInspector;
import org.eclipse.scout.commons.runtime.BundleBrowser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;

/**
 * Collects JUnit Test classes using the following rules:
 * <ol>
 * <li>A JUnit test is expected to be the outer most class in a java file (i.e. inner classes are not scanned)</li>
 * <li>JUnit test classes must contain <em>Test</em> in their class names</li>
 * <li>JUnit test classes must have a class-level annotation <code>@RunWith</code> or at least one method that is
 * annotated with <code>@Test</code></li>
 * </ol>
 */
public class JUnitTestClassBrowser {

  @SuppressWarnings("restriction")
  public List<Class<?>> collectAllJUnitTestClasses() {
    boolean dev = Platform.inDevelopmentMode();
    if (dev) {
      System.out.println("In -dev mode: only the test(s) marked with @DevTestMarker are run as a convenience (all tests if no such annotation is found)");
    }
    List<Class<?>> junitTestClasses = new ArrayList<Class<?>>();
    List<Class<?>> devClasses = new ArrayList<Class<?>>();
    for (Bundle bundle : Activator.getDefault().getBundle().getBundleContext().getBundles()) {
      // exclude fragments as their content is searched by their host bundles.
      if (bundle instanceof org.eclipse.osgi.framework.internal.core.BundleFragment) {
        continue;
      }

      String[] classNames;
      try {
        BundleBrowser bundleBrowser = new BundleBrowser(bundle.getSymbolicName(), bundle.getSymbolicName());
        classNames = bundleBrowser.getClasses(false, true);
      }
      catch (Exception e1) {
        System.err.println(e1);
        continue;
      }
      // filter
      for (String className : classNames) {
        // fast pre-check
        if (className.indexOf("Test") >= 0) {
          try {
            Class<?> c = BundleInspector.getHostBundle(bundle).loadClass(className);
            if ((c.getModifiers() & Modifier.ABSTRACT) == 0) {
              if (c.getAnnotation(RunWith.class) != null) {
                //add it
                if (dev && c.getAnnotation(DevTestMarker.class) != null) {
                  devClasses.add(c);
                }
                junitTestClasses.add(c);
              }
              else {
                Method[] methods = c.getMethods();
                if (methods != null) {
                  for (Method m : methods) {
                    if (m.getAnnotation(Test.class) != null) {
                      //add it
                      if (dev && c.getAnnotation(DevTestMarker.class) != null) {
                        devClasses.add(c);
                      }
                      junitTestClasses.add(c);
                      break;
                    }
                  }
                }
              }
            }
          }
          catch (Throwable t) {
            // nop
          }
        }
      }
    }
    if (devClasses.size() > 0) {
      return devClasses;
    }
    else {
      return junitTestClasses;
    }
  }
}
