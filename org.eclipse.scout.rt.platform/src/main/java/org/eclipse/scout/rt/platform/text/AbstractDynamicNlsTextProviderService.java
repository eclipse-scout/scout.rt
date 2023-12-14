/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.text;

import java.util.Locale;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.nls.DynamicNls;
import org.eclipse.scout.rt.platform.nls.ITextPostProcessor;

public abstract class AbstractDynamicNlsTextProviderService implements ITextProviderService {

  private final DynamicNls m_instance = createDynamicNls();

  /**
   * Gets the base name where the <code>DynamicNls</code> instance searches for .properties files.<br>
   * Examples:<br>
   * <ul>
   * <li>"org.eclipse.scout.rt.shared.texts.Texts": searches in
   * org/eclipse/scout/rt/shared/texts/Texts&lt;languagesuffix&gt;.properties on the classpath.</li>
   * <li>"org.eclipse.scout.rt.shared.translations.Docs": searches in
   * org/eclipse/scout/rt/shared/translations/Docs&lt;languagesuffix&gt;.properties on the classpath</li>
   * </ul>
   *
   * @return A <code>String</code> containing the base name.
   * @see DynamicNls
   */
  public abstract String getDynamicNlsBaseName();

  public DynamicNls getDynamicNls() {
    return m_instance;
  }

  protected DynamicNls createDynamicNls() {
    return BEANS.get(DynamicNls.class).withTextPostProcessors(BEANS.all(ITextPostProcessor.class));
  }

  @PostConstruct
  protected void registerResourceBundle() {
    getDynamicNls().registerResourceBundle(getDynamicNlsBaseName(), getClass());
  }

  @Override
  public String getText(Locale locale, String key, String... messageArguments) {
    return getDynamicNls().getText(locale, key, messageArguments);
  }

  @Override
  public Map<String, String> getTextMap(Locale locale) {
    return getDynamicNls().getTextMap(locale);
  }
}
