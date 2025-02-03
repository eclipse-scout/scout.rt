/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.datachange;

import static org.junit.Assert.*;

import java.util.Collections;

import org.eclipse.scout.rt.platform.util.ChangeStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DataChangeManagerTest {

  private DataChangeManager m_manager;
  private IDataChangeListener m_listener;

  @Before
  public void before() {
    m_manager = new DataChangeManager();
    m_listener = Mockito.mock(IDataChangeListener.class);
  }

  @Test
  public void testRegisterNullListener() {
    m_manager.add(null, false);
    assertEquals(Collections.emptyMap(), m_manager.listAll());

    // unregister
    m_manager.remove(null);
    assertEquals(Collections.emptyMap(), m_manager.listAll());
  }

  @Test
  public void testRegisterTypedListener() {
    m_manager.add(m_listener, true, String.class, Long.class);
    assertEquals(2, m_manager.listAll().size());
    assertEquals(Collections.singletonList(m_listener), m_manager.list(String.class));
    assertEquals(Collections.singletonList(m_listener), m_manager.list(Long.class));

    // unregister with type
    m_manager.remove(m_listener, String.class);
    assertEquals(1, m_manager.listAll().size());
    assertEquals(Collections.singletonList(m_listener), m_manager.list(Long.class));

    // unregister without type
    m_manager.remove(m_listener);
    assertEquals(Collections.emptyMap(), m_manager.listAll());
  }

  @Test
  public void testSetBuffering() {
    // initial false
    assertFalse(m_manager.isBuffering());

    // set to true
    m_manager.setBuffering(true);
    assertTrue(m_manager.isBuffering());

    // again set to true, should not have any side effects
    m_manager.setBuffering(true);
    assertTrue(m_manager.isBuffering());

    // set to false is expected to work directly (i.e. not the same number of false needed as true has been issued)
    m_manager.setBuffering(false);
    assertFalse(m_manager.isBuffering());
  }

  @Test
  public void testEventBuffering() {
    DataChangeEvent event = new DataChangeEvent(String.class, ChangeStatus.UPDATED);

    // add mock listener
    m_manager.add(m_listener, false, String.class);

    // issue data changed, listener is expected to be notified
    m_manager.fireEvent(event);
    Mockito.verify(m_listener).dataChanged(event);
    Mockito.verifyNoMoreInteractions(m_listener);
    Mockito.reset(m_listener);

    // buffer events, issue data change, listener is not expected to be notified
    m_manager.setBuffering(true);
    m_manager.fireEvent(event);
    Mockito.verifyNoMoreInteractions(m_listener);

    // issue same event twice (only one notification is expected in the next part)
    m_manager.fireEvent(event);
    Mockito.verifyNoMoreInteractions(m_listener);

    // disable buffering, only one event is expected
    m_manager.setBuffering(false);
    Mockito.verify(m_listener).dataChanged(event);
    Mockito.verifyNoMoreInteractions(m_listener);
  }
}
