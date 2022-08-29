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

import java.util.UUID;

import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuIdWithCustomFromString;
import org.junit.Test;

public class AbstractUuIdTest {

  protected static final UUID TEST_UUID = UUID.randomUUID();
  protected static final FixtureUuId FIXTURE_UUID_1 = FixtureUuId.of(TEST_UUID);

  @Test
  public void testCompareTo_null() {
    assertEquals(1, FIXTURE_UUID_1.compareTo(null));
  }

  @Test
  public void testCompareTo_sameValue() {
    assertEquals(0, FIXTURE_UUID_1.compareTo(FixtureUuId.of(TEST_UUID)));
    assertEquals(0, FIXTURE_UUID_1.compareTo(FixtureUuIdWithCustomFromString.of(TEST_UUID)));
  }

  @Test
  public void testCompareTo_otherValue() {
    FixtureUuId fixtureUuId1 = FixtureUuId.of("4e0b3c9a-1ad4-41ba-a36e-1670c4872973");
    FixtureUuId fixtureUuId2 = FixtureUuId.of("4e0b3c9a-1ad4-41ba-a36e-1670c4872974");
    assertTrue(fixtureUuId1.compareTo(fixtureUuId2) < 0);
    assertTrue(fixtureUuId2.compareTo(fixtureUuId1) > 0);
  }
}
