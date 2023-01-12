/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.enumeration;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.dataobject.enumeration.fixture.AlphabetFixture;
import org.eclipse.scout.rt.dataobject.enumeration.fixture.EmptyEnumNameFixture;
import org.eclipse.scout.rt.dataobject.enumeration.fixture.NoEnumNameFixture;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Various tests for {@link EnumInventory}.
 */
@RunWith(PlatformTestRunner.class)
public class EnumInventoryTest {

  protected EnumInventory m_inventory;

  @Before
  public void before() {
    m_inventory = new EnumInventory();
    m_inventory.registerClass(AlphabetFixture.class);
    m_inventory.registerClass(EmptyEnumNameFixture.class);
    m_inventory.registerClass(NoEnumNameFixture.class);
  }

  @Test
  public void testToEnumName() {
    assertEquals("scout.AlphabetFixture", m_inventory.toEnumName(AlphabetFixture.class));

    assertNull(m_inventory.toEnumName(EmptyEnumNameFixture.class));
    assertNull(m_inventory.toEnumName(NoEnumNameFixture.class));
  }

  @Test
  public void testFromEnumName() {
    assertEquals(AlphabetFixture.class, m_inventory.fromEnumName("scout.AlphabetFixture"));

    assertNull(m_inventory.fromEnumName(null));
    assertNull(m_inventory.fromEnumName("foo"));
  }

  @Test
  public void testGetEnumNameToClassMapItems() {
    assertEquals(AlphabetFixture.class, m_inventory.getEnumNameToClassMap().get("scout.AlphabetFixture"));
  }

  @Test
  public void testGetEnumNameToClassMapSize() {
    assertEquals(1, m_inventory.getEnumNameToClassMap().size());
  }

  @Test(expected = AssertionException.class)
  public void testRegisterDuplicateEnumName() {
    m_inventory.registerClass(AlphabetFixture.class);
    m_inventory.registerClass(AlphabetFixture.class);
  }

  @Test
  public void testResolveEnumName() {
    assertEquals("scout.AlphabetFixture", m_inventory.resolveEnumName(AlphabetFixture.class));
    assertEquals("", m_inventory.resolveEnumName(EmptyEnumNameFixture.class));
    assertNull(m_inventory.resolveEnumName(NoEnumNameFixture.class));

    assertNull(m_inventory.resolveEnumName(Object.class));
  }

  /**
   * Test for {@link EnumInventory#init()} based on class inventory
   */
  @Test
  public void testInit() {
    EnumInventory inv = BEANS.get(EnumInventory.class);
    assertEquals("scout.AlphabetFixture", inv.toEnumName(AlphabetFixture.class));
  }
}
