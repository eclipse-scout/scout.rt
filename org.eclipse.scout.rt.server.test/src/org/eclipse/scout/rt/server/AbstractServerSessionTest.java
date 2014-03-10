/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.serialization.IObjectSerializer;
import org.eclipse.scout.commons.serialization.SerializationUtility;
import org.eclipse.scout.rt.server.fixture.TestServerSession;
import org.eclipse.scout.rt.server.internal.Activator;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link AbstractServerSession}
 */
public class AbstractServerSessionTest {

  private IObjectSerializer m_objs;
  private IServerSession m_testSession;

  @Before
  public void setup() {
    m_objs = SerializationUtility.createObjectSerializer();
    m_testSession = new TestServerSession();
  }

  @Test
  public void testSerialize() throws IOException {
    m_objs.serialize(m_testSession);
  }

  @Test
  public void testDeserializeSession() throws IOException, ProcessingException, ClassNotFoundException {
    byte[] serializedSession = m_objs.serialize(m_testSession);
    IServerSession deserializedSession = m_objs.deserialize(serializedSession, IServerSession.class);
    assertSessionsEquals(m_testSession, deserializedSession);
    assertNotNull(deserializedSession.getTexts());
  }

  @Test
  public void testDeserializeLoadedSession() throws IOException, ProcessingException, ClassNotFoundException {
    m_testSession.loadSession(Activator.getDefault().getBundle());
    byte[] serializedSession = m_objs.serialize(m_testSession);
    IServerSession deserializedSession = m_objs.deserialize(serializedSession, IServerSession.class);
    assertSessionsEquals(m_testSession, deserializedSession);
    assertNotNull(deserializedSession.getTexts());
  }

  private void assertSessionsEquals(IServerSession expected, IServerSession actual) {
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getLocale(), actual.getLocale());
    assertEquals(expected.getUserAgent(), actual.getUserAgent());
    assertEquals(expected.getUserId(), actual.getUserId());
    assertEquals(expected.getBundle(), actual.getBundle());
    assertEquals(expected.getVirtualSessionId(), actual.getVirtualSessionId());
//   TODO tsw fix serialization
//    ScoutAssert.assertListEquals(expected.getSharedVariableMap().entrySet(), actual.getSharedVariableMap().entrySet());
  }

}
