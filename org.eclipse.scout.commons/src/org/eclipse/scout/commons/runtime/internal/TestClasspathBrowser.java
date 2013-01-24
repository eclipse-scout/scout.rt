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
package org.eclipse.scout.commons.runtime.internal;

import java.lang.reflect.Modifier;

import org.eclipse.scout.commons.runtime.ClasspathBrowser;

public final class TestClasspathBrowser {

  private TestClasspathBrowser() {
  }

  public static void main(String[] args) throws Exception {
    ClasspathBrowser b = new ClasspathBrowser();
    b.addDefaultClasspaths();
    b.addClasspathsByClassLoader(TestClasspathBrowser.class);
    b.visit();
    //
    /*
     * System.out.println("# Classes"); for(String s: b.getClasses()){
     * System.out.println(s); } // System.out.println("# Resources"); for(String
     * s: b.getResources()){ System.out.println(s); }
     */
    // filter
    System.out.println("# Service Implementations");
    for (String className : b.getClasses()) {
      try {
        Class c = Class.forName(className, true, TestClasspathBrowser.class.getClassLoader());
        if (!c.isInterface()) {
          int flags = c.getModifiers();
          if (Modifier.isPublic(flags) && (!Modifier.isAbstract(flags)) && (!c.getSimpleName().startsWith("Abstract"))) {
            System.out.println(c);
          }
        }
      }
      catch (Throwable t) {
        System.out.println("Failed :" + className + ": " + t);
      }
    }
  }

}
