/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import static org.junit.Assert.*;

import java.util.UUID;

import org.eclipse.scout.rt.dataobject.fixture.FixtureStringId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuId;
import org.eclipse.scout.rt.dataobject.fixture.FixtureUuIdWithCustomFromString;
import org.eclipse.scout.rt.dataobject.id.AbstractRootId;
import org.junit.Test;

/**
 * Test cases for {@link AbstractRootId}
 */
public class AbstractRootIdTest {

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
  public void testUnwrapAsString() {
    assertEquals(TEST_UUID.toString(), FIXTURE_UUID_1.unwrapAsString());
  }

  @Test
  public void testEqualsObject() {
    assertEquals(FIXTURE_UUID_1, FIXTURE_UUID_2);
    assertEquals(FIXTURE_UUID_1, FIXTURE_UUID_1);
    //noinspection SimplifiableAssertion
    assertFalse(FIXTURE_UUID_1.equals(null));
    //noinspection EqualsBetweenInconvertibleTypes,SimplifiableAssertion
    assertFalse(FIXTURE_UUID_1.equals(FixtureUuIdWithCustomFromString.create()));
  }

  @Test
  public void testToString() {
    assertEquals("FixtureStringId [mock]", FixtureStringId.of("mock").toString());
  }
}
