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
package org.eclipse.scout.commons.nls;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.eclipse.scout.commons.internal.Activator;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * <h4>NlsResourceBundle</h4> Other than {@link ResourceBundle} this bundle
 * loads bundles by their intuitiv names. Example: There are bundles
 * Texts.properties (default english), Texts_de.properties and
 * Texts_fr.properties {@link ResourceBundle#getBundle("Texts",de_CH)} will
 * yield Texts_de.properties! This cache will yield Texts.properties (which
 * basically is the correct solution)
 * 
 * @author imo
 */
public final class NlsResourceBundle extends PropertyResourceBundle {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(NlsResourceBundle.class);

  public NlsResourceBundle(InputStream stream) throws IOException {
    super(stream);
  }

  public static NlsResourceBundle getBundle(String baseName, Locale locale, ClassLoader cl) {
    return getBundle(baseName, locale, cl, (Class) null);
  }

  public static NlsResourceBundle getBundle(String baseName, Locale locale, ClassLoader cl, Class wrapperClass) {
    String ls = locale.toString();
    ArrayList<String> suffixes = new ArrayList<String>();
    suffixes.add("_" + ls);
    int i = ls.lastIndexOf('_');
    while (i >= 0) {
      ls = ls.substring(0, i);
      suffixes.add("_" + ls);
      // next
      i = ls.lastIndexOf('_');
    }
    suffixes.add("");
    //
    NlsResourceBundle root = null;
    NlsResourceBundle child = null;
    for (String suffix : suffixes) {
      String fileName = baseName.replace('.', '/') + suffix + ".properties";
      URL res = cl.getResource(fileName);
      if (res == null) {
        // Resource not found by class loader. Perhaps this instance is used outside a running
        // Equinox instance (e.g. from an ordinary JUnit test started from the Eclipse IDE).
        // Try to load the resource bundle from the expanded development workspace project structure.
        res = getResourceFromDevelopmentWorkspaceProject(wrapperClass, fileName);
      }
      if (res != null) {
        InputStream in = null;
        try {
          in = res.openStream();
          NlsResourceBundle parent = new NlsResourceBundle(in);
          if (root == null) {
            root = parent;
          }
          if (child != null) {
            child.setParent(parent);
          }
          child = parent;
        }
        catch (IOException e) {
          LOG.warn(null, e);
        }
        finally {
          if (in != null) {
            try {
              in.close();
            }
            catch (Throwable t) {
            }
          }
        }
      }
    }
    return root;
  }

  /**
   * Resolves the requested file in the given class's project location if the current java process is not running in an
   * eclipse instance (e.g. if an ordinary JUnit test is executed). The requested file is expected to be located
   * directly in the project folder and the compiled classes are expected to be located in a sub-folder in the project
   * directory as well.
   * <p/>
   * <h3>Example</h3> The following project structure is expected for
   * <code>getResourceFromDevelopmentWorkspaceProject(org.eclipse.foo.Texts.class, "resources/texts/Texts.properties");</code>
   * 
   * <pre>
   * o project folder
   * |
   * +---o bin
   * |   |
   * |   +---o org.eclipse.foo.Texts.class
   * |   +---o &lt;other compiled java classes&gt;
   * |
   * +---o resources
   *     |
   *     +---o texts
   *         |
   *         +---o Texts.properties
   *         +---o Texts_de.properties
   *         +---o Texts_en.properties
   * </pre>
   * 
   * @param clazz
   *          arbitrary class for determining the requested resource's project location on disk.
   * @param fileName
   *          file name of the requested resource relative to the wrapper class's project folder.
   * @return Returns <code>null</code> if invoked within a running eclipse environment or if the requested resource can
   *         not be found. Otherwise a file URL is returned.
   */
  private static URL getResourceFromDevelopmentWorkspaceProject(Class clazz, String fileName) {
    if (Activator.getDefault() != null) {
      // eclipse is running
      return null;
    }
    URL res = null;
    try {
      URL location = clazz.getProtectionDomain().getCodeSource().getLocation();
      if ("file".equals(location.getProtocol())) {
        File projectLocation = new File(location.toURI());
        if (projectLocation.exists() && projectLocation.isDirectory()) {
          File f = new File(projectLocation, fileName);
          if (f.exists() && f.isFile()) {
            res = f.toURI().toURL();
          }
        }
      }
    }
    catch (Exception e) {
      LOG.debug("Error while resolving resource bundle in development mode outside a running eclipse", e);
    }
    return res;
  }

}
