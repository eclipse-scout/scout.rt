/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.scriptprocessor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.ui.html.scriptprocessor.internal.impl.CompileCssWithLess;
import org.eclipse.scout.rt.ui.html.scriptprocessor.internal.impl.MinifyCssWithYui;
import org.eclipse.scout.rt.ui.html.scriptprocessor.internal.impl.MinifyJsWithYui;
import org.eclipse.scout.rt.ui.html.scriptprocessor.internal.loader.SandboxClassLoaderBuilder;

/**
 * Default wrapper for YUI and LESS used to compile and minify javscript and css.
 */
@ApplicationScoped
public class ScriptProcessor {
  private URLClassLoader m_yuiLoader;
  private URLClassLoader m_lessLoader;

  public ScriptProcessor() {
    //set up an external private class loader
    m_yuiLoader = new SandboxClassLoaderBuilder()
        .addLocalJar("private-libs/yuicompressor.jar")
        .addClasses("yui-calls.jar",
            "org.eclipse.scout.rt.ui.html.scriptprocessor.internal.impl.MinifyCssWithYui",
            "org.eclipse.scout.rt.ui.html.scriptprocessor.internal.impl.MinifyJsWithYui",
            "org.eclipse.scout.rt.ui.html.scriptprocessor.internal.impl.MinifyJsWithYui$1")
        .build(null);
    m_lessLoader = new SandboxClassLoaderBuilder()
        // Add commons-logging.jar which logs to java.util.logger because there is no other log implementation available
        // within the sandbox. Using slf4j and Scout's AutoRegisteringJulLevelChangePropagator will propagate log messages
        // to the actual logger and log levels can be defined in one place.
        .addLocalJar("private-libs/commons-logging.jar")
        .addLocalJar("private-libs/rhino.jar")
        .addLocalJar("private-libs/lesscss-engine.jar")
        .addClasses("less-calls.jar",
            "org.eclipse.scout.rt.ui.html.scriptprocessor.internal.impl.CompileCssWithLess",
            "org.eclipse.scout.rt.ui.html.scriptprocessor.internal.impl.ScoutClasspathResourceLoader")
        .build(null);
  }

  public void close() throws IOException {
    if (m_yuiLoader != null) {
      m_yuiLoader.close();
    }
    if (m_lessLoader != null) {
      m_lessLoader.close();
    }
  }

  public String compileCss(String content) throws IOException {
    return runInClassLoader(m_lessLoader, CompileCssWithLess.class.getName(), new Class[]{String.class}, new Object[]{content});
  }

  public String compileJs(String content) throws IOException {
    return content;
  }

  public String minifyCss(String content) throws IOException {
    return runInClassLoader(m_yuiLoader, MinifyCssWithYui.class.getName(), new Class[]{String.class}, new Object[]{content});
  }

  public String minifyJs(String content) throws IOException {
    return runInClassLoader(m_yuiLoader, MinifyJsWithYui.class.getName(), new Class[]{String.class, boolean.class}, new Object[]{content, obfuscateJS()});
  }

  protected boolean obfuscateJS() {
    return true;
  }

  protected String runInClassLoader(ClassLoader loader, String classname, Class<?>[] types, Object[] args) throws IOException {
    try {
      Class<?> c = loader.loadClass(classname);
      Object o = c.newInstance();
      Method m = c.getMethod("run", types);
      Object result = m.invoke(o, args);
      return (String) result;
    }
    catch (InvocationTargetException e0) {
      Throwable t = e0.getTargetException();
      if (t instanceof IOException) {
        throw (IOException) t;
      }
      throw new IOException("Failed running " + classname, e0);
    }
    catch (Exception e1) {
      throw new IOException("Failed running " + classname, e1);
    }
  }
}
