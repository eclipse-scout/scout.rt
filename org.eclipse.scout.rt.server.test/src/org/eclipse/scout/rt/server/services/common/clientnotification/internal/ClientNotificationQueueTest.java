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
package org.eclipse.scout.rt.server.services.common.clientnotification.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ThreadContext;
import org.eclipse.scout.rt.server.services.common.clientnotification.AllUserFilter;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationFilter;
import org.eclipse.scout.rt.shared.services.common.clientnotification.AbstractClientNotification;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link ClientNotificationQueue}
 */
public class ClientNotificationQueueTest {
  private static final String TEST_ID = "TEST_ID";
  private IClientNotification m_testNotification;
  private ClientNotificationQueue m_clientNotificationQueue;

  private IServerSession m_testServerSession;
  private IServerSession m_testServerSession2;
  private Map<Class, Object> m_threadContextBackup;

  @Before
  public void setup() {
    m_testNotification = createTestNotification(1000);
    m_clientNotificationQueue = new ClientNotificationQueue();
    m_testServerSession = createTestServerSession();
    m_testServerSession2 = createTestServerSession();
    m_threadContextBackup = ThreadContext.backup();
    ThreadContext.putServerSession(m_testServerSession);
  }

  private AbstractServerSession createTestServerSession() {
    return new AbstractServerSession(true) {

      private static final long serialVersionUID = 1L;
    };
  }

  @After
  public void tearDown() {
    ThreadContext.restore(m_threadContextBackup);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullNotificationFails() {
    m_clientNotificationQueue.putNotification(null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFilterFails() {
    m_clientNotificationQueue.putNotification(m_testNotification, null);
  }

  @Test
  public void testPutNotification() throws Exception {
    m_clientNotificationQueue.putNotification(m_testNotification, new AllUserFilter(1000));
    assertTrue(m_clientNotificationQueue.getNextNotifications(0).contains(m_testNotification));
  }

  @Test
  public void testInactiveNotificationNotReceived() throws Exception {
    IClientNotificationFilter inactiveFilter = mock(IClientNotificationFilter.class);
    when(inactiveFilter.isActive()).thenReturn(false);
    when(inactiveFilter.accept()).thenReturn(true);
    m_clientNotificationQueue.putNotification(m_testNotification, inactiveFilter);
    assertFalse(m_clientNotificationQueue.getNextNotifications(0).contains(m_testNotification));
  }

  @Test
  public void testRejectingFilterNotificationNotReceived() throws Exception {
    IClientNotificationFilter inactiveFilter = mock(IClientNotificationFilter.class);
    when(inactiveFilter.isActive()).thenReturn(true);
    when(inactiveFilter.accept()).thenReturn(false);
    m_clientNotificationQueue.putNotification(m_testNotification, inactiveFilter);
    assertFalse(m_clientNotificationQueue.getNextNotifications(0).contains(m_testNotification));
  }

  @Test
  public void testNotificationOnlyConsumedOnce() throws Exception {
    IClientNotificationFilter inactiveFilter = mock(IClientNotificationFilter.class);
    when(inactiveFilter.isActive()).thenReturn(true);
    when(inactiveFilter.accept()).thenReturn(true);
    when(inactiveFilter.isMulticast()).thenReturn(true);
    m_clientNotificationQueue.putNotification(m_testNotification, inactiveFilter);
    m_clientNotificationQueue.ackNotifications(getIds(CollectionUtility.hashSet((m_testNotification))));
    assertFalse(m_clientNotificationQueue.getNextNotifications(0).contains(m_testNotification));
    ThreadContext.putServerSession(m_testServerSession2);
    assertTrue(m_clientNotificationQueue.getNextNotifications(0).contains(m_testNotification));
  }

  @Test
  public void testSingleCastNotificationRemoved() throws Exception {
    IClientNotificationFilter inactiveFilter = mock(IClientNotificationFilter.class);
    when(inactiveFilter.isActive()).thenReturn(true);
    when(inactiveFilter.accept()).thenReturn(true);
    when(inactiveFilter.isMulticast()).thenReturn(false);
    m_clientNotificationQueue.putNotification(m_testNotification, inactiveFilter);
    m_clientNotificationQueue.ackNotifications(getIds(CollectionUtility.hashSet((m_testNotification))));
    assertFalse(m_clientNotificationQueue.getNextNotifications(0).contains(m_testNotification));
    ThreadContext.putServerSession(m_testServerSession2);
    assertFalse(m_clientNotificationQueue.getNextNotifications(0).contains(m_testNotification));
  }

  /**
   * Notifications of the same instance or same type with coalesce <code>true</code> are replaced.
   */
  @Test
  public void testCoalesce() throws Exception {
    m_clientNotificationQueue.putNotification(createTestNotification(1000), new AllUserFilter(1000));
    IClientNotification testNotification2 = createTestNotification2(1000);
    m_clientNotificationQueue.putNotification(testNotification2, new AllUserFilter(1000));
    m_clientNotificationQueue.putNotification(m_testNotification, new AllUserFilter(1000));
    m_clientNotificationQueue.putNotification(m_testNotification, new AllUserFilter(1000));
    assertTrue(m_clientNotificationQueue.getNextNotifications(0).contains(m_testNotification));
    assertTrue(m_clientNotificationQueue.getNextNotifications(0).contains(testNotification2));
    assertEquals(2, m_clientNotificationQueue.getNextNotifications(0).size());
  }

  private Set<String> getIds(Collection<IClientNotification> notifications) {
    HashSet<String> ids = new HashSet<String>();
    for (IClientNotification n : notifications) {
      ids.add(n.getId());
    }
    return ids;
  }

  /**
   * @return notification with {@link #TEST_ID}
   */
  private IClientNotification createTestNotification(long timeout) {
    return new TestClientNotification(timeout);
  }

  /**
   * @return notification with {@link #TEST_ID}
   */
  private IClientNotification createTestNotification2(long timeout) {
    return new TestClientNotification(timeout) {

      private static final long serialVersionUID = 1L;
    };
  }

  private class TestClientNotification extends AbstractClientNotification {

    private static final long serialVersionUID = 1L;

    public TestClientNotification(long timeout) {
      super(timeout);
    }

    @Override
    public boolean coalesce(IClientNotification existingNotification) {
      return true;
    }

    @Override
    public String getId() {
      return "test2";
    }
  }

}
