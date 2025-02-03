/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.clientnotification;

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link ClientNotificationCollector}
 */
public class TransactionalClientNotificationCollectorTest {
  ClientNotificationCollector m_collector;

  @Before
  public void before() {
    m_collector = new ClientNotificationCollector();
  }

  /**
   * Initially it should be empty and active
   */
  @Test
  public void testNewClientNotificationCollector() {
    assertTrue(m_collector.isActive());
    assertTrue(m_collector.consume().isEmpty());
  }

  /**
   * The collector is not active anymore, after the values are consumed
   */
  @Test
  public void testNotActiveAnymoreAfterConsumed() {
    m_collector.consume();
    assertFalse(m_collector.isActive());
  }

  @Test
  public void testAddAllOnlyExecutedIfActive() {
    m_collector.consume();
    boolean added = m_collector.addAll(CollectionUtility.arrayList(Mockito.mock(ClientNotificationMessage.class)));
    assertFalse(added);
    assertTrue(m_collector.consume().isEmpty());
  }

  @Test
  public void testMessagesAddedIfActive() {
    ClientNotificationMessage mockMessage = Mockito.mock(ClientNotificationMessage.class);
    boolean added = m_collector.addAll(CollectionUtility.arrayList(mockMessage));
    assertTrue(added);
    List<ClientNotificationMessage> res = m_collector.consume();
    assertEquals(1, res.size());
    assertEquals(mockMessage, res.get(0));
  }
}
