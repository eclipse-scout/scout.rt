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
package org.eclipse.scout.rt.shared;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.services.common.text.ITextProviderService;

/**
 * This is the base class for translations access in scout applications.<br>
 * It provides prioritized access to all text services available to the scope.
 *
 * @see ITextProviderService
 */
public class ScoutTexts {

  /**
   * The {@link ScoutTexts} which are currently associated with the current thread.
   */
  public static final ThreadLocal<ScoutTexts> CURRENT = new ThreadLocal<>();

  private static volatile ScoutTexts DEFAULT;

  private final List<? extends ITextProviderService> m_textProviders;

  public ScoutTexts() {
    this(BEANS.all(ITextProviderService.class));
  }

  public ScoutTexts(List<? extends ITextProviderService> textProviders) {
    m_textProviders = textProviders;
  }

  public static String get(String key, String... messageArguments) {
    return getInstance().getText(key, messageArguments);
  }

  public static String get(Locale locale, String key, String... messageArguments) {
    return getInstance().getText(locale, key, messageArguments);
  }

  /**
   * @return {@link ScoutTexts} associated with the current thread or the JVM-wide instance if not set.
   */
  public static ScoutTexts getInstance() {
    ScoutTexts texts = ScoutTexts.CURRENT.get();
    if (texts != null) {
      return texts;
    }

    // Create the global instance lazy because of the service call in the default constructor.
    if (DEFAULT == null) {
      synchronized (ScoutTexts.class) {
        if (DEFAULT == null) {
          DEFAULT = new ScoutTexts();
        }
      }
    }
    return DEFAULT;
  }

  public final String getText(String key, String... messageArguments) {
    return getText(null, key, messageArguments);
  }

  public final String getText(Locale locale, String key, String... messageArguments) {
    return getTextInternal(locale, key, getDefaultFallback(key), messageArguments);
  }

  public Map<String, String> getTextMap(Locale locale) {
    HashMap<String, String> map = new HashMap<String, String>();
    List<? extends ITextProviderService> providers = getTextProviders();
    for (int i = providers.size() - 1; i >= 0; i--) {
      map.putAll(providers.get(i).getTextMap(locale));
    }
    return map;
  }

  protected List<? extends ITextProviderService> getTextProviders() {
    return m_textProviders;
  }

  protected String getTextInternal(Locale locale, String key, String fallback, String... messageArguments) {
    for (ITextProviderService provider : getTextProviders()) {
      String result = provider.getText(locale, key, messageArguments);
      if (result != null) {
        return result;
      }
    }
    return fallback;
  }

  protected String getDefaultFallback(String key) {
    return "{undefined text " + key + "}";
  }

  public String getTextWithFallback(Locale locale, String key, String fallback, String... messageArguments) {
    return getTextInternal(locale, key, fallback, messageArguments);
  }
}
