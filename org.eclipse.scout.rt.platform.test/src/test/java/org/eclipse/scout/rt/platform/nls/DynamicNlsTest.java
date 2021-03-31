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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    assertNull(MissingResourceBundleTexts.get("anyKey"));
    Map<String, String> textMap = MissingResourceBundleTexts.getInstance().getTextMap(Locale.ENGLISH);
    assertNotNull(textMap);
    assertTrue(textMap.isEmpty());
  }

  @Test
  public void testTextPostProcessing() {
    DynamicNls nls = spy(new DynamicNls().withTextPostProcessor(new DefaultTextPostProcessor()));
    String streetGerman = "Straße";
    when(nls.getTextInternal(any(), anyString())).thenReturn(streetGerman);

    assertEquals(streetGerman, nls.getText(Locale.GERMAN, "whatever"));
    assertEquals("Strasse", nls.getText(new Locale("de", "CH"), "whatever"));
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
    public static final String RESOURCE_BUNDLE_NAME = "org.eclipse.scout.rt.platform.texts.NonExistingTexts";
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
    public static final String RESOURCE_BUNDLE_NAME = "org.eclipse.scout.rt.platform.texts.Texts";
    private static TestResourceBundleTexts instance = new TestResourceBundleTexts();

    public static TestResourceBundleTexts getInstance() {
      return instance;
    }

    protected TestResourceBundleTexts() {
      registerResourceBundle(RESOURCE_BUNDLE_NAME, TestResourceBundleTexts.class);
    }
  }
}
