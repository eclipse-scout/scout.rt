/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import static org.junit.Assert.*;

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

import com.fasterxml.jackson.databind.JavaType;

public class TypeFactoryUtilityTest {

  @Test
  public void testToJavaType_DoValue() {
    ParameterizedType type = BEANS.get(DataObjectInventory.class).getAttributeDescription(TestItemDo.class, "id").get().getType();
    AttributeType attributeType = TypeFactoryUtility.toAttributeType(type);
    assertTrue(attributeType.isDoValue());
    assertFalse(attributeType.isDoCollection());
    JavaType jt = attributeType.getJavaType();
    assertEquals(String.class, jt.getRawClass());
  }

  @Test
  public void testToJavaType_List() {
    ParameterizedType type = BEANS.get(DataObjectInventory.class).getAttributeDescription(TestCollectionsDo.class, "itemListAttribute").get().getType();
    AttributeType attributeType = TypeFactoryUtility.toAttributeType(type);
    assertTrue(attributeType.isDoValue());
    assertFalse(attributeType.isDoCollection());
    JavaType jt = attributeType.getJavaType();
    assertEquals(List.class, jt.getRawClass());
    assertEquals(TestItemDo.class, jt.getBindings().getBoundType(0).getRawClass());
  }

  @Test
  public void testToJavaType_DoList() {
    ParameterizedType type = BEANS.get(DataObjectInventory.class).getAttributeDescription(TestCollectionsDo.class, "itemDoListAttribute").get().getType();
    AttributeType attributeType = TypeFactoryUtility.toAttributeType(type);
    assertFalse(attributeType.isDoValue());
    assertTrue(attributeType.isDoCollection());
    JavaType jt = attributeType.getJavaType();
    assertEquals(DoList.class, jt.getRawClass());
    assertEquals(TestItemDo.class, jt.getBindings().getBoundType(0).getRawClass());
  }

  @Test
  public void testToJavaType_DoSet() {
    ParameterizedType type = BEANS.get(DataObjectInventory.class).getAttributeDescription(TestCollectionsDo.class, "itemDoSetAttribute").get().getType();
    AttributeType attributeType = TypeFactoryUtility.toAttributeType(type);
    assertFalse(attributeType.isDoValue());
    assertTrue(attributeType.isDoCollection());
    JavaType jt = attributeType.getJavaType();
    assertEquals(DoSet.class, jt.getRawClass());
    assertEquals(TestItemDo.class, jt.getBindings().getBoundType(0).getRawClass());
  }

  @Test
  public void testToJavaType_DoCollection() {
    ParameterizedType type = BEANS.get(DataObjectInventory.class).getAttributeDescription(TestCollectionsDo.class, "itemDoCollectionAttribute").get().getType();
    AttributeType attributeType = TypeFactoryUtility.toAttributeType(type);
    assertFalse(attributeType.isDoValue());
    assertTrue(attributeType.isDoCollection());
    JavaType jt = attributeType.getJavaType();
    assertEquals(DoCollection.class, jt.getRawClass());
    assertEquals(TestItemDo.class, jt.getBindings().getBoundType(0).getRawClass());
  }

  @Test(expected = PlatformException.class)
  public void testToJavaType_Invalid() throws Exception {
    TypeFactoryUtility.toAttributeType((ParameterizedType) (List.class.getMethod("iterator").getGenericReturnType()));
  }
}
