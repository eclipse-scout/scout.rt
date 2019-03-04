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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DynamicNls {

  private final List<NlsResourceBundleCache> m_resourceBundles;

  public DynamicNls() {
    m_resourceBundles = new ArrayList<>();
  }

  public void registerResourceBundle(String resourceBundleName, Class<?> wrapperClass) {
    m_resourceBundles.add(0, new NlsResourceBundleCache(resourceBundleName, wrapperClass));
  }

  /**
   * @param key
   *          nls text key
   * @param messageArguments
   *          the translation of the text might contain variables {0},{1},{2},... Examples: getText("MissingFile1");
   *          with translation: MissingFile1=Das File konnte nicht gefunden werden getText("MissingFile2",fileName);
   *          with translation: MissingFile2=Das File {0} konnte nicht gefunden werden.
   *          getText("MissingFile3",fileName,dir); with translation: MissingFile3=Das File {0} im Ordner {1} konnte
   *          nicht gefunden werden
   */
  public final String getText(String key, String... messageArguments) {
    return getText(null, key, messageArguments);
  }

  /**
   * @param locale
   *          the locale of the text
   * @param key
   *          nls text key
   * @param messageArguments
   *          the translation of the text might contain variables {0},{1},{2},... Examples: getText("MissingFile1");
   *          with translation: MissingFile1=Das File konnte nicht gefunden werden getText("MissingFile2",fileName);
   *          with translation: MissingFile2=Das File {0} konnte nicht gefunden werden.
   *          getText("MissingFile3",fileName,dir); with translation: MissingFile3=Das File {0} im Ordner {1} konnte
   *          nicht gefunden werden
   */
  public final String getText(Locale locale, String key, String... messageArguments) {
    if (key == null) {
      return null;
    }

    String text = getTextInternal(locale, key);
    return NlsUtility.bindText(text, messageArguments);
  }

  private String getTextInternal(Locale locale, String key) {
    if (locale == null) {
      locale = getDefaultLocale();
    }

    for (NlsResourceBundleCache c : m_resourceBundles) {
      NlsResourceBundle resourceBundle = c.getResourceBundle(locale);
      if (resourceBundle != null) {
        String result = resourceBundle.getText(key);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  /**
   * get all key/texts defined or redefined by the wrapper class for that locale
   */
  public Map<String, String> getTextMap(Locale locale) {
    if (locale == null) {
      locale = getDefaultLocale();
    }

    Map<String, String> map = new HashMap<>();
    for (NlsResourceBundleCache c : m_resourceBundles) {
      NlsResourceBundle resourceBundle = c.getResourceBundle(locale);
      if (resourceBundle != null) {
        resourceBundle.collectTextMapping(map::putIfAbsent);
      }
    }
    return map;
  }

  /**
   * Override this method to change default locale behavior
   */
  protected Locale getDefaultLocale() {
    return NlsLocale.get();
  }
}
