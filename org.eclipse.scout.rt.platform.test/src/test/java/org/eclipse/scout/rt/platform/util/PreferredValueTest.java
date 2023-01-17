/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class PreferredValueTest {

  private static final Object OBJECT_1 = new Object();
  private static final Object OBJECT_2 = new Object();
  private static final Object OBJECT_3 = new Object();

  @Test
  public void test() {
    PreferredValue<Object> preferredValue = new PreferredValue<>(null, false);
    assertNull(preferredValue.get());
    assertFalse(preferredValue.isPreferredValue());

    preferredValue.set(OBJECT_1, false);
    assertSame(OBJECT_1, preferredValue.get());
    assertFalse(preferredValue.isPreferredValue());

    preferredValue.set(OBJECT_2, false);
    assertSame(OBJECT_2, preferredValue.get());
    assertFalse(preferredValue.isPreferredValue());

    preferredValue.set(OBJECT_3, true);
    assertSame(OBJECT_3, preferredValue.get());
    assertTrue(preferredValue.isPreferredValue());

    preferredValue.set(OBJECT_1, false);
    assertSame(OBJECT_3, preferredValue.get());
    assertTrue(preferredValue.isPreferredValue());

    preferredValue.set(OBJECT_2, true);
    assertSame(OBJECT_2, preferredValue.get());
    assertTrue(preferredValue.isPreferredValue());
  }

  @Test
  public void testMarkAsPreferredValue() {
    PreferredValue<Object> preferredValue = new PreferredValue<>(null, false);
    preferredValue.set(OBJECT_1, false);
    assertFalse(preferredValue.isPreferredValue());
    assertSame(OBJECT_1, preferredValue.get());

    preferredValue.markAsPreferredValue();
    assertTrue(preferredValue.isPreferredValue());
    assertSame(OBJECT_1, preferredValue.get());

    preferredValue.set(OBJECT_2, false);
    assertTrue(preferredValue.isPreferredValue());
    assertSame(OBJECT_1, preferredValue.get());
  }

  @Test
  public void testCopy() {
    PreferredValue<Object> preferredValue = new PreferredValue<>(OBJECT_1, false);
    assertSame(OBJECT_1, preferredValue.get());
    assertFalse(preferredValue.isPreferredValue());

    PreferredValue<Object> copy = preferredValue.copy();
    copy.set(OBJECT_2, false);
    assertSame(OBJECT_1, preferredValue.get());
    assertFalse(preferredValue.isPreferredValue());
    assertSame(OBJECT_2, copy.get());
    assertFalse(copy.isPreferredValue());

    copy.set(OBJECT_3, true);
    assertSame(OBJECT_1, preferredValue.get());
    assertFalse(preferredValue.isPreferredValue());
    assertSame(OBJECT_3, copy.get());
    assertTrue(copy.isPreferredValue());
  }
}
