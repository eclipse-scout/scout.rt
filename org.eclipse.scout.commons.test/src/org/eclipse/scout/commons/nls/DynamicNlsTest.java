/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.nls;

import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit for {@link DynamicNls}
 */
public class DynamicNlsTest {

  @Test
  public void testMissingResourceBundles() {
    Assert.assertEquals(null, MissingResourceBundleTexts.get("anyKey"));
    Map<String, String> textMap = MissingResourceBundleTexts.getInstance().getTextMap(Locale.ENGLISH);
    Assert.assertNotNull(textMap);
    Assert.assertTrue(textMap.isEmpty());
  }

  private static class MissingResourceBundleTexts extends DynamicNls {
    public static final String RESOURCE_BUNDLE_NAME = "resources.texts.NonExistingTexts";//$NON-NLS-1$
    private static MissingResourceBundleTexts instance = new MissingResourceBundleTexts();

    public static MissingResourceBundleTexts getInstance() {
      return instance;
    }

    public static String get(String key, String... messageArguments) {
      return getInstance().getText(key, messageArguments);
    }

    protected MissingResourceBundleTexts() {
      registerResourceBundle(RESOURCE_BUNDLE_NAME, MissingResourceBundleTexts.class);
    }
  }
}
