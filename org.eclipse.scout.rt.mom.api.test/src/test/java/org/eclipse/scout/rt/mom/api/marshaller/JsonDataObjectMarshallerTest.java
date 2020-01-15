/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.api.marshaller;

import static org.eclipse.scout.rt.testing.platform.util.ScoutAssert.assertThrows;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.TypeName;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Test;

public class JsonDataObjectMarshallerTest {

  @Test
  public void test() {
    Map<String, String> context = new HashMap<>();

    DoEntity data = BEANS.get(DoEntityBuilder.class).put("key", "value").build();
    assertEquals(data, marshallAndUnmarshallAsDataObject(data, context));

    context = new HashMap<>();
    assertThat(marshallAndUnmarshallAsDataObject(new JsonMarshallerTestEntity(), context), instanceOf(JsonMarshallerTestEntity.class));

    context = new HashMap<>();
    JsonMarshallerTestEntity testee = new JsonMarshallerTestEntity();
    assertThat((JsonMarshallerTestEntity) marshallAndUnmarshallAsDataObject(testee, context), is(equalTo(testee)));
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
    assertEquals(null, BEANS.get(JsonDataObjectMarshaller.class).unmarshall(jsonText, context));
  }

  @Test
  public void testNonDataObject() {
    assertThrows(AssertionException.class, () -> BEANS.get(JsonDataObjectMarshaller.class).marshall("foo", null));
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
