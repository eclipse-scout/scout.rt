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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * <h4>NlsResourceBundle</h4> Other than {@link ResourceBundle} this bundle loads bundles by their intuitive names.
 * Example: There are bundles Texts.properties (default English), Texts_de.properties and Texts_fr.properties {@link
 * ResourceBundle#getBundle("Texts",de_CH)} will yield Texts_de.properties! This cache will yield Texts.properties
 * (which basically is the correct solution)
 *
 * @author Ivan Motsch
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
}
