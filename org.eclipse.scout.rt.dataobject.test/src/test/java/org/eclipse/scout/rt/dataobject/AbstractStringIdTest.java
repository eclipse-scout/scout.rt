/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.dataobject.fixture.FixtureString2Id;
import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.junit.Test;

public class AbstractStringIdTest {

  protected static final String TEST_ID = "mock";
  protected static final FixtureStringId FIXTURE_ID_1 = FixtureStringId.of(TEST_ID);

  @Test
  public void testCompareTo_null() {
    assertEquals(1, FIXTURE_ID_1.compareTo(null));
  }

  @Test
  public void testCompareTo_sameValue() {
    assertEquals(0, FIXTURE_ID_1.compareTo(FixtureStringId.of(TEST_ID)));
    assertEquals(0, FIXTURE_ID_1.compareTo(FixtureString2Id.of(TEST_ID)));
  }

  @Test
  public void testCompareTo_otherValue() {
    FixtureStringId id1 = FixtureStringId.of("abcd");
    FixtureStringId id2 = FixtureStringId.of("efgh");
    assertTrue(id1.compareTo(id2) < 0);
    assertTrue(id2.compareTo(id1) > 0);
  }
}
