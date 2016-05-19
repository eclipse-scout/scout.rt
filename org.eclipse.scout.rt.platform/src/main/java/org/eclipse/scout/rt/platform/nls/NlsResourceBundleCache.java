/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.nls;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <h4>ResourceBundleCache</h4> Is used to cache resource bundle instances per {@link java.util.Locale}
 *
 * @author Ivan Motsch
 */
public class NlsResourceBundleCache {
  private final String m_resourceBundleName;
  private final Class<?> m_wrapperClass;
  private final ConcurrentMap<Locale, ResourceBundle> m_resourceBundles;

  /** constant indicating that no resource bundle exists */
  private static final ResourceBundle NONEXISTENT_BUNDLE = new ResourceBundle() {
    @Override
    public Enumeration<String> getKeys() {
      return null;
    }

    @Override
    protected Object handleGetObject(String key) {
      return null;
    }

    @Override
    public String toString() {
      return "NONEXISTENT_BUNDLE";
    }
  };

  public NlsResourceBundleCache(String resourceBundleName, Class wrapperClass) {
    m_resourceBundleName = resourceBundleName;
    m_wrapperClass = wrapperClass;
    m_resourceBundles = new ConcurrentHashMap<Locale, ResourceBundle>();
  }

  public ResourceBundle getResourceBundle(Locale locale) {
    if (locale == null) {
      throw new IllegalArgumentException("locale must not be null");
    }

    ResourceBundle resourceBundle = m_resourceBundles.get(locale);
    if (resourceBundle == NONEXISTENT_BUNDLE) {
      return null;
    }

    if (resourceBundle != null) {
      return resourceBundle;
    }

    resourceBundle = NlsResourceBundle.getBundle(m_resourceBundleName, locale, m_wrapperClass.getClassLoader());
    if (resourceBundle == null) {
      // each execution will return the same result (thus it's okay to directly put this result, it will always be null once null)
      // null is not supported, thus using a singleton instead to mark non existent bundle
      m_resourceBundles.put(locale, NONEXISTENT_BUNDLE);
      return null;
    }

    ResourceBundle r = m_resourceBundles.putIfAbsent(locale, resourceBundle);
    // check for non existent bundle not necessary because already verified
    // by explicitly calling NlsResourceBundle.getBundle above
    // always returning same instance (thus using r if set in which case locale resource bundle is obsolete)
    return r != null ? r : resourceBundle;
  }

  public Class<?> getWrapperClass() {
    return m_wrapperClass;
  }
}
