/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.nls;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.DevelopmentTextsFileWatcherEnabledProperty;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h4>ResourceBundleCache</h4> Is used to cache resource bundle instances per {@link Locale}
 *
 * @author Ivan Motsch
 */
public class NlsResourceBundleCache {
  private static final Logger LOG = LoggerFactory.getLogger(NlsResourceBundleCache.class);

  private final String m_resourceBundleName;
  private final Class<?> m_wrapperClass;
  private final ConcurrentMap<Locale, NlsResourceBundle> m_resourceBundles;

  /**
   * constant indicating that no resource bundle exists
   */
  private static final NlsResourceBundle NONEXISTENT_BUNDLE = new NlsResourceBundle(null, Collections.emptyMap());

  public NlsResourceBundleCache(String resourceBundleName, Class wrapperClass) {
    m_resourceBundleName = resourceBundleName;
    m_wrapperClass = wrapperClass;
    m_resourceBundles = new ConcurrentHashMap<>();

    // Attach a file watcher in development mode to clear the cache if files have been edited.
    if (Platform.get().inDevelopmentMode() && CONFIG.getPropertyValue(DevelopmentTextsFileWatcherEnabledProperty.class)) {
      try {
        BEANS.get(NlsFileWatcher.class).watch(m_resourceBundleName, (path) -> {
          m_resourceBundles.clear();
          LOG.info("Cleared bundle cache for nls resource bundle {}", m_resourceBundleName);
        }, m_wrapperClass.getClassLoader());
      }
      catch (IOException e) {
        LOG.warn("Could not create nls file watcher", e);
      }
    }
  }

  public Class<?> getWrapperClass() {
    return m_wrapperClass;
  }

  public NlsResourceBundle getResourceBundle(Locale locale) {
    Assertions.assertNotNull(locale);
    NlsResourceBundle resourceBundle = m_resourceBundles.get(locale);
    if (resourceBundle == NONEXISTENT_BUNDLE) {
      return null;
    }
    if (resourceBundle != null) {
      return resourceBundle;
    }

    resourceBundle = loadBundle(locale);
    if (resourceBundle == null) {
      // each execution will return the same result (thus it's okay to directly put this result, it will always be null once null)
      // null is not supported, thus using a singleton instead to mark non existent bundle
      m_resourceBundles.put(locale, NONEXISTENT_BUNDLE);
      return null;
    }
    NlsResourceBundle r = m_resourceBundles.putIfAbsent(locale, resourceBundle);
    // check for non existent bundle not necessary because already verified
    // by explicitly calling NlsResourceBundle.getBundle above
    // always returning same instance (thus using r if set in which case locale resource bundle is obsolete)
    return r != null ? r : resourceBundle;
  }

  protected NlsResourceBundle loadBundle(Locale locale) {
    if (Locale.ROOT.equals(locale)) {
      return NlsResourceBundle.getBundle(null, m_resourceBundleName, locale, m_wrapperClass.getClassLoader());
    }
    NlsResourceBundle parentBundle = getResourceBundle(getParentLocale(locale));
    NlsResourceBundle bundle = NlsResourceBundle.getBundle(parentBundle, m_resourceBundleName, locale, m_wrapperClass.getClassLoader());
    return bundle != null ? bundle : parentBundle; // use directly parent bundle if not found
  }

  protected Locale getParentLocale(Locale locale) {
    String tag = locale.toLanguageTag();
    int idx = tag.lastIndexOf('-');
    if (idx > 0) {
      String parentTag = tag.substring(0, idx);
      return Locale.forLanguageTag(parentTag);
    }
    // either this is a locale with only a language or it is the root locale - in any case return root locale as 'parent'
    return Locale.ROOT;
  }
}
