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

import static org.junit.Assert.*;

import java.util.Locale;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JUnit tests for {@link ScoutTexts} and {@link TEXTS}
 */
@RunWith(PlatformTestRunner.class)
public class TextsTest {

  @Test
  public void testGet() {
    assertEquals("Value 1", TEXTS.get("key1"));
    assertEquals("{undefined text anyKey}", TEXTS.get("anyKey"));
    assertEquals("Value 1", TEXTS.get("key1", "X"));
    assertEquals("value X", TEXTS.get("key6", "X", "Y"));
    assertEquals("value {0}", TEXTS.get("key6"));
  }

  @Test
  public void testGetWithFallback() {
    assertEquals("Value 2", TEXTS.getWithFallback("key2", "fallback"));
    assertEquals("fallback", TEXTS.getWithFallback("anyKey", "fallback"));
    assertNull(TEXTS.getWithFallback("anyKey", null));
    assertEquals("value ABC", TEXTS.getWithFallback("key6", "fallback {0}", "ABC"));
    assertEquals("fallback {0}", TEXTS.getWithFallback("anyKey", "fallback {0}", "ABC"));
    assertEquals("value DEF", TEXTS.getWithFallback("anyKey", TEXTS.getWithFallback("key6", "fallback", "DEF"), "ABC"));
  }

  @Test
  public void testGetTextMap() {
    Map<String, String> textMap = BEANS.get(ScoutTexts.class).getTextMap(Locale.ENGLISH);
    assertNotNull(textMap);
  }
}
