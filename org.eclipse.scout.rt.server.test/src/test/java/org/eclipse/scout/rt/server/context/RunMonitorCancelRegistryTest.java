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
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.context.RunMonitorCancelRegistry.IRegistrationHandle;
import org.junit.Before;
import org.junit.Test;

public class RunMonitorCancelRegistryTest {

  private static final String SESSION_ID_1 = "session_1";
  private static final String SESSION_ID_2 = "session_2";

  private RunMonitorCancelRegistry m_registry;

  private RunMonitor m_monitor1 = new RunMonitor();
  private RunMonitor m_monitor2 = new RunMonitor();
  private RunMonitor m_monitor3 = new RunMonitor();

  private IRegistrationHandle m_regHandle1;
  private IRegistrationHandle m_regHandle2;
  private IRegistrationHandle m_regHandle3;

  /**
   * Prepares 3 RunMonitors:<br>
   * session1: monitor1 (requestId=1), monitor2 (requestId=2)<br/>
   * session2: monitor3 (requestId=1)
   */
  @Before
  public void before() {
    m_registry = new RunMonitorCancelRegistry();

    m_monitor1 = new RunMonitor();
    m_monitor2 = new RunMonitor();
    m_monitor3 = new RunMonitor();

    m_regHandle1 = m_registry.register(m_monitor1, SESSION_ID_1, 1L);
    m_regHandle2 = m_registry.register(m_monitor2, SESSION_ID_1, 2L);
    m_regHandle3 = m_registry.register(m_monitor3, SESSION_ID_2, 1L);

    assertEquals(CollectionUtility.hashSet(m_monitor1, m_monitor2, m_monitor3), m_registry.getAll());
    assertEquals(CollectionUtility.arrayList(m_monitor1, m_monitor2), m_registry.getAllBySession(SESSION_ID_1));
    assertEquals(CollectionUtility.arrayList(m_monitor3), m_registry.getAllBySession(SESSION_ID_2));
    assertEquals(CollectionUtility.arrayList(m_monitor1), m_registry.getAllBySessionIdAndRequestId(SESSION_ID_1, 1L));
    assertEquals(CollectionUtility.arrayList(m_monitor2), m_registry.getAllBySessionIdAndRequestId(SESSION_ID_1, 2L));
    assertEquals(CollectionUtility.arrayList(m_monitor3), m_registry.getAllBySessionIdAndRequestId(SESSION_ID_2, 1L));
  }

  @Test
  public void testUnregister() {
    // Unregister monitor2
    m_regHandle2.unregister();

    assertEquals(CollectionUtility.hashSet(m_monitor1, m_monitor3), m_registry.getAll());
    assertEquals(CollectionUtility.arrayList(m_monitor1), m_registry.getAllBySession(SESSION_ID_1));
    assertEquals(CollectionUtility.arrayList(m_monitor3), m_registry.getAllBySession(SESSION_ID_2));
    assertEquals(CollectionUtility.arrayList(m_monitor1), m_registry.getAllBySessionIdAndRequestId(SESSION_ID_1, 1L));
    assertTrue(m_registry.getAllBySessionIdAndRequestId(SESSION_ID_1, 2L).isEmpty());
    assertEquals(CollectionUtility.arrayList(m_monitor3), m_registry.getAllBySessionIdAndRequestId(SESSION_ID_2, 1L));

    // Unregister monitor1
    m_regHandle1.unregister();

    assertEquals(CollectionUtility.hashSet(m_monitor3), m_registry.getAll());
    assertTrue(m_registry.getAllBySession(SESSION_ID_1).isEmpty());
    assertEquals(CollectionUtility.arrayList(m_monitor3), m_registry.getAllBySession(SESSION_ID_2));
    assertTrue(m_registry.getAllBySessionIdAndRequestId(SESSION_ID_1, 1L).isEmpty());
    assertTrue(m_registry.getAllBySessionIdAndRequestId(SESSION_ID_1, 2L).isEmpty());
    assertEquals(CollectionUtility.arrayList(m_monitor3), m_registry.getAllBySessionIdAndRequestId(SESSION_ID_2, 1L));

    // Unregister monitor3
    m_regHandle3.unregister();

    assertTrue(m_registry.getAll().isEmpty());
    assertTrue(m_registry.getAllBySession(SESSION_ID_1).isEmpty());
    assertTrue(m_registry.getAllBySession(SESSION_ID_2).isEmpty());
    assertTrue(m_registry.getAllBySessionIdAndRequestId(SESSION_ID_1, 1L).isEmpty());
    assertTrue(m_registry.getAllBySessionIdAndRequestId(SESSION_ID_1, 2L).isEmpty());
    assertTrue(m_registry.getAllBySessionIdAndRequestId(SESSION_ID_2, 1L).isEmpty());
  }

