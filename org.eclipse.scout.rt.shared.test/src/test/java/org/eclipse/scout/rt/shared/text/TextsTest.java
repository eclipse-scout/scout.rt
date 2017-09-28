/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Locale;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.TEXTS;
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
  }

  @Test
  public void testGetWithFallback() {
    assertEquals("Value 2", TEXTS.getWithFallback("key2", "fallback"));
    assertEquals("fallback", TEXTS.getWithFallback("anyKey", "fallback"));
    assertNull(TEXTS.getWithFallback("anyKey", null));
  }

  @Test
  public void testGetTextMap() {
    Map<String, String> textMap = BEANS.get(ScoutTexts.class).getTextMap(Locale.ENGLISH);
    assertNotNull(textMap);
  }
}
