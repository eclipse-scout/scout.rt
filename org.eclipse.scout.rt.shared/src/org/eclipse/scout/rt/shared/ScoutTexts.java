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
package org.eclipse.scout.rt.shared;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.scout.rt.shared.services.common.text.ITextProviderService;
import org.eclipse.scout.service.SERVICES;

/**
 * This is the base class for translations access in scout applications.<br>
 * It provides prioritized access to all text services available to the scope.
 * 
 * @see IClientSession#getNlsTexts()
 * @see IServerSession#getNlsTexts()
 * @see ClientJob
 * @see ServerJob
 * @see ITextProviderService
 */
public class ScoutTexts {

  /**
   * Jop property name tha can be used when Texts should be used in associated contexts such as swt display, rwt display
   * etc.
   */
  public static final QualifiedName JOB_PROPERTY_NAME = new QualifiedName(ScoutTexts.class.getName(), "ref");

  private static final ScoutTexts defaultInstance = new ScoutTexts();

  private final ITextProviderService[] m_textProviders;

  public ScoutTexts() {
    this(SERVICES.getServices(ITextProviderService.class));
  }

  public ScoutTexts(ITextProviderService[] textProviders) {
    m_textProviders = textProviders;
  }

  public static String get(String key, String... messageArguments) {
    return getInstance().getText(key, messageArguments);
  }

  public static String get(Locale locale, String key, String... messageArguments) {
    return getInstance().getText(locale, key, messageArguments);
  }

  /**
   * Queries {@link TextsThreadLocal#get()}. If this one is null, then the default
   * instance is used as global default. That way applications can override scout texts in their
   * application sessions, without interfering with other scout applications in the same osgi/eclipse runtime.
   * 
   * @return the <code>ScoutTexts</code> instance to use in current scope.
   */
  public static ScoutTexts getInstance() {
    ScoutTexts t = TextsThreadLocal.get();
    if (t == null) {
      t = defaultInstance;
    }
    return t;
  }

  public final String getText(String key, String... messageArguments) {
    return getText(null, key, messageArguments);
  }

  public final String getText(Locale locale, String key, String... messageArguments) {
    return getTextInternal(locale, key, messageArguments);
  }

  public Map<String, String> getTextMap(Locale locale) {
    HashMap<String, String> map = new HashMap<String, String>();
    ITextProviderService[] providers = getTextProviders();
    for (int i = providers.length - 1; i >= 0; i--) {
      map.putAll(providers[i].getTextMap(locale));
    }
    return map;
  }

  protected ITextProviderService[] getTextProviders() {
    return m_textProviders;
  }

  protected String getTextInternal(Locale locale, String key, String... messageArguments) {
    for (ITextProviderService provider : getTextProviders()) {
      String result = provider.getText(locale, key, messageArguments);
      if (result != null) {
        return result;
      }
    }
    return "{undefined text " + key + "}";
  }
}
