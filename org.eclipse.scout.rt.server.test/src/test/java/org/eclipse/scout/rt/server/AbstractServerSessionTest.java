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
import java.util.UUID;

import org.eclipse.scout.rt.platform.serialization.IObjectSerializer;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.eclipse.scout.rt.testing.commons.ScoutAssert;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link AbstractServerSession}
 */
@RunWith(ServerTestRunner.class)
@RunWithServerSession(TestServerSession.class)
@RunWithSubject("john")
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
  public void testDeserializeSession() throws IOException, ClassNotFoundException {
    byte[] serializedSession = m_objs.serialize(m_testSession);
    IServerSession deserializedSession = m_objs.deserialize(serializedSession, IServerSession.class);
    assertSessionsEquals(m_testSession, deserializedSession);
    assertNotNull(deserializedSession.getTexts());
  }

  @Test
  public void testDeserializeLoadedSession() throws IOException, ClassNotFoundException {
    m_testSession.start(UUID.randomUUID().toString());
    byte[] serializedSession = m_objs.serialize(m_testSession);
    IServerSession deserializedSession = m_objs.deserialize(serializedSession, IServerSession.class);
    assertSessionsEquals(m_testSession, deserializedSession);
    assertNotNull(deserializedSession.getTexts());
  }

  private void assertSessionsEquals(IServerSession expected, IServerSession actual) {
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getUserId(), actual.getUserId());
    ScoutAssert.assertListEquals(expected.getSharedVariableMap().entrySet(), actual.getSharedVariableMap().entrySet());
  }

}
