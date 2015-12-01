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
package org.eclipse.scout.rt.shared.services.common.text;

import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.nls.DynamicNls;

public abstract class AbstractDynamicNlsTextProviderService implements ITextProviderService {

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
  protected abstract String getDynamicNlsBaseName();

  protected final DynamicNls instance = new DynamicNls();

  @PostConstruct
  protected void registerResourceBundle() {
    instance.registerResourceBundle(getDynamicNlsBaseName(), getClass());
  }

  @Override
  public String getText(Locale locale, String key, String... messageArguments) {
    return instance.getText(locale, key, messageArguments);
  }

  @Override
  public Map<String, String> getTextMap(Locale locale) {
    return instance.getTextMap(locale);
  }
}
