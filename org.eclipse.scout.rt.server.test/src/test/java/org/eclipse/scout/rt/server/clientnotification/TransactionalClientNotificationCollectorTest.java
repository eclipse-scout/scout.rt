/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.clientnotification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link TransactionalClientNotificationCollector}
 */
public class TransactionalClientNotificationCollectorTest {
  TransactionalClientNotificationCollector m_collector;

  @Before
  public void before() {
    m_collector = new TransactionalClientNotificationCollector();
  }

  /**
   * Initially it should be empty and active
   */
  @Test
  public void testNewClientNotificationCollector() {
    assertTrue(m_collector.isActive());
    assertTrue(m_collector.values().isEmpty());
  }

  /**
   * The collector is not active anymore, after the values are consumed
   */
  @Test
  public void testNotActiveAnymoreAfterConsumed() {
    m_collector.values();
    assertFalse(m_collector.isActive());
  }

  @Test
  public void testAddAllOnlyExecutedIfActive() {
    m_collector.values();
    boolean added = m_collector.addAll(CollectionUtility.arrayList(Mockito.mock(ClientNotificationMessage.class)));
    assertFalse(added);
    assertTrue(m_collector.values().isEmpty());
  }

  @Test
  public void testMessagesAddedIfActive() throws Exception {
    ClientNotificationMessage mockMessage = Mockito.mock(ClientNotificationMessage.class);
    boolean added = m_collector.addAll(CollectionUtility.arrayList(mockMessage));
    assertTrue(added);
    assertEquals(1, m_collector.values().size());
    assertEquals(mockMessage, m_collector.values().get(0));
  }

}
