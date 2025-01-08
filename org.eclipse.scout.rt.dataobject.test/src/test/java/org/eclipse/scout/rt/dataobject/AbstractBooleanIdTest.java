/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.dataobject.fixture.FixtureBooleanId;
import org.junit.Test;

public class AbstractBooleanIdTest {

  protected static final Boolean TEST_ID = false;
  protected static final FixtureBooleanId FIXTURE_ID_1 = FixtureBooleanId.of(TEST_ID);

  @Test
  public void testCompareTo_null() {
    assertEquals(1, FIXTURE_ID_1.compareTo(null));
  }

  @Test
  public void testCompareTo_sameValue() {
    assertEquals(0, FIXTURE_ID_1.compareTo(FixtureBooleanId.of(TEST_ID)));
  }

  @Test
  public void testCompareTo_otherValue() {
    FixtureBooleanId id1 = FixtureBooleanId.of(false);
    FixtureBooleanId id2 = FixtureBooleanId.of(true);
    assertTrue(id1.compareTo(id2) < 0);
    assertTrue(id2.compareTo(id1) > 0);
  }
}
