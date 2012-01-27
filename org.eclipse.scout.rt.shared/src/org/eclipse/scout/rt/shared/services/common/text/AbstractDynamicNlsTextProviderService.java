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

import org.eclipse.scout.commons.nls.DynamicNls;
import org.eclipse.scout.service.AbstractService;
import org.osgi.framework.ServiceRegistration;

public abstract class AbstractDynamicNlsTextProviderService extends AbstractService implements ITextProviderService {

  /**
   * Gets the base name where the <code>DynamicNls</code> instance searches for .properties files.<br>
   * Examples:<br>
   * <ul>
   * <li>"resources.texts.Texts": searches in &lt;plugin of your class&gt;/resources/texts/Texts&lt;language
   * suffix&gt;.properties</li>
   * <li>"translations.Docs": searches in &lt;plugin of your class&gt;/translations/Docs&lt;language
   * suffix&gt;.properties</li>
   * </ul>
   * 
   * @return A <code>String</code> containing the base name.
   * @see DynamicNls
   */
  protected abstract String getDynamicNlsBaseName();

  protected DynamicNls instance = new DynamicNls();

  @Override
  public void initializeService(ServiceRegistration registration) {
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
