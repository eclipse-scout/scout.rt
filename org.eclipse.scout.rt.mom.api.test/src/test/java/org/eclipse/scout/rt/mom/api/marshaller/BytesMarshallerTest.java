package org.eclipse.scout.rt.mom.api.marshaller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Test;

public class BytesMarshallerTest {

  @Test
  public void test() {
    byte[] bytes = "ABC".getBytes(StandardCharsets.UTF_8);
    BytesMarshaller marshaller = BEANS.get(BytesMarshaller.class);
    Map<String, String> context = new HashMap<>();

    assertEquals(bytes, marshaller.marshall(bytes, context));
    assertEquals(bytes, marshaller.unmarshall(bytes, context));
  }

  @Test(expected = AssertionException.class)
  public void testObject() {
    BEANS.get(BytesMarshaller.class).marshall(new Object(), new HashMap<String, String>());
  }

  @Test
  public void testNull() {
    BytesMarshaller marshaller = BEANS.get(BytesMarshaller.class);
    Map<String, String> context = new HashMap<>();

    assertNull(marshaller.marshall(null, context));
    assertNull(marshaller.unmarshall(null, context));
  }

  @Test
  public void testEmpty() {
    byte[] bytes = new byte[0];

    BytesMarshaller marshaller = BEANS.get(BytesMarshaller.class);
    Map<String, String> context = new HashMap<>();

    assertEquals(0, ((byte[]) marshaller.marshall(bytes, context)).length);
    assertEquals(0, ((byte[]) marshaller.unmarshall(bytes, context)).length);
  }
}
