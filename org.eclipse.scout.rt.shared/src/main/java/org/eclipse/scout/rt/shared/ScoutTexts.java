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

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.services.common.text.ITextProviderService;

/**
 * ScoutTexts provides support for text translations.
 * <p>
 * The actual translation is performed by {@link ITextProviderService}s. If none of them knows the text key a fallback
 * text is used (see {@link #getDefaultFallback(String)} and
 * {@link #getTextWithFallback(Locale, String, String, String...)}, respectively).
 * <p>
 * This implementation caches all available {@link ITextProviderService}s for better performance (otherwise 2/3 of a
 * {@link #getText(String, String...)} invocation would be spend for collecting {@link ITextProviderService}s). Invoke
 * {@link #reloadTextProviders()} after modifying the set of text provider services.
 *
 * @see TEXTS
 * @see ITextProviderService
 */
@ApplicationScoped
public class ScoutTexts {

  /**
   * Cached list of ordered {@link ITextProviderService}s
   */
  private volatile List<? extends ITextProviderService> m_textProviders;

  public ScoutTexts() {
    reloadTextProviders();
  }

  public void reloadTextProviders() {
    m_textProviders = BEANS.all(ITextProviderService.class);
  }

  public final String getText(String key, String... messageArguments) {
    return getText(null, key, messageArguments);
  }

  public final String getText(Locale locale, String key, String... messageArguments) {
    return getTextInternal(locale, key, getDefaultFallback(key), messageArguments);
  }

  public Map<String, String> getTextMap(Locale locale) {
    Map<String, String> map = new HashMap<String, String>();
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
