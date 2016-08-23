package org.eclipse.scout.rt.mom.api.marshaller;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.junit.Test;

public class ObjectMarshallerTest {

  @Test
  public void test() {
    ObjectMarshaller marshaller = BEANS.get(ObjectMarshaller.class);

    Map<String, String> context = new HashMap<>();
    assertEquals("ABC", marshaller.unmarshall(marshaller.marshall("ABC", context), context));

    context = new HashMap<>();
    assertThat(marshaller.unmarshall(marshaller.marshall(new TestObject(), context), context), instanceOf(TestObject.class));

    context = new HashMap<>();
    TestObject testee = new TestObject();
    assertThat((TestObject) marshaller.unmarshall(marshaller.marshall(testee, context), context), is(equalTo(testee)));
  }

  @Test
  public void testNotSerializableObject() {
    Map<String, String> context = new HashMap<>();
    try {
      BEANS.get(ObjectMarshaller.class).marshall(new Object(), context);
      fail("NotSerializableException expected");
    }
    catch (Exception e) {
      assertThat(e.getCause(), is(instanceOf(NotSerializableException.class)));
    }
  }

  @Test
  public void testEmpty() {
    ObjectMarshaller marshaller = BEANS.get(ObjectMarshaller.class);
    Map<String, String> context = new HashMap<>();

    byte[] bytes = (byte[]) marshaller.marshall("", context);
    assertEquals("", marshaller.unmarshall(bytes, context));
  }

  @Test
  public void testNull() {
    ObjectMarshaller marshaller = BEANS.get(ObjectMarshaller.class);
    Map<String, String> context = new HashMap<>();

    byte[] bytes = (byte[]) marshaller.marshall(null, context);
    assertEquals(null, marshaller.unmarshall(bytes, context));
  }

  public static class TestObject implements Serializable {

    private static final long serialVersionUID = 1L;

    public String m_field1;
    public int m_field2;

    public TestObject withField1(String field1) {
      m_field1 = field1;
      return this;
    }

    public TestObject withField2(int field2) {
      m_field2 = field2;
      return this;
    }

    @Override
    public String toString() {
      return "TestObject.toString()";
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }

      if (obj == null) {
        return false;
      }

      if (obj.getClass() != TestObject.class) {
        return false;
      }

      TestObject o = (TestObject) obj;

      return CompareUtility.equals(o.m_field1, m_field1) && CompareUtility.equals(o.m_field2, m_field2);
    }
  }
}
