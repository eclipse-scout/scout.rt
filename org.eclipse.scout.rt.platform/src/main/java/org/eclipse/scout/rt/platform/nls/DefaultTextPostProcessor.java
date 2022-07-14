/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.nls;

import java.util.Locale;

import org.eclipse.scout.rt.platform.text.NlsKey;
import org.eclipse.scout.rt.platform.util.StringUtility;

public class DefaultTextPostProcessor implements ITextPostProcessor {

  protected static final Locale DE_CH = new Locale("de", "CH");

  @Override
  public String apply(Locale textLocale, @NlsKey String textKey, String text, String... messageArguments) {
    if (textLocale == null || StringUtility.isNullOrEmpty(text)) {
      return text;
    }

    // check for language and country to allow variants
    if (DE_CH.getLanguage().equals(textLocale.getLanguage()) && DE_CH.getCountry().equals(textLocale.getCountry())) {
      text = text.replace("ß", "ss");
    }
    return text;
  }
}
