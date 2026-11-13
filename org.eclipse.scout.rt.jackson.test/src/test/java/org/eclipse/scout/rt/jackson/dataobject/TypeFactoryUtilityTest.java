/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.eclipse.scout.rt.dataobject.DataObjectInventory;
import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestCollectionsDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestItemDo;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.junit.Test;

import tools.jackson.databind.DatabindContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.type.TypeFactory;

public class TypeFactoryUtilityTest {

  @Test
  public void testToJavaType_DoValue() {
    DatabindContext ctxt = spy(DatabindContext.class);
    when(ctxt.getTypeFactory()).thenReturn(TypeFactory.createDefaultInstance());
    ParameterizedType type = BEANS.get(DataObjectInventory.class).getAttributeDescription(TestItemDo.class, "id").get().getType();
    AttributeType attributeType = TypeFactoryUtility.toAttributeType(type, ctxt);
    assertTrue(attributeType.isDoValue());
    assertFalse(attributeType.isDoCollection());
    JavaType jt = attributeType.getJavaType();
    assertEquals(String.class, jt.getRawClass());
  }

  @Test
  public void testToJavaType_List() {
    DatabindContext ctxt = spy(DatabindContext.class);
    when(ctxt.getTypeFactory()).thenReturn(TypeFactory.createDefaultInstance());
    ParameterizedType type = BEANS.get(DataObjectInventory.class).getAttributeDescription(TestCollectionsDo.class, "itemListAttribute").get().getType();
    AttributeType attributeType = TypeFactoryUtility.toAttributeType(type, ctxt);
    assertTrue(attributeType.isDoValue());
    assertFalse(attributeType.isDoCollection());
    JavaType jt = attributeType.getJavaType();
    assertEquals(List.class, jt.getRawClass());
    assertEquals(TestItemDo.class, jt.getBindings().getBoundType(0).getRawClass());
  }

  @Test
  public void testToJavaType_DoList() {
    DatabindContext ctxt = spy(DatabindContext.class);
    when(ctxt.getTypeFactory()).thenReturn(TypeFactory.createDefaultInstance());
    ParameterizedType type = BEANS.get(DataObjectInventory.class).getAttributeDescription(TestCollectionsDo.class, "itemDoListAttribute").get().getType();
    AttributeType attributeType = TypeFactoryUtility.toAttributeType(type, ctxt);
    assertFalse(attributeType.isDoValue());
    assertTrue(attributeType.isDoCollection());
    JavaType jt = attributeType.getJavaType();
    assertEquals(DoList.class, jt.getRawClass());
    assertEquals(TestItemDo.class, jt.getBindings().getBoundType(0).getRawClass());
  }

  @Test
  public void testToJavaType_DoSet() {
    DatabindContext ctxt = spy(DatabindContext.class);
    when(ctxt.getTypeFactory()).thenReturn(TypeFactory.createDefaultInstance());
    ParameterizedType type = BEANS.get(DataObjectInventory.class).getAttributeDescription(TestCollectionsDo.class, "itemDoSetAttribute").get().getType();
    AttributeType attributeType = TypeFactoryUtility.toAttributeType(type, ctxt);
    assertFalse(attributeType.isDoValue());
    assertTrue(attributeType.isDoCollection());
    JavaType jt = attributeType.getJavaType();
    assertEquals(DoSet.class, jt.getRawClass());
    assertEquals(TestItemDo.class, jt.getBindings().getBoundType(0).getRawClass());
  }

  @Test
  public void testToJavaType_DoCollection() {
    DatabindContext ctxt = spy(DatabindContext.class);
    when(ctxt.getTypeFactory()).thenReturn(TypeFactory.createDefaultInstance());
    ParameterizedType type = BEANS.get(DataObjectInventory.class).getAttributeDescription(TestCollectionsDo.class, "itemDoCollectionAttribute").get().getType();
    AttributeType attributeType = TypeFactoryUtility.toAttributeType(type, ctxt);
    assertFalse(attributeType.isDoValue());
    assertTrue(attributeType.isDoCollection());
    JavaType jt = attributeType.getJavaType();
    assertEquals(DoCollection.class, jt.getRawClass());
    assertEquals(TestItemDo.class, jt.getBindings().getBoundType(0).getRawClass());
  }

  @Test(expected = PlatformException.class)
  public void testToJavaType_Invalid() throws Exception {
    DatabindContext ctxt = spy(DatabindContext.class);
    when(ctxt.getTypeFactory()).thenReturn(TypeFactory.createDefaultInstance());
    TypeFactoryUtility.toAttributeType((ParameterizedType) (List.class.getMethod("iterator").getGenericReturnType()), ctxt);
  }
}
