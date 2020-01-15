/*******************************************************************************
 * Copyright (c) 2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.jms;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Message;

import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.JsonDataObjectMarshaller;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.dataobject.DoEntityBuilder;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.junit.Test;
import org.mockito.Mockito;

public class JmsMessageReaderTest {

  @Test
  public void testReadContext() throws Exception {
    testReadContext(null, new HashMap<>());
    testReadContext("{}", new HashMap<>());
    testReadContext(BEANS.get(DoEntityBuilder.class).put("attribute", "foo").buildString(), CollectionUtility.hashMap(ImmutablePair.of("attribute", "foo")));

    // context is Map<String, String>, other JSON-serializable types are not expected but supported
    testReadContext(BEANS.get(DoEntityBuilder.class).put("attribute", 42).buildString(), CollectionUtility.hashMap(ImmutablePair.of("attribute", 42)));
    testReadContext(BEANS.get(DoEntityBuilder.class).put("attribute", Boolean.TRUE).buildString(), CollectionUtility.hashMap(ImmutablePair.of("attribute", Boolean.TRUE)));
  }

  protected void testReadContext(String contextJson, Map<Object, Object> expected) throws Exception {
    Message message = Mockito.mock(Message.class);
    Mockito.when(message.getStringProperty(IJmsMomProperties.JMS_PROP_MARSHALLER_CONTEXT)).thenReturn(contextJson);

    IMarshaller marshaller = BEANS.get(JsonDataObjectMarshaller.class);
    JmsMessageReader<String> reader = JmsMessageReader.newInstance(message, marshaller);
    assertEquals(expected, reader.m_marshallerContext);
  }
}
