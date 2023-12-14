/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.api.marshaller;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Generated;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

public class JsonDataObjectMarshallerTest {

  @Test
  public void test() {
    Map<String, String> context = new HashMap<>();

    IDoEntity data = BEANS.get(DoEntityBuilder.class).put("key", "value").build();
    assertEquals(data, marshallAndUnmarshallAsDataObject(data, context));

    context = new HashMap<>();
    MatcherAssert.assertThat(marshallAndUnmarshallAsDataObject(new JsonMarshallerTestEntity(), context), instanceOf(JsonMarshallerTestEntity.class));

    context = new HashMap<>();
    JsonMarshallerTestEntity testee = new JsonMarshallerTestEntity();
    MatcherAssert.assertThat(marshallAndUnmarshallAsDataObject(testee, context), is(equalTo(testee)));
  }

  @Test
  public void testEmpty() {
    Map<String, String> context = new HashMap<>();

    String jsonText = (String) BEANS.get(JsonDataObjectMarshaller.class).marshall(BEANS.get(DoEntity.class), context);
    assertEquals(BEANS.get(DoEntity.class), BEANS.get(JsonDataObjectMarshaller.class).unmarshall(jsonText, context));
  }

  @Test
  public void testNull() {
    Map<String, String> context = new HashMap<>();

    String jsonText = (String) BEANS.get(JsonDataObjectMarshaller.class).marshall(null, context);
    assertNull(BEANS.get(JsonDataObjectMarshaller.class).unmarshall(jsonText, context));
  }

  @Test
  public void testNonDataObject() {
    Assert.assertThrows(AssertionException.class, () -> BEANS.get(JsonDataObjectMarshaller.class).marshall("foo", null));
  }

  private static Object marshallAndUnmarshallAsDataObject(Object object, Map<String, String> context) {
    JsonDataObjectMarshaller marshaller = BEANS.get(JsonDataObjectMarshaller.class);
    return marshaller.unmarshall(marshaller.marshall(object, context), context);
  }

  @TypeName("scout.JsonDataObjectMarshallerTestEntity")
  public static class JsonMarshallerTestEntity extends DoEntity {

    public DoValue<String> field1() {
      return doValue("field1");
    }

    public DoValue<Integer> field2() {
      return doValue("field2");
    }

    /* **************************************************************************
     * GENERATED CONVENIENCE METHODS
     * *************************************************************************/

    @Generated("DoConvenienceMethodsGenerator")
    public JsonMarshallerTestEntity withField1(String field1) {
      field1().set(field1);
      return this;
    }

    @Generated("DoConvenienceMethodsGenerator")
    public String getField1() {
      return field1().get();
    }

    @Generated("DoConvenienceMethodsGenerator")
    public JsonMarshallerTestEntity withField2(Integer field2) {
      field2().set(field2);
      return this;
    }

    @Generated("DoConvenienceMethodsGenerator")
    public Integer getField2() {
      return field2().get();
    }
  }
}
