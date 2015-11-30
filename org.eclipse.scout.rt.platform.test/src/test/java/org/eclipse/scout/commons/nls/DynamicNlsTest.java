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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit for {@link DynamicNls}
 */
public class DynamicNlsTest {

  @Test
  public void testMissingResourceBundles() {
    assertEquals(null, MissingResourceBundleTexts.get("anyKey"));
    Map<String, String> textMap = MissingResourceBundleTexts.getInstance().getTextMap(Locale.ENGLISH);
    assertNotNull(textMap);
    assertTrue(textMap.isEmpty());
  }

  @Ignore("Performance Test: Not reliable")
  @Test(timeout = 1000)
  public void testMissingKeyPerformance() {
    TestResourceBundleTexts nls = TestResourceBundleTexts.getInstance();
    for (int i = 0; i < 1000000; i++) {
      nls.getText(Locale.GERMAN, "non-existing-key");
    }
  }

  private static class MissingResourceBundleTexts extends DynamicNls {
    public static final String RESOURCE_BUNDLE_NAME = "org.eclipse.scout.commons.texts.NonExistingTexts";
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

  private static class TestResourceBundleTexts extends DynamicNls {
    public static final String RESOURCE_BUNDLE_NAME = "org.eclipse.scout.commons.texts.Texts";
    private static TestResourceBundleTexts instance = new TestResourceBundleTexts();

    public static TestResourceBundleTexts getInstance() {
      return instance;
    }

    protected TestResourceBundleTexts() {
      registerResourceBundle(RESOURCE_BUNDLE_NAME, TestResourceBundleTexts.class);
    }
  }
}
