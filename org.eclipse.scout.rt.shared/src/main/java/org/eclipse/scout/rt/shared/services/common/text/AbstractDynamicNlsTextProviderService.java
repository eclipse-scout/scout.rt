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
package org.eclipse.scout.rt.shared.services.common.text;

import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.eclipse.scout.rt.platform.nls.DynamicNls;

public abstract class AbstractDynamicNlsTextProviderService implements ITextProviderService {

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
