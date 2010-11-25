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

import java.util.Locale;

import org.eclipse.scout.commons.nls.DynamicNls;

/**
 * This class provides the nls support.<br>
 * Do not change any member nor field of this class anytime otherwise the nls
 * support is not anymore garanteed. This class is auto generated and is
 * maintained by the plugins translations.nls file in the root directory of the
 * plugin.
 * 
 * @see translations.nls
 */
public class ScoutTexts extends DynamicNls {
  public static final String RESOURCE_BUNDLE_NAME = "resources.texts.ScoutTexts";//$NON-NLS-1$
  private static ScoutTexts instance = new ScoutTexts();

  public static ScoutTexts getInstance() {
    return instance;
  }

  public static String get(String key, String... messageArguments) {
    return getInstance().getText(key, messageArguments);
  }

  public static String get(Locale locale, String key, String... messageArguments) {
    return getInstance().getText(locale, key, messageArguments);
  }

  protected ScoutTexts() {
    registerResourceBundle(RESOURCE_BUNDLE_NAME, ScoutTexts.class);
  }
}
