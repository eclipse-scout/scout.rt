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

import java.util.Date;

import org.eclipse.scout.rt.dataobject.fixture.FixtureDateId;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.junit.Test;

public class AbstractDateIdTest {

  protected static final Date TEST_ID = DateUtility.parse("2012-10-01", "yyyy-MM-dd");
  protected static final FixtureDateId FIXTURE_ID_1 = FixtureDateId.of(TEST_ID);

  @Test
  public void testCompareTo_null() {
    assertEquals(1, FIXTURE_ID_1.compareTo(null));
  }

  @Test
  public void testCompareTo_sameValue() {
    assertEquals(0, FIXTURE_ID_1.compareTo(FixtureDateId.of(TEST_ID)));
  }

  @Test
  public void testCompareTo_otherValue() {
    FixtureDateId id1 = FixtureDateId.of(DateUtility.parse("2015-07-01", "yyyy-MM-dd"));
    FixtureDateId id2 = FixtureDateId.of(DateUtility.parse("2024-01-01", "yyyy-MM-dd"));
    assertTrue(id1.compareTo(id2) < 0);
    assertTrue(id2.compareTo(id1) > 0);
  }
}
