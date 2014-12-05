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
package org.eclipse.scout.rt.ui.html.thirdparty;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.scout.rt.ui.html.thirdparty.internal.impl.CompileCssWithLess;
import org.eclipse.scout.rt.ui.html.thirdparty.internal.impl.MinifyCssWithYui;
import org.eclipse.scout.rt.ui.html.thirdparty.internal.impl.MinifyJsWithYui;
import org.eclipse.scout.rt.ui.html.thirdparty.internal.loader.SandboxClassLoaderBuilder;

public class ThirdPartyScriptProcessorImpl {
  private ClassLoader m_yuiLoader;
  private ClassLoader m_lessLoader;

  public ThirdPartyScriptProcessorImpl() {
    //set up an external private class loader
    m_yuiLoader = new SandboxClassLoaderBuilder().
        addLocalJar("private-libs/yuicompressor.jar").
        addJarContaining(ThirdPartyScriptProcessorImpl.class).
        build(null);
    m_lessLoader = new SandboxClassLoaderBuilder().
        addLocalJar("private-libs/commons-cli.jar").
        addLocalJar("private-libs/commons-logging.jar").
        addLocalJar("private-libs/rhino.jar").
        addLocalJar("private-libs/lesscss-engine.jar").
        addJarContaining(ThirdPartyScriptProcessorImpl.class).
        build(null);
  }

  public String compileCss(String content) throws IOException {
    return runInClassLoader(m_lessLoader, CompileCssWithLess.class.getName(), content);
  }

  public String compileJs(String content) throws IOException {
    return content;
  }

  public String minifyCss(String content) throws IOException {
    return runInClassLoader(m_yuiLoader, MinifyCssWithYui.class.getName(), content);
  }

  public String minifyJs(String content) throws IOException {
    return runInClassLoader(m_yuiLoader, MinifyJsWithYui.class.getName(), content);
  }

  protected String runInClassLoader(ClassLoader loader, String classname, String arg0) throws IOException {
    try {
      Class<?> c = loader.loadClass(classname);
      Object o = c.newInstance();
      Method m = c.getMethod("run", String.class);
      Object result = m.invoke(o, arg0);
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
