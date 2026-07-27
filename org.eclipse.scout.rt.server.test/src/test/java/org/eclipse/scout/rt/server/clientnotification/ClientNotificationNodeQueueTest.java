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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.platform.util.date.IDateProvider;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationProperties.NodeQueueCapacity;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationAddress;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.mock.MockConfigPropertyRule;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.date.FixedDateProvider;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link ClientNotificationNodeQueue}
 */
@RunWith(PlatformTestRunner.class)
public class ClientNotificationNodeQueueTest {

  protected static final int MAX_TEST_CAPACITY = 10;
  protected static final FixedDateProvider DATE_PROVIDER = new FixedDateProvider();
  protected static List<IBean<Object>> s_beans = new ArrayList<>();

  protected ClientNotificationNodeQueue m_queue;

  @Rule
  public MockConfigPropertyRule<Integer> m_nodeQueueCapacityPropertyRule = new MockConfigPropertyRule<>(NodeQueueCapacity.class, MAX_TEST_CAPACITY);

  @BeforeClass
  public static void beforeClass() {
    s_beans.add(BeanTestingHelper.get().registerBean(new BeanMetaData(IDateProvider.class)
        .withApplicationScoped(true)
        .withInitialInstance(DATE_PROVIDER)));
  }

  @AfterClass
  public static void afterClass() {
    BeanTestingHelper.get().unregisterBeans(s_beans);
  }

  @Before
  public void setup() {
    m_queue = new ClientNotificationNodeQueue();
    m_queue.init(NodeId.of("testNodeId"));
  }

  @Test
  public void testEmptyQueue() {
    List<ClientNotificationMessage> collector = m_queue.getNotifications(10, 10, TimeUnit.MILLISECONDS);
    assertTrue(collector.isEmpty());
  }

  @Test
  public void testMaxMessagesQueue() {
    putTestNotifications(3);
    List<ClientNotificationMessage> collector = m_queue.getNotifications(2, 10, TimeUnit.MILLISECONDS);
    assertEquals(2, collector.size());
  }

  @Test
  public void testBlockingWait() {
    IFuture<List<ClientNotificationMessage>> res = Jobs.schedule(() -> m_queue.getNotifications(10, 100, TimeUnit.MILLISECONDS), Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));
    ClientNotificationAddress allNodes = ClientNotificationAddress.createAllNodesAddress();
    m_queue.put(new ClientNotificationMessage(allNodes, "test", "cid"));
    m_queue.put(new ClientNotificationMessage(allNodes, "test2", "cid"));
    List<ClientNotificationMessage> notifications = res.awaitDoneAndGet();
    assertEquals(2, notifications.size());
    assertEquals("test", notifications.get(0).getNotification());
    assertEquals("test2", notifications.get(1).getNotification());
  }

  @Test
  public void testCapacityReached() {
    putTestNotifications(11);
    List<ClientNotificationMessage> notifications = m_queue.getNotifications(100, MAX_TEST_CAPACITY, TimeUnit.MILLISECONDS);
    assertEquals(MAX_TEST_CAPACITY, notifications.size());
    assertEquals("test1", notifications.get(0).getNotification());
  }

  @Test
  public void testLastConsumeAccess() {
    assertEquals(0, m_queue.getLastConsumeAccess());
    assertEquals("", m_queue.getLastConsumeAccessFormatted());

    DATE_PROVIDER.setDate(new Date(1));
    m_queue.consume(1, 1, TimeUnit.MILLISECONDS);
    assertEquals(1, m_queue.getLastConsumeAccess());
    assertEquals(DateUtility.format(new Date(1), "yyyy-MM-dd HH:mm:ss.SSS"), m_queue.getLastConsumeAccessFormatted());
  }

  private void putTestNotifications(int count) {
    ClientNotificationAddress allNodes = ClientNotificationAddress.createAllNodesAddress();
    for (int i = 0; i < count; i++) {
      m_queue.put(new ClientNotificationMessage(allNodes, "test" + i, "cid"));
    }
  }
}
