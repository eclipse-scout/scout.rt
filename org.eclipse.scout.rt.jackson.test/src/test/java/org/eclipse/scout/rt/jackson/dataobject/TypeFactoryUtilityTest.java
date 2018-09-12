/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
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

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.eclipse.scout.rt.jackson.dataobject.fixture.TestCollectionsDo;
import org.eclipse.scout.rt.jackson.dataobject.fixture.TestItemDo;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.dataobject.DataObjectInventory;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;

public class TypeFactoryUtilityTest {

  @Test
  public void testToJavaType_DoValue() {
    ParameterizedType type = BEANS.get(DataObjectInventory.class).getAttributeDescription(TestItemDo.class, "id").get().getType();
    JavaType jt = TypeFactoryUtility.toJavaType(type);
    assertEquals(String.class, jt.getRawClass());
  }

  @Test
  public void testToJavaType_List() {
    ParameterizedType type = BEANS.get(DataObjectInventory.class).getAttributeDescription(TestCollectionsDo.class, "itemListAttribute").get().getType();
    JavaType jt = TypeFactoryUtility.toJavaType(type);
    assertEquals(List.class, jt.getRawClass());
    assertEquals(TestItemDo.class, jt.getBindings().getBoundType(0).getRawClass());
  }

  @Test
  public void testToJavaType_DoList() {
    ParameterizedType type = BEANS.get(DataObjectInventory.class).getAttributeDescription(TestCollectionsDo.class, "itemDoListAttribute").get().getType();
    JavaType jt = TypeFactoryUtility.toJavaType(type);
    assertEquals(DoList.class, jt.getRawClass());
    assertEquals(TestItemDo.class, jt.getBindings().getBoundType(0).getRawClass());
  }

  @Test(expected = PlatformException.class)
  public void testToJavaType_Invalid() throws Exception {
    TypeFactoryUtility.toJavaType((ParameterizedType) (List.class.getMethod("iterator").getGenericReturnType()));
  }
}
