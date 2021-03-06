/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.mom.api.marshaller;

import static org.junit.Assert.*;

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
    BEANS.get(BytesMarshaller.class).marshall(new Object(), new HashMap<>());
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
