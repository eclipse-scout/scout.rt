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
package org.eclipse.scout.commons.runtime;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Browser known classes on the class path Note: this visitor only detects
 * classes on the "visitable" classpath. This is mainly the area of
 * URLClassLoaders and jar/zip-based archives
 */
public class ClasspathBrowser {
  private static final boolean DEBUG_PATH = false;
  private static final boolean DEBUG_ENTRY = false;

  private ArrayList<String> m_classpathsList = new ArrayList<String>();
  //
  private Set<String> m_classes = new TreeSet<String>();
  private Set<String> m_resources = new TreeSet<String>();

  public ClasspathBrowser() {
  }

  public void addDefaultClasspaths() {
    addClasspath(System.getProperty("sun.boot.class.path"));
    addClasspath(System.getProperty("java.class.path"));
  }

  public void addClasspath(String cp) {
    m_classpathsList.add(cp);
  }

  public void addClasspathsByClassLoader(Class startClass) {
    ClassLoader cl = startClass.getClassLoader();
    while (cl != null) {
      if (cl instanceof URLClassLoader) {
        URL[] a = ((URLClassLoader) cl).getURLs();
        if (a != null) {
          for (URL url : a) {
            m_classpathsList.add(url.getFile());
          }
        }
      }
      else {
        /*
         * IBM's WebSphere uses a com.ibm.ws.classloader.CompoundClassLoader
         * that has a getPaths() method.
         */
        try {
          Method m = cl.getClass().getMethod("getPaths");
          String[] b = (String[]) m.invoke(cl);
          if (b != null) {
            for (String path : b) {
              m_classpathsList.add(path);
            }
          }
        }
        catch (Exception e) {
          if (DEBUG_PATH) log(e.getMessage());
        }
      }
      // next
      cl = cl.getParent();
    }
  }

  public void visit() throws Exception {
    m_classes.clear();
    m_resources.clear();
    for (String s : m_classpathsList) {
      visitClasspaths(s);
    }
  }

  public String[] getClasses() {
    return m_classes.toArray(new String[0]);
  }

  public String[] getResources() {
    return m_resources.toArray(new String[0]);
  }

  private void visitClasspaths(String classpaths) throws Exception {
    if (DEBUG_PATH) log("visit classpaths " + classpaths);
    String[] a = classpaths.split(File.pathSeparator);
    for (String cp : a) {
      visitClasspath(cp);
    }
  }

  private void visitClasspath(String classpath) throws Exception {
    if (DEBUG_PATH) log(" visit classpath " + classpath);
    File f = new File(classpath.trim());
    if (f.isDirectory()) {
      String dirName = f.getCanonicalPath();
      int dirNameLen = dirName.length();
      if (dirName.endsWith("/") || dirName.endsWith("\\")) {
        // ok
      }
      else {
        dirNameLen++;
      }
      visitDirectory(dirNameLen, f);
    }
    else if (f.exists()) {
      visitJar(f);
    }
    else {
      if (DEBUG_PATH) log(" " + classpath + " does not exist");
    }
  }

  private void visitJar(File f) throws Exception {
    JarFile jarFile = new JarFile(f);
    if (DEBUG_PATH) log("  visit jar " + f + " with " + jarFile.size() + " elements");
    for (Enumeration en = jarFile.entries(); en.hasMoreElements();) {
      JarEntry entry = (JarEntry) en.nextElement();
      if (entry.isDirectory()) {
        // nop
      }
      else {
        String entryName = entry.getName();
        visitEntry(entryName);
      }
    }
  }

  private void visitDirectory(int namePrefixLen, File dir) throws Exception {
    File[] files = dir.listFiles();
    if (files != null) {
      int n = files.length;
      if (DEBUG_PATH) log("  visit dir " + dir + " with " + n + " elements");
      for (int i = 0; i < n; i++) {
        if (files[i].isDirectory()) {
          visitDirectory(namePrefixLen, files[i]);
        }
        else {
          String fileName = files[i].getCanonicalPath();
          String entryName = fileName.substring(namePrefixLen);
          visitEntry(entryName);
        }
      }
    }
  }

  private void visitEntry(String entryName) throws Exception {
    if (entryName.endsWith(".class")) {
      // class
      String className = entryName.substring(0, entryName.length() - 6);
      className = className.replaceAll("[/\\\\]", ".");
      if (acceptClass(className)) {
        // add
        if (DEBUG_ENTRY) log("   C " + className + " +");
        m_classes.add(className);
      }
      else {
        if (DEBUG_ENTRY) log("   C " + className + " -");
      }
    }
    else {
      // resource
      String resName = entryName;
      String resPath = "";
      resName = resName.replace('\\', '/');
      int i = resName.lastIndexOf('/');
      if (i >= 0) {
        resPath = resName.substring(0, i + 1);
        resName = resName.substring(i + 1);
      }
      if (!resPath.startsWith("/")) {
        resPath = "/" + resPath;
      }
      if (acceptResoure(resPath, resName)) {
        // add
        if (DEBUG_ENTRY) log("   R " + resPath + resName + " +");
        m_resources.add(resPath + resName);
      }
      else {
        if (DEBUG_ENTRY) log("   R " + resPath + resName + " -");
      }
    }
  }

  protected boolean acceptClass(String className) {
    if (className.indexOf("$") >= 0) return false;
    if (className.startsWith("java.")) return false;
    if (className.startsWith("javax.")) return false;
    if (className.startsWith("com.sun.")) return false;
    if (className.startsWith("sun.")) return false;
    if (className.startsWith("sunw.")) return false;
    if (className.startsWith("org.")) return false;
    if (className.startsWith("oracle.")) return false;
    if (className.startsWith("com.thoughtworks.")) return false;
    if (className.startsWith("com.ibm.")) return false;
    if (className.startsWith("com.tivoli.")) return false;
    if (className.startsWith("net.sf.retrotranslator.")) return false;
    if (className.startsWith("WEB-INF.classes.")) return false;
    return true;
  }

  /**
   * @param path
   *          only forwardslashes and a forward slash at the end (
   *          /META-INF/services/ )
   * @param name
   *          name without path info ( config.xml )
   */

  protected boolean acceptResoure(String path, String name) {
    if (path.startsWith("/java/")) return false;
    if (path.startsWith("/sun/")) return false;
    if (path.startsWith("/com/sun/")) return false;
    if (path.startsWith("/javax/")) return false;
    if (path.startsWith("/java/")) return false;
    if (path.startsWith("/org/")) return false;
    if (path.startsWith("/oracle/")) return false;
    if (path.startsWith("/com/ibm/")) return false;
    if (path.startsWith("/com/tivoli/")) return false;
    if (path.startsWith("/net/sf/retrotranslator/")) return false;
    if (path.startsWith("/WEB-INF/classes/")) return false;
    return true;
  }

  private void log(String s) {
    System.out.println(s);
  }

}
