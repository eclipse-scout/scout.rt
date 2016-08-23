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
package org.eclipse.scout.rt.server.clientnotification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationAddress;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link ClientNotificationNodeQueue}
 */
@RunWith(PlatformTestRunner.class)
public class ClientNotificationNodeQueueTest {
  private ClientNotificationNodeQueue m_queue;
  private int MAX_TEST_CAPACITY = 10;

  @Before
  public void setup() {
    m_queue = new ClientNotificationNodeQueue(MAX_TEST_CAPACITY);
    m_queue.registerSession("testSession", "testUser");
    m_queue.setNodeId("testNodeId");
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
    IFuture<List<ClientNotificationMessage>> res = Jobs.schedule(new Callable<List<ClientNotificationMessage>>() {

      @Override
      public List<ClientNotificationMessage> call() throws Exception {
        return m_queue.getNotifications(10, 100, TimeUnit.MILLISECONDS);
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));
    ClientNotificationAddress allNodes = ClientNotificationAddress.createAllNodesAddress();
    m_queue.put(new ClientNotificationMessage(allNodes, "test", true, "cid"));
    m_queue.put(new ClientNotificationMessage(allNodes, "test2", true, "cid"));
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

  private void putTestNotifications(int count) {
    ClientNotificationAddress allNodes = ClientNotificationAddress.createAllNodesAddress();
    for (int i = 0; i < count; i++) {
      m_queue.put(new ClientNotificationMessage(allNodes, "test" + i, true, "cid"));
    }
  }

}
