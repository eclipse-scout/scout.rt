/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.nls;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h4>NlsResourceBundle</h4> Other than {@link ResourceBundle} this bundle loads bundles by their intuitive names.
 * Example: There are bundles Texts.properties (default English), Texts_de.properties and Texts_fr.properties
 * {@link ResourceBundle#getBundle("Texts",de_CH)} will yield Texts_de.properties! This cache will yield
 * Texts.properties (which basically is the correct solution)
 *
 * @author Ivan Motsch
 */
public final class NlsResourceBundle extends PropertyResourceBundle {

  private static final Logger LOG = LoggerFactory.getLogger(NlsResourceBundle.class);

  public static final String TEXT_RESOURCE_EXTENSION = "properties";

  public NlsResourceBundle(InputStream stream) throws IOException {
    super(stream);
  }

  @SuppressWarnings("squid:S1185")
  @Override
  protected void setParent(ResourceBundle parent) {
    // allow access of this setter in NlsResourceBundleCache
    super.setParent(parent);
  }

  public static NlsResourceBundle getBundle(String baseName, Locale locale, ClassLoader cl) {
    String suffix = getLocaleSuffix(locale);
    try {
      return NlsResourceBundle.getBundle(baseName, suffix, cl);
    }
    catch (IOException e) {
      LOG.warn("Error loading nls resource with base name '{}' and suffix '{}'", baseName, suffix, e);
      return null;
    }
  }

  private static String getLocaleSuffix(Locale locale) {
    if (locale == null || Locale.ROOT.equals(locale)) {
      return "";
    }
    return "_" + locale.toString();
  }

  /**
   * Creates and returns a new {@link NlsResourceBundle} if there is a property file found using the given base name and
   * suffix. Otherwise it returns null.
   */
  public static NlsResourceBundle getBundle(String baseName, String suffix, ClassLoader cl) throws IOException {
    String fileName = baseName.replace('.', '/') + suffix + '.' + TEXT_RESOURCE_EXTENSION;
    URL res = cl.getResource(fileName);
    if (res != null) {
      try (InputStream in = res.openStream()) {
        return new NlsResourceBundle(in);
      }
    }
    return null;
  }
}
