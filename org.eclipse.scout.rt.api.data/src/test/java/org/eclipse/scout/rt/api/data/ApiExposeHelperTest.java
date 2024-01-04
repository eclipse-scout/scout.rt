/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.security.ISecurityProvider;
import org.junit.Test;

public class ApiExposeHelperTest {
  @Test
  public void testHasApiExposedAnnotation() {
    ApiExposeHelper helper = BEANS.get(ApiExposeHelper.class);
    IBean<ApiExposedBeanFixture> beanFixture = BEANS.getBeanManager().getBean(ApiExposedBeanFixture.class);
    ApiExposedFixture fixture = new ApiExposedFixture();
    assertTrue(helper.hasApiExposedAnnotation(fixture));
    assertTrue(helper.hasApiExposedAnnotation(beanFixture));
    assertTrue(helper.hasApiExposedAnnotation(ApiExposedFixture.class));

    IBean<ISecurityProvider> unexposedBean = BEANS.getBeanManager().getBean(ISecurityProvider.class);
    assertFalse(helper.hasApiExposedAnnotation(unexposedBean));
    assertFalse(helper.hasApiExposedAnnotation(new ApiExposeHelperTest()));
    assertFalse(helper.hasApiExposedAnnotation(ApiExposeHelperTest.class));
    assertFalse(helper.hasApiExposedAnnotation((IBean<?>) null));
    assertFalse(helper.hasApiExposedAnnotation((Class<?>) null));
    assertFalse(helper.hasApiExposedAnnotation((Object) null));
  }

  @Test
  public void testObjectTypeOf() {
    ApiExposeHelper helper = BEANS.get(ApiExposeHelper.class);
    ApiExposedFixture fixture = new ApiExposedFixture();
    assertEquals("test2", helper.objectTypeOf(fixture));
    assertEquals("test2", helper.objectTypeOf(ApiExposedFixture.class));

    assertNull(helper.objectTypeOf(new ApiExposeHelperTest()));
    assertNull(helper.objectTypeOf(ApiExposeHelperTest.class));
    assertNull(helper.objectTypeOf(null));
    assertNull(helper.objectTypeOf((Object) null));
    assertNull(helper.objectTypeOf(ApiExposedEmptyFixture.class));
  }

  @Test
  public void testFieldNameOf() {
    ApiExposeHelper helper = BEANS.get(ApiExposeHelper.class);
    ApiExposedFixture fixture = new ApiExposedFixture();
    assertEquals("field2", helper.fieldNameOf(fixture));
    assertEquals("field2", helper.fieldNameOf(ApiExposedFixture.class));

    assertNull(helper.fieldNameOf(new ApiExposeHelperTest()));
    assertNull(helper.fieldNameOf(ApiExposeHelperTest.class));
    assertNull(helper.fieldNameOf(null));
    assertNull(helper.fieldNameOf((Object) null));
    assertNull(helper.fieldNameOf(ApiExposedEmptyFixture.class));
  }

  @Test
  public void testSetObjectTypeToDo() {
    ApiExposeHelper helper = BEANS.get(ApiExposeHelper.class);

    // null safety
    helper.setObjectTypeToDo(null, null);
    helper.setObjectTypeToDo((Object) null, null);

    // nothing is transferred if no annotation present
    DoEntity emptyBean = BEANS.get(DoEntity.class);
    helper.setObjectTypeToDo(new ApiExposeHelperTest(), emptyBean);
    assertNull(emptyBean.get(ApiExposeHelper.OBJECT_TYPE_ATTRIBUTE_NAME, String.class));

    // value is transferred from annotation to DO
    emptyBean = BEANS.get(DoEntity.class);
    helper.setObjectTypeToDo(new ApiExposedFixture(), emptyBean);
    assertEquals("test2", emptyBean.get(ApiExposeHelper.OBJECT_TYPE_ATTRIBUTE_NAME, String.class));

    // empty value is ignored
    emptyBean = BEANS.get(DoEntity.class);
    helper.setObjectTypeToDo(ApiExposedEmptyFixture.class, emptyBean);
    assertNull(emptyBean.get(ApiExposeHelper.OBJECT_TYPE_ATTRIBUTE_NAME, String.class));

    // custom values are preserved
    DoEntity beanWithCustomValue = BEANS.get(DoEntity.class);
    String customValue = "customValue";
    beanWithCustomValue.put(ApiExposeHelper.OBJECT_TYPE_ATTRIBUTE_NAME, customValue);
    helper.setObjectTypeToDo(ApiExposedFixture.class, beanWithCustomValue);
    assertEquals(customValue, beanWithCustomValue.get(ApiExposeHelper.OBJECT_TYPE_ATTRIBUTE_NAME, String.class));
  }

  @Bean
  @ApiExposed
  @FieldName("field")
  @ObjectType("test")
  public static class ApiExposedBeanFixture {

  }

  @ApiExposed
  @FieldName("field2")
  @ObjectType("test2")
  public static class ApiExposedFixture {

  }

  @ApiExposed
  @FieldName("")
  @ObjectType("")
  public static class ApiExposedEmptyFixture {

  }
}
