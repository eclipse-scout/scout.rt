/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.shared.ISession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class RunMonitorCancelRegistryTest {

  private ISession m_session;

  @Before
  public void before() {
    m_session = mockSession();
  }

  @Test
  public void testRegisterAndUnregister() {
    RunMonitor monitor1 = new RunMonitor();
    RunMonitor monitor2 = new RunMonitor();

    RunMonitorCancelRegistry registry = new RunMonitorCancelRegistry();
    registry.register(m_session, 1, monitor1); // register 1. Monitor
    registry.register(m_session, 2, monitor2); // register 2. Monitor

    assertEquals(2, getRunMonitors(m_session).size());
    assertSame(monitor1, getRunMonitors(m_session).get(1L).get());
    assertSame(monitor2, getRunMonitors(m_session).get(2L).get());

    registry.unregister(m_session, 2); // register 2. Monitor
    assertEquals(1, getRunMonitors(m_session).size());
    assertSame(monitor1, getRunMonitors(m_session).get(1L).get());

    registry.unregister(m_session, 1); // register 1. Monitor
    assertNull(getRunMonitors(m_session));
  }

  @Test
  public void testRegisterAndCancelAndUnregister() {
    RunMonitor monitor = new RunMonitor();

    RunMonitorCancelRegistry registry = new RunMonitorCancelRegistry();
    registry.register(m_session, 2, monitor);

    assertEquals(1, getRunMonitors(m_session).size());
    assertSame(monitor, getRunMonitors(m_session).get(2L).get());

    assertTrue(registry.cancel(m_session, 2));
    assertNull(getRunMonitors(m_session));

    registry.unregister(m_session, 2);
    assertNull(getRunMonitors(m_session));
  }

  @Test
  public void testCancelUnknownMonitor() {
    RunMonitor monitor = new RunMonitor();

    RunMonitorCancelRegistry registry = new RunMonitorCancelRegistry();
    registry.register(m_session, 2, monitor);

    assertEquals(1, getRunMonitors(m_session).size());
    assertSame(monitor, getRunMonitors(m_session).get(2L).get());

    assertFalse(registry.cancel(m_session, 3));
    assertEquals(1, getRunMonitors(m_session).size());
    assertSame(monitor, getRunMonitors(m_session).get(2L).get());

    registry.unregister(m_session, 3);
    assertEquals(1, getRunMonitors(m_session).size());
    assertSame(monitor, getRunMonitors(m_session).get(2L).get());
  }

  @Test
  public void testRegisterWithZeroId() {
    RunMonitorCancelRegistry registry = new RunMonitorCancelRegistry();
    registry.register(m_session, 0, new RunMonitor());
    assertNull(getRunMonitors(m_session));
  }

  @Test
  public void testUnregisterWithZeroId() {
    RunMonitor monitor = new RunMonitor();

    RunMonitorCancelRegistry registry = new RunMonitorCancelRegistry();
    registry.register(m_session, 2, monitor);

    registry.unregister(m_session, 0);
    assertEquals(1, getRunMonitors(m_session).size());
    assertSame(monitor, getRunMonitors(m_session).get(2L).get());
  }

  @Test
  public void testCancelWithZeroId() {
    RunMonitor monitor = new RunMonitor();

    RunMonitorCancelRegistry registry = new RunMonitorCancelRegistry();
    registry.register(m_session, 2, monitor);

    registry.cancel(m_session, 0);
    assertEquals(1, getRunMonitors(m_session).size());
    assertSame(monitor, getRunMonitors(m_session).get(2L).get());
  }

  private ISession mockSession() {
    final Map<String, Object> map = new HashMap<>();

    ISession session = mock(ISession.class);

    // mock session.getData(key)
    doAnswer(new Answer<Object>() {

      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        return map.get(invocation.getArguments()[0]);
      }

    }).when(session).getData(anyString());

    // mock session.setDataData(key, value)
    doAnswer(new Answer<Void>() {

      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        Object key = invocation.getArguments()[0];
        Object value = invocation.getArguments()[1];
        if (value == null) {
          map.remove(key);
        }
        else {
          map.put((String) key, value);
        }
        return null;
      }
    }).when(session).setData(anyString(), anyObject());

    return session;
  }

  @SuppressWarnings("unchecked")
  private Map<Long, WeakReference<RunMonitor>> getRunMonitors(ISession session) {
    return (Map<Long, WeakReference<RunMonitor>>) session.getData(RunMonitorCancelRegistry.RUN_MONITORS_KEY);
  }
}
