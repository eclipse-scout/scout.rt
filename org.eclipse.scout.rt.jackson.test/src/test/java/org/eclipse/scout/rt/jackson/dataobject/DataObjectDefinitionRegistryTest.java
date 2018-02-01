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
package org.eclipse.scout.rt.jackson.dataobject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.eclipse.scout.rt.jackson.dataobject.fixture.TestComplexEntityDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestDateDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestItemDo;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.dataobject.IValueFormatConstants;
import org.eclipse.scout.rt.platform.dataobject.TypeName;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;

/**
 * Tests for {@link DataObjectDefinitionRegistry}
 */
public class DataObjectDefinitionRegistryTest {

  protected DataObjectDefinitionRegistry m_registry;

  @TypeName("TestBaseFixtureEntity")
  public static abstract class TestBaseFixtureEntityDo extends DoEntity {
  }

  public static class TestFixtureSubclass1Do extends TestBaseFixtureEntityDo {
  }

  public static class TestFixtureSubclass2Do extends TestBaseFixtureEntityDo {
  }

  @Before
  public void before() {
    m_registry = new DataObjectDefinitionRegistry();
    m_registry.registerClass(TestComplexEntityDo.class);
    m_registry.registerClass(TestItemDo.class);
    m_registry.registerClass(TestDateDo.class);
  }

  @Test
  public void testToTypeName() {
    assertEquals("TestItem", m_registry.toTypeName(TestItemDo.class));
    assertEquals("TestComplexEntity", m_registry.toTypeName(TestComplexEntityDo.class));

    // fallback to simple class name
    assertEquals("TestFixtureSubclass1Do", m_registry.toTypeName(TestFixtureSubclass1Do.class));

    assertNull(m_registry.toTypeName(null));
    assertEquals("Object", m_registry.toTypeName(Object.class));
    assertEquals("String", m_registry.toTypeName(String.class));
  }

  @Test
  public void testFromTypeName() {
    assertEquals(TestItemDo.class, m_registry.fromTypeName("TestItem"));
    assertEquals(TestComplexEntityDo.class, m_registry.fromTypeName("TestComplexEntity"));

    assertNull(m_registry.fromTypeName(null));
    assertNull(m_registry.fromTypeName("foo"));

    m_registry.registerClass(TestBaseFixtureEntityDo.class);
    assertNull(m_registry.fromTypeName("TestBaseFixtureEntity"));
  }

  @Test
  public void testGetTypeNameToClassMap() {
    assertEquals(TestItemDo.class, m_registry.getTypeNameToClassMap().get("TestItem"));
    assertEquals(TestComplexEntityDo.class, m_registry.getTypeNameToClassMap().get("TestComplexEntity"));
  }

  @Test
  public void testGetAttributeDescription() throws Exception {
    Optional<DataObjectAttributeDefinition> attributeDescription = m_registry.getAttributeDescription(TestItemDo.class, "id");
    assertEquals("id", attributeDescription.get().getName());
    assertEquals(String.class, attributeDescription.get().getType().getRawClass());
    assertEquals(TestItemDo.class.getMethod("id"), attributeDescription.get().getAccessor());
    assertFalse(attributeDescription.get().getFormatPattern().isPresent());

    assertEquals(IValueFormatConstants.DATE_PATTERN, m_registry.getAttributeDescription(TestDateDo.class, "dateOnlyDoList").get().getFormatPattern().get());
    JavaType type = m_registry.getAttributeDescription(TestDateDo.class, "dateOnlyDoList").get().getType();
    type.getBindings().getTypeParameters().get(0);
    assertEquals(DoList.class, type.getRawClass());
    assertEquals(Date.class, type.getBindings().getTypeParameters().get(0).getRawClass());

    assertFalse(m_registry.getAttributeDescription(TestItemDo.class, "foo").isPresent());
    assertFalse(m_registry.getAttributeDescription(DoEntity.class, "foo").isPresent());
  }

  @Test
  public void testGetAttributesDescription() {
    Map<String, DataObjectAttributeDefinition> attributesDescription = m_registry.getAttributesDescription(TestItemDo.class);
    assertEquals(2, attributesDescription.size());
    assertEquals("id", attributesDescription.get("id").getName());
    assertEquals("stringAttribute", attributesDescription.get("stringAttribute").getName());
  }

  @Test(expected = AssertionException.class)
  public void testRegisterDuplicate() {
    m_registry.registerClass(TestItemDo.class);
    m_registry.registerClass(TestItemDo.class);
  }

  @Test
  public void testJsonAnnotationName() {
    assertEquals("TestItem", m_registry.jsonAnnotationName(TestItemDo.class));
    assertEquals("TestBaseFixtureEntity", m_registry.jsonAnnotationName(TestFixtureSubclass1Do.class));
    assertEquals("Object", m_registry.jsonAnnotationName(Object.class));
  }
}
