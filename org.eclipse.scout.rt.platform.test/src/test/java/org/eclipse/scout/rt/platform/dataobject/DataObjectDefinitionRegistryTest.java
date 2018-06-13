/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.dataobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.eclipse.scout.rt.platform.dataobject.fixture.DateFixtureDo;
import org.eclipse.scout.rt.platform.dataobject.fixture.EntityFixtureDo;
import org.eclipse.scout.rt.platform.dataobject.fixture.OtherEntityFixtureDo;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link DataObjectInventory}
 */
public class DataObjectDefinitionRegistryTest {

  protected DataObjectInventory m_inventory;

  @TypeName("TestBaseFixtureEntity")
  public static abstract class TestBaseFixtureEntityDo extends DoEntity {
  }

  public static class TestFixtureSubclass1Do extends TestBaseFixtureEntityDo {
  }

  public static class TestFixtureSubclass2Do extends TestBaseFixtureEntityDo {
  }

  @TypeName("TestFixtureEntity")
  public static abstract class TestFixtureEntityDo extends DoEntity {
    public DoValue<String> id() {
      return doValue("id");
    }

    // static attribute definitions should be ignored
    public static DoValue<String> idStatic() {
      return new DoValue<>();
    }
  }

  @Before
  public void before() {
    m_inventory = new DataObjectInventory();
    m_inventory.registerClass(EntityFixtureDo.class);
    m_inventory.registerClass(OtherEntityFixtureDo.class);
    m_inventory.registerClass(DateFixtureDo.class);
    m_inventory.registerClass(TestFixtureEntityDo.class);
  }

  @Test
  public void testToTypeName() {
    assertEquals("EntityFixture", m_inventory.toTypeName(EntityFixtureDo.class));
    assertEquals("OtherEntityFixture", m_inventory.toTypeName(OtherEntityFixtureDo.class));
    assertEquals("DateFixture", m_inventory.toTypeName(DateFixtureDo.class));

    // fallback to simple class name
    assertEquals("TestFixtureSubclass1Do", m_inventory.toTypeName(TestFixtureSubclass1Do.class));

    assertNull(m_inventory.toTypeName(null));
    assertEquals("Object", m_inventory.toTypeName(Object.class));
    assertEquals("String", m_inventory.toTypeName(String.class));
  }

  @Test
  public void testFromTypeName() {
    assertEquals(EntityFixtureDo.class, m_inventory.fromTypeName("EntityFixture"));
    assertEquals(OtherEntityFixtureDo.class, m_inventory.fromTypeName("OtherEntityFixture"));

    assertNull(m_inventory.fromTypeName(null));
    assertNull(m_inventory.fromTypeName("foo"));

    m_inventory.registerClass(TestBaseFixtureEntityDo.class);
    assertNull(m_inventory.fromTypeName("TestBaseFixtureEntity"));
  }

  @Test
  public void testGetTypeNameToClassMapItems() {
    assertEquals(EntityFixtureDo.class, m_inventory.getTypeNameToClassMap().get("EntityFixture"));
    assertEquals(OtherEntityFixtureDo.class, m_inventory.getTypeNameToClassMap().get("OtherEntityFixture"));
  }

  @Test
  public void testGetTypeNameToClassMapSize() {
    assertEquals(4, m_inventory.getTypeNameToClassMap().size());
  }

  @Test
  public void testGetAttributeDescription() throws Exception {
    Optional<DataObjectAttributeDescriptor> attributeDescription = m_inventory.getAttributeDescription(EntityFixtureDo.class, "id");
    assertEquals("id", attributeDescription.get().getName());
    assertEquals(DoValue.class, attributeDescription.get().getType().getRawType());
    assertEquals(String.class, attributeDescription.get().getType().getActualTypeArguments()[0]);
    assertEquals(EntityFixtureDo.class.getMethod("id"), attributeDescription.get().getAccessor());
    assertFalse(attributeDescription.get().getFormatPattern().isPresent());

    attributeDescription = m_inventory.getAttributeDescription(EntityFixtureDo.class, "otherEntities");
    assertEquals("otherEntities", attributeDescription.get().getName());
    assertEquals(DoList.class, attributeDescription.get().getType().getRawType());
    assertEquals(OtherEntityFixtureDo.class, attributeDescription.get().getType().getActualTypeArguments()[0]);
    assertEquals(EntityFixtureDo.class.getMethod("otherEntities"), attributeDescription.get().getAccessor());
    assertFalse(attributeDescription.get().getFormatPattern().isPresent());

    attributeDescription = m_inventory.getAttributeDescription(DateFixtureDo.class, "date");
    assertEquals("date", attributeDescription.get().getName());
    assertEquals(DoValue.class, attributeDescription.get().getType().getRawType());
    assertEquals(Date.class, attributeDescription.get().getType().getActualTypeArguments()[0]);
    assertEquals(DateFixtureDo.class.getMethod("date"), attributeDescription.get().getAccessor());
    assertEquals(IValueFormatConstants.DATE_PATTERN, attributeDescription.get().getFormatPattern().get());

    attributeDescription = m_inventory.getAttributeDescription(DateFixtureDo.class, "list");
    assertEquals("list", attributeDescription.get().getName());
    assertEquals(DoList.class, attributeDescription.get().getType().getRawType());
    assertEquals(Integer.class, attributeDescription.get().getType().getActualTypeArguments()[0]);
    assertEquals(DateFixtureDo.class.getMethod("_list"), attributeDescription.get().getAccessor());
    assertFalse(attributeDescription.get().getFormatPattern().isPresent());

    assertFalse(m_inventory.getAttributeDescription(EntityFixtureDo.class, "foo").isPresent());
    assertFalse(m_inventory.getAttributeDescription(DoEntity.class, "foo").isPresent());

    attributeDescription = m_inventory.getAttributeDescription(TestFixtureEntityDo.class, "id");
    assertTrue(attributeDescription.isPresent());

    attributeDescription = m_inventory.getAttributeDescription(TestFixtureEntityDo.class, "idStatic");
    assertFalse(attributeDescription.isPresent());
  }

  @Test
  public void testGetAttributesDescription() {
    Map<String, DataObjectAttributeDescriptor> attributesDescription = m_inventory.getAttributesDescription(EntityFixtureDo.class);
    assertEquals(2, attributesDescription.size());
    assertEquals("id", attributesDescription.get("id").getName());
    assertEquals("otherEntities", attributesDescription.get("otherEntities").getName());
  }

  @Test(expected = AssertionException.class)
  public void testRegisterDuplicate() {
    m_inventory.registerClass(EntityFixtureDo.class);
    m_inventory.registerClass(EntityFixtureDo.class);
  }

  @Test
  public void testGetTypeName() {
    assertEquals("EntityFixture", m_inventory.getTypeName(EntityFixtureDo.class));
    assertEquals("TestBaseFixtureEntity", m_inventory.getTypeName(TestFixtureSubclass1Do.class));
    assertEquals("Object", m_inventory.getTypeName(Object.class));
  }
}
