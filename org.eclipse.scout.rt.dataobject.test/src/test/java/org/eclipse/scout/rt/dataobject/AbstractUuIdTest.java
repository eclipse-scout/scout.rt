/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.UUID;

import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.id.AbstractUuId;
import org.junit.Test;

/**
 * Test cases for {@link AbstractUuId}
 */
public class AbstractUuIdTest {

  protected static final UUID TEST_UUID = UUID.randomUUID();
  protected static final FixtureUuId FIXTURE_UUID_1 = FixtureUuId.of(TEST_UUID);
  protected static final FixtureUuId FIXTURE_UUID_2 = FixtureUuId.of(TEST_UUID);

  @Test
  public void testHashCode() {
    assertEquals(FIXTURE_UUID_1.hashCode(), FIXTURE_UUID_1.hashCode());
  }

  @Test
  public void testUnwrap() {
    assertEquals(TEST_UUID, FIXTURE_UUID_1.unwrap());
  }

  @Test
  public void testEqualsObject() {
    assertEquals(FIXTURE_UUID_1, FIXTURE_UUID_2);
    assertEquals(FIXTURE_UUID_1, FIXTURE_UUID_1);
    assertFalse(FIXTURE_UUID_1.equals(null));
  }
}
