/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PreferredValueTest {

  private final static Object OBJECT_1 = new Object();
  private final static Object OBJECT_2 = new Object();
  private final static Object OBJECT_3 = new Object();

  @Test
  public void test() {
    PreferredValue<Object> preferredValue = new PreferredValue<Object>(null, false);
    assertNull(preferredValue.get());
    assertFalse(preferredValue.isPreferred());

    preferredValue.set(OBJECT_1, false);
    assertSame(OBJECT_1, preferredValue.get());
    assertFalse(preferredValue.isPreferred());

    preferredValue.set(OBJECT_2, false);
    assertSame(OBJECT_2, preferredValue.get());
    assertFalse(preferredValue.isPreferred());

    preferredValue.set(OBJECT_3, true);
    assertSame(OBJECT_3, preferredValue.get());
    assertTrue(preferredValue.isPreferred());

    preferredValue.set(OBJECT_1, false);
    assertSame(OBJECT_3, preferredValue.get());
    assertTrue(preferredValue.isPreferred());

    preferredValue.set(OBJECT_2, true);
    assertSame(OBJECT_2, preferredValue.get());
    assertTrue(preferredValue.isPreferred());
  }

  @Test
  public void testCopy() {
    PreferredValue<Object> preferredValue = new PreferredValue<Object>(OBJECT_1, false);
    assertSame(OBJECT_1, preferredValue.get());
    assertFalse(preferredValue.isPreferred());

    PreferredValue<Object> copy = preferredValue.copy();
    copy.set(OBJECT_2, false);
    assertSame(OBJECT_1, preferredValue.get());
    assertFalse(preferredValue.isPreferred());
    assertSame(OBJECT_2, copy.get());
    assertFalse(copy.isPreferred());

    copy.set(OBJECT_3, true);
    assertSame(OBJECT_1, preferredValue.get());
    assertFalse(preferredValue.isPreferred());
    assertSame(OBJECT_3, copy.get());
    assertTrue(copy.isPreferred());
  }
}