  @Test
  public void testCancelBySession() {
    // Cancel session 1
    assertTrue(m_registry.cancelAllBySessionId(SESSION_ID_1));
    assertFalse(m_registry.cancelAllBySessionId(SESSION_ID_1));

    assertEquals(CollectionUtility.hashSet(m_monitor3), m_registry.getAll());
    assertTrue(m_registry.getAllBySession(SESSION_ID_1).isEmpty());
    assertEquals(CollectionUtility.arrayList(m_monitor3), m_registry.getAllBySession(SESSION_ID_2));
    assertTrue(m_registry.getAllBySessionIdAndRequestId(SESSION_ID_1, 1L).isEmpty());
    assertTrue(m_registry.getAllBySessionIdAndRequestId(SESSION_ID_1, 2L).isEmpty());
    assertEquals(CollectionUtility.arrayList(m_monitor3), m_registry.getAllBySessionIdAndRequestId(SESSION_ID_2, 1L));

    // Cancel session 2
    assertTrue(m_registry.cancelAllBySessionId(SESSION_ID_2));
    assertFalse(m_registry.cancelAllBySessionId(SESSION_ID_2));

    assertTrue(m_registry.getAll().isEmpty());
    assertTrue(m_registry.getAllBySession(SESSION_ID_1).isEmpty());
    assertTrue(m_registry.getAllBySession(SESSION_ID_2).isEmpty());
    assertTrue(m_registry.getAllBySessionIdAndRequestId(SESSION_ID_1, 1L).isEmpty());
    assertTrue(m_registry.getAllBySessionIdAndRequestId(SESSION_ID_1, 2L).isEmpty());
    assertTrue(m_registry.getAllBySessionIdAndRequestId(SESSION_ID_2, 1L).isEmpty());
  }

  @Test
  public void testCancelBySessionAndRequestId() {
    // Cancel sessionId=1, requestId=1
    assertTrue(m_registry.cancelAllBySessionIdAndRequestId(SESSION_ID_1, 1));
    assertFalse(m_registry.cancelAllBySessionIdAndRequestId(SESSION_ID_1, 1));

    assertEquals(CollectionUtility.hashSet(m_monitor2, m_monitor3), m_registry.getAll());
    assertEquals(CollectionUtility.arrayList(m_monitor2), m_registry.getAllBySession(SESSION_ID_1));
    assertEquals(CollectionUtility.arrayList(m_monitor3), m_registry.getAllBySession(SESSION_ID_2));
    assertTrue(m_registry.getAllBySessionIdAndRequestId(SESSION_ID_1, 1L).isEmpty());
    assertEquals(CollectionUtility.arrayList(m_monitor2), m_registry.getAllBySessionIdAndRequestId(SESSION_ID_1, 2L));
    assertEquals(CollectionUtility.arrayList(m_monitor3), m_registry.getAllBySessionIdAndRequestId(SESSION_ID_2, 1L));

    // Cancel sessionId=2
    assertTrue(m_registry.cancelAllBySessionId(SESSION_ID_2));
    assertFalse(m_registry.cancelAllBySessionId(SESSION_ID_2));

    assertEquals(CollectionUtility.hashSet(m_monitor2), m_registry.getAll());
    assertEquals(CollectionUtility.arrayList(m_monitor2), m_registry.getAllBySession(SESSION_ID_1));
    assertTrue(m_registry.getAllBySession(SESSION_ID_2).isEmpty());
    assertTrue(m_registry.getAllBySessionIdAndRequestId(SESSION_ID_1, 1L).isEmpty());
    assertEquals(CollectionUtility.arrayList(m_monitor2), m_registry.getAllBySessionIdAndRequestId(SESSION_ID_1, 2L));
    assertTrue(m_registry.getAllBySessionIdAndRequestId(SESSION_ID_2, 1L).isEmpty());

    // Cancel sessionId=1, requestId=2
    assertTrue(m_registry.cancelAllBySessionIdAndRequestId(SESSION_ID_1, 2));
    assertFalse(m_registry.cancelAllBySessionIdAndRequestId(SESSION_ID_1, 2));

    assertTrue(m_registry.getAll().isEmpty());
    assertTrue(m_registry.getAllBySession(SESSION_ID_1).isEmpty());
    assertTrue(m_registry.getAllBySession(SESSION_ID_2).isEmpty());
    assertTrue(m_registry.getAllBySessionIdAndRequestId(SESSION_ID_1, 1L).isEmpty());
    assertTrue(m_registry.getAllBySessionIdAndRequestId(SESSION_ID_1, 2L).isEmpty());
    assertTrue(m_registry.getAllBySessionIdAndRequestId(SESSION_ID_2, 1L).isEmpty());
  }

  @Test
  public void testCancelUnknownMonitor() {
    assertFalse(m_registry.cancelAllBySessionIdAndRequestId(SESSION_ID_1, 3));

    assertEquals(CollectionUtility.hashSet(m_monitor1, m_monitor2, m_monitor3), m_registry.getAll());
    assertEquals(CollectionUtility.arrayList(m_monitor1, m_monitor2), m_registry.getAllBySession(SESSION_ID_1));
    assertEquals(CollectionUtility.arrayList(m_monitor3), m_registry.getAllBySession(SESSION_ID_2));
    assertEquals(CollectionUtility.arrayList(m_monitor1), m_registry.getAllBySessionIdAndRequestId(SESSION_ID_1, 1L));
    assertEquals(CollectionUtility.arrayList(m_monitor2), m_registry.getAllBySessionIdAndRequestId(SESSION_ID_1, 2L));
    assertEquals(CollectionUtility.arrayList(m_monitor3), m_registry.getAllBySessionIdAndRequestId(SESSION_ID_2, 1L));
  }
}
