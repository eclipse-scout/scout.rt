/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.nls;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
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

  @Test
  public void testTextPostProcessorUniqueness() {
    ITextPostProcessor postProcessor = new TestingTextPostProcessor();
    DynamicNls nls = spy(new DynamicNls()
        .withTextPostProcessor(postProcessor));

    List<ITextPostProcessor> postProcessorList = Arrays.asList(postProcessor, postProcessor);
    nls.withTextPostProcessor(postProcessor);
    nls.withTextPostProcessors(postProcessorList);

    assertEquals(1, nls.getTextPostProcessors().size());
  }

  @Test
  public void testNullTextPostProcessor() {
    ITextPostProcessor postProcessor = new TestingTextPostProcessor();
    DynamicNls nls = spy(new DynamicNls());

    List<ITextPostProcessor> postProcessorList = Arrays.asList(postProcessor, null);
    nls.withTextPostProcessors(postProcessorList);

    assertEquals(1, nls.getTextPostProcessors().size());
  }

  @Test
  public void testTextPostProcessorOrder() {
    ITextPostProcessor postProcessor1 = new TestingTextPostProcessor();
    ITextPostProcessor postProcessor2 = new TestingTextPostProcessor();
    ITextPostProcessor postProcessor3 = new TestingTextPostProcessor();
    DynamicNls nls = spy(new DynamicNls());

    nls.withTextPostProcessors(Arrays.asList(postProcessor1, postProcessor2, postProcessor3));
    LinkedHashSet<ITextPostProcessor> expected = CollectionUtility.orderedHashSetWithoutNullElements(Arrays.asList(postProcessor1, postProcessor2, postProcessor3));

    // test initial post processors
    assertEqualOrder(expected, nls.getTextPostProcessors());

    // add one new post processor
    ITextPostProcessor postProcessor4 = new TestingTextPostProcessor();
    expected.add(postProcessor4);
    nls.withTextPostProcessor(postProcessor4);

    assertEqualOrder(expected, nls.getTextPostProcessors());
  }

  @Ignore("Performance Test: Not reliable")
  @Test(timeout = 1000)
  public void testMissingKeyPerformance() {
    TestResourceBundleTexts nls = TestResourceBundleTexts.getInstance();
    for (int i = 0; i < 1000000; i++) {
      nls.getText(Locale.GERMAN, "non-existing-key");
    }
  }

  protected void assertEqualOrder(Collection<?> expected, Collection<?> actual) {
    assertEquals(expected.size(), actual.size());

    Iterator<?> expectedIterator = expected.iterator();
    Iterator<?> actualIterator = actual.iterator();
    while (expectedIterator.hasNext()) {
      assertEquals(expectedIterator.next(), actualIterator.next());
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

  private static class TestingTextPostProcessor implements ITextPostProcessor {

    @Override
    public String apply(Locale textLocale, String textKey, String text, String... messageArguments) {
      return text;
    }
  }
}
