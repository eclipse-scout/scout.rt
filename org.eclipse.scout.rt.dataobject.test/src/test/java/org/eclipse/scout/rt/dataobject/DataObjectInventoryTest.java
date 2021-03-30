/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.eclipse.scout.rt.dataobject.fixture.DataObjectFixtureTypeVersions.DataObjectFixture_1_0_0;
import org.eclipse.scout.rt.dataobject.fixture.DataObjectFixtureTypeVersions.DataObjectFixture_1_0_0_034;
import org.eclipse.scout.rt.dataobject.fixture.DataObjectFixtureTypeVersions.DataObjectFixture_1_0_0_Duplicate;
import org.eclipse.scout.rt.dataobject.fixture.DataObjectFixtureTypeVersions.DataObjectFixture_No_Version;
import org.eclipse.scout.rt.dataobject.fixture.DataObjectFixtureTypeVersions.NonRegisteredNamespaceFixture_1_0_0;
import org.eclipse.scout.rt.dataobject.fixture.DataObjectProjectFixtureTypeVersions.DataObjectProjectFixture_1_2_3_004;
import org.eclipse.scout.rt.dataobject.fixture.DateFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.EntityFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.EntityFixtureInvalidTypeNameDo;
import org.eclipse.scout.rt.dataobject.fixture.OtherEntityFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.ProjectFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.ProjectSubFixtureDo;
import org.eclipse.scout.rt.dataobject.fixture.ScoutFixtureDo;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Various tests for {@link DataObjectInventory}
 */
