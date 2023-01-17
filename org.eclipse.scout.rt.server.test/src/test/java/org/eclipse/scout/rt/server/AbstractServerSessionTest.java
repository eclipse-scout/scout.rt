/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.scout.rt.platform.serialization.IObjectSerializer;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.platform.util.ScoutAssert;
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
  }

  @Test
  public void testDeserializeLoadedSession() throws IOException, ClassNotFoundException {
    m_testSession.start(UUID.randomUUID().toString());
    byte[] serializedSession = m_objs.serialize(m_testSession);
    IServerSession deserializedSession = m_objs.deserialize(serializedSession, IServerSession.class);
    assertSessionsEquals(m_testSession, deserializedSession);
  }

  private void assertSessionsEquals(IServerSession expected, IServerSession actual) {
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getUserId(), actual.getUserId());
    ScoutAssert.assertListEquals(expected.getSharedVariableMap().entrySet(), actual.getSharedVariableMap().entrySet());
  }

}