@RunWith(PlatformTestRunner.class)
public class DataObjectInventoryTest {

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
    m_inventory.registerClassByTypeName(EntityFixtureDo.class);
    m_inventory.registerClassByTypeName(OtherEntityFixtureDo.class);
    m_inventory.registerClassByTypeName(DateFixtureDo.class);
    m_inventory.registerClassByTypeName(TestFixtureEntityDo.class);
    m_inventory.registerClassByTypeName(EntityFixtureInvalidTypeNameDo.class);
  }

  @Test
  public void testValidateTypeVersionImplementors() {
    m_inventory.validateTypeVersionImplementors(); // no exception

    BeanTestingHelper beanTestingHelper = BEANS.get(BeanTestingHelper.class);
    IBean<Object> fixtureNoVersionBean = beanTestingHelper.registerBean(new BeanMetaData(DataObjectFixture_No_Version.class));
    try {
      assertThrows(AssertionException.class, () -> m_inventory.validateTypeVersionImplementors());
    }
    finally {
      beanTestingHelper.unregisterBean(fixtureNoVersionBean);
    }

    IBean<Object> fixtureNoRegisteredNamespaceBean = beanTestingHelper.registerBean(new BeanMetaData(NonRegisteredNamespaceFixture_1_0_0.class));
    try {
      assertThrows(AssertionException.class, () -> m_inventory.validateTypeVersionImplementors());
    }
    finally {
      beanTestingHelper.unregisterBean(fixtureNoRegisteredNamespaceBean);
    }

    IBean<Object> fixtureDuplicateVersionBean = beanTestingHelper.registerBean(new BeanMetaData(DataObjectFixture_1_0_0_Duplicate.class));
    try {
      assertThrows(AssertionException.class, () -> m_inventory.validateTypeVersionImplementors());
    }
    finally {
      beanTestingHelper.unregisterBean(fixtureDuplicateVersionBean);
    }
  }

  @Test
  public void testToTypeName() {
    assertEquals("EntityFixture", m_inventory.toTypeName(EntityFixtureDo.class));
    assertEquals("OtherEntityFixture", m_inventory.toTypeName(OtherEntityFixtureDo.class));
    assertEquals("DateFixture", m_inventory.toTypeName(DateFixtureDo.class));

    assertNull(m_inventory.toTypeName(EntityFixtureInvalidTypeNameDo.class));
    assertNull(m_inventory.toTypeName(TestFixtureSubclass1Do.class));
    assertNull(m_inventory.toTypeName(null));
    assertNull(m_inventory.toTypeName(Object.class));
    assertNull(m_inventory.toTypeName(String.class));
  }

  @Test
  public void testFromTypeName() {
    assertEquals(EntityFixtureDo.class, m_inventory.fromTypeName("EntityFixture"));
    assertEquals(OtherEntityFixtureDo.class, m_inventory.fromTypeName("OtherEntityFixture"));

    assertNull(m_inventory.fromTypeName(null));
    assertNull(m_inventory.fromTypeName("foo"));

    m_inventory.registerClassByTypeName(TestBaseFixtureEntityDo.class);
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
    assertTrue(attributeDescription.isPresent());
    assertEquals("id", attributeDescription.get().getName());
    assertEquals(DoValue.class, attributeDescription.get().getType().getRawType());
    assertEquals(String.class, attributeDescription.get().getType().getActualTypeArguments()[0]);
    assertEquals(EntityFixtureDo.class.getMethod("id"), attributeDescription.get().getAccessor());
    assertFalse(attributeDescription.get().getFormatPattern().isPresent());

    attributeDescription = m_inventory.getAttributeDescription(EntityFixtureDo.class, "otherEntities");
    assertTrue(attributeDescription.isPresent());
    assertEquals("otherEntities", attributeDescription.get().getName());
    assertEquals(DoList.class, attributeDescription.get().getType().getRawType());
    assertEquals(OtherEntityFixtureDo.class, attributeDescription.get().getType().getActualTypeArguments()[0]);
    assertEquals(EntityFixtureDo.class.getMethod("otherEntities"), attributeDescription.get().getAccessor());
    assertFalse(attributeDescription.get().getFormatPattern().isPresent());

    attributeDescription = m_inventory.getAttributeDescription(DateFixtureDo.class, "date");
    assertTrue(attributeDescription.isPresent());
    assertEquals("date", attributeDescription.get().getName());
    assertEquals(DoValue.class, attributeDescription.get().getType().getRawType());
    assertEquals(Date.class, attributeDescription.get().getType().getActualTypeArguments()[0]);
    assertEquals(DateFixtureDo.class.getMethod("date"), attributeDescription.get().getAccessor());
    assertEquals(IValueFormatConstants.DATE_PATTERN, attributeDescription.get().getFormatPattern().get());

    attributeDescription = m_inventory.getAttributeDescription(DateFixtureDo.class, "list");
    assertTrue(attributeDescription.isPresent());
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
    assertEquals(5, attributesDescription.size());
    assertEquals("id", attributesDescription.get("id").getName());
    assertEquals("otherEntity", attributesDescription.get("otherEntity").getName());
    assertEquals("otherEntities", attributesDescription.get("otherEntities").getName());
    assertEquals("otherEntitiesList", attributesDescription.get("otherEntitiesList").getName());
    assertEquals("otherEntitiesMap", attributesDescription.get("otherEntitiesMap").getName());
  }

  @Test(expected = AssertionException.class)
  public void testRegisterDuplicateTypeName() {
    m_inventory.registerClassByTypeName(EntityFixtureDo.class);
    m_inventory.registerClassByTypeName(EntityFixtureDo.class);
  }

  @Test
  public void testResolveTypeName() {
    assertEquals("EntityFixture", m_inventory.resolveTypeName(EntityFixtureDo.class));
    assertEquals("TestBaseFixtureEntity", m_inventory.resolveTypeName(TestFixtureSubclass1Do.class));
    assertEquals("", m_inventory.resolveTypeName(EntityFixtureInvalidTypeNameDo.class));

    assertNull(m_inventory.resolveTypeName(Object.class));
  }

  @Test
  public void testResolveTypeVersionClass() {
    assertNull(m_inventory.resolveTypeVersionClass(EntityFixtureDo.class));
    assertEquals(DataObjectFixture_1_0_0.class, m_inventory.resolveTypeVersionClass(OtherEntityFixtureDo.class));
    assertNull(m_inventory.resolveTypeVersionClass(Object.class));

    m_inventory.registerClassByTypeVersion(ScoutFixtureDo.class);
    assertEquals(DataObjectFixture_1_0_0_034.VERSION, m_inventory.getTypeVersion(ScoutFixtureDo.class));

    m_inventory.registerClassByTypeVersion(ProjectFixtureDo.class);
    assertEquals(DataObjectProjectFixture_1_2_3_004.VERSION, m_inventory.getTypeVersion(ProjectFixtureDo.class));
    assertNull(m_inventory.getTypeVersion(ProjectSubFixtureDo.class));
  }

  @Test(expected = AssertionException.class)
  public void testRegisterDuplicateTypeVersion() {
    m_inventory.registerClassByTypeVersion(ScoutFixtureDo.class);
    m_inventory.registerClassByTypeVersion(ScoutFixtureDo.class);
  }

  /**
   * Test for {@link DataObjectInventory#init()} based on class inventory
   */
  @Test
  public void testInit() {
    DataObjectInventory inv = BEANS.get(DataObjectInventory.class);
    assertEquals("ScoutFixture", inv.toTypeName(ScoutFixtureDo.class));
    assertEquals("ScoutFixture", inv.toTypeName(ProjectFixtureDo.class));
    assertEquals("ScoutFixture", inv.toTypeName(ProjectSubFixtureDo.class));

    assertEquals(DataObjectFixture_1_0_0_034.VERSION, inv.getTypeVersion(ScoutFixtureDo.class));
    assertEquals(DataObjectProjectFixture_1_2_3_004.VERSION, inv.getTypeVersion(ProjectFixtureDo.class));

    assertNull(inv.getTypeVersion(ProjectSubFixtureDo.class));
  }
}
