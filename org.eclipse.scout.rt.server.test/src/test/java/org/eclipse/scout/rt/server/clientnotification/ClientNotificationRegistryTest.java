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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterSynchronizationService;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * Tests for {@link ClientNotificationRegistry}
 */
@RunWith(PlatformTestRunner.class)
public class ClientNotificationRegistryTest {
  private static final String TEST_NODE = "Node1";
  private static final String TEST_NOTIFICATION = "testNotification";
  private static final String TEST_SESSION = "testSessionId";
  private static final String TEST_SESSION_2 = "testSessionId2";
  private static final String TEST_USER = "User1";
  private static final int TEST_QUEUE_EXPIRE_TIMEOUT = 10 + 60 * 1000;

  /**
   * Tests that a Notification for all nodes is consumed by multiple test nodes
   */
  @Test
  public void testNotificationsForAllNodes() {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
    reg.registerSession("testNodeId", "testSessionId", TEST_USER);
    reg.registerSession("testNodeId2", "testSessionId", TEST_USER);
    reg.putForAllNodes(TEST_NOTIFICATION);
    List<ClientNotificationMessage> notificationsNode1 = consumeNoWait(reg, "testNodeId");
    List<ClientNotificationMessage> notificationsNode2 = consumeNoWait(reg, "testNodeId2");
    assertSingleTestNotification(notificationsNode1);
    assertSingleTestNotification(notificationsNode2);
  }

  /**
   * Tests that a notification for a single session is only consumed by nodes that have the session registered.
   */
  @Test
  public void testNotificationsSingleSession() {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
    reg.registerSession(TEST_NODE, TEST_SESSION, TEST_USER);
    reg.registerSession("Node2", TEST_SESSION, TEST_USER);
    reg.registerSession("Node2", "otherSession", TEST_USER);
    reg.registerSession("Node3", "otherSession", TEST_USER);
    reg.putForSession(TEST_SESSION, TEST_NOTIFICATION);

    List<ClientNotificationMessage> notificationsN1 = consumeNoWait(reg, TEST_NODE);
    List<ClientNotificationMessage> notificationsN2 = consumeNoWait(reg, "Node2");
    List<ClientNotificationMessage> notificationsN3 = consumeNoWait(reg, "Node3");
    assertSingleTestNotification(notificationsN1);
    assertSingleTestNotification(notificationsN2);
    assertTrue(notificationsN3.isEmpty());
  }

  /**
   * If a session is unregistered, no notification is consumed.
   */
  @Test
  public void testNotificationsUnregisteredSession() {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
    reg.registerSession("testNodeId", TEST_SESSION, TEST_USER);
    reg.unregisterSession("testNodeId", TEST_SESSION, TEST_USER);
    reg.putForSession(TEST_SESSION, TEST_NOTIFICATION);
    List<ClientNotificationMessage> notificationsNode = consumeNoWait(reg, "testNodeId");
    assertTrue(notificationsNode.isEmpty());
  }

  /**
   * Register/unregister does not affect consumption.
   */
  @Test
  public void testNotificationsUnregisteredSingleSession() {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
    reg.registerSession("testNodeId", TEST_SESSION, TEST_USER);
    reg.registerSession("testNodeId", TEST_SESSION_2, TEST_USER);
    reg.unregisterSession("testNodeId", TEST_SESSION_2, TEST_USER);
    reg.putForUser(TEST_USER, TEST_NOTIFICATION);
    List<ClientNotificationMessage> notificationsNode = consumeNoWait(reg, "testNodeId");
    assertEquals(1, notificationsNode.size());
  }

  /**
   * Tests that a notification for a single user is only consumed by nodes that have the user registered.
   */
  @Test
  public void testNotificationsSingleUser() {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
    reg.registerSession(TEST_NODE, TEST_SESSION, TEST_USER);
    reg.registerSession("Node2", TEST_SESSION, "User2");
    reg.registerSession("Node2", "otherSession", TEST_USER);
    reg.registerSession("Node3", "otherSession", "User2");
    reg.putForUser(TEST_USER, TEST_NOTIFICATION);

    List<ClientNotificationMessage> notificationsN1 = consumeNoWait(reg, TEST_NODE);
    List<ClientNotificationMessage> notificationsN2 = consumeNoWait(reg, "Node2");
    List<ClientNotificationMessage> notificationsN3 = consumeNoWait(reg, "Node3");
    assertSingleTestNotification(notificationsN1);
    assertSingleTestNotification(notificationsN2);
    assertTrue(notificationsN3.isEmpty());
  }

  /**
   * Tests that multiple notifications for a user are consumed in the correct order.
   */
  @Test
  public void testMultipleNotifications() throws Exception {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
    reg.registerSession(TEST_NODE, TEST_SESSION, TEST_USER);
    reg.putForUser(TEST_USER, TEST_NOTIFICATION);
    reg.putForUser(TEST_USER, "notification2");

    List<ClientNotificationMessage> notificationsN1 = reg.consume(TEST_NODE, 10, 1, TimeUnit.MILLISECONDS);
    assertEquals(2, notificationsN1.size());
    assertEquals(TEST_NOTIFICATION, notificationsN1.get(0).getNotification());
    assertEquals("notification2", notificationsN1.get(1).getNotification());
  }

  @Test
  public void testNotificationsForAllSessions() {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
    reg.registerSession("testNodeId", TEST_SESSION, TEST_USER);
    reg.registerSession("testNodeId", TEST_SESSION_2, TEST_USER);
    reg.putForAllSessions(TEST_NOTIFICATION);
    List<ClientNotificationMessage> notificationsNode = consumeNoWait(reg, "testNodeId");
    assertEquals(1, notificationsNode.size());
  }

  /**
   * Empty collection of notifications must not trigger a cluster notification.
   */
  @Test
  public void testEmptyNotificationsAreNotDistributedOverCluster() {
    final IClusterSynchronizationService mockClusterSyncService = Mockito.mock(IClusterSynchronizationService.class);
    final IBean<?> bean = TestingUtility.registerBean(new BeanMetaData(IClusterSynchronizationService.class)
        .withInitialInstance(mockClusterSyncService)
        .withApplicationScoped(true));
    try {
      ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
      reg.registerSession("testNodeId", "testSessionId", TEST_USER);
      reg.publish(Collections.<ClientNotificationMessage> emptySet());
      assertEquals(Collections.emptyList(), consumeNoWait(reg, "testNodeId"));
      Mockito.verifyZeroInteractions(mockClusterSyncService);
    }
    finally {
      TestingUtility.unregisterBean(bean);
    }
  }

  /**
   * Empty collection of notifications must not trigger a cluster notification.
   */
  @Test
  public void testNotificationsWithoutDistributingOverCluster() {
    final IClusterSynchronizationService mockClusterSyncService = Mockito.mock(IClusterSynchronizationService.class);
    final IBean<?> bean = TestingUtility.registerBean(new BeanMetaData(IClusterSynchronizationService.class)
        .withInitialInstance(mockClusterSyncService)
        .withApplicationScoped(true));
    try {
      ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
      reg.registerSession("testNodeId", "testSessionId", TEST_USER);
      reg.registerSession("testNodeId2", "testSessionId", TEST_USER);
      reg.putForAllNodes(TEST_NOTIFICATION, false);
      List<ClientNotificationMessage> notificationsNode1 = consumeNoWait(reg, "testNodeId");
      List<ClientNotificationMessage> notificationsNode2 = consumeNoWait(reg, "testNodeId2");
      assertSingleTestNotification(notificationsNode1);
      assertSingleTestNotification(notificationsNode2);
      Mockito.verifyZeroInteractions(mockClusterSyncService);
    }
    finally {
      TestingUtility.unregisterBean(bean);
    }
  }

  @Test
  public void registeredNodeAvailable() {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
    reg.registerSession(TEST_NODE, TEST_SESSION, TEST_USER);
    assertTrue(reg.getRegisteredNodeIds().contains(TEST_NODE));
  }

  /**
   * If no sessions are registered, the node should not be available in the registry
   */
  @Test
  public void registeredNodeInitiallsNotAvailable() {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
    assertFalse(reg.getRegisteredNodeIds().contains(TEST_NODE));
  }

  /**
   * If all sessions are gone, the node should not be available in the registry.
   */
  @Test
  public void registeredNodeNotAvailable_afterUnregister() {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
    reg.registerSession(TEST_NODE, TEST_SESSION, TEST_USER);
    reg.unregisterSession(TEST_NODE, TEST_SESSION, TEST_USER);
    assertFalse(reg.getRegisteredNodeIds().contains(TEST_NODE));
  }

  /**
   * If not sessions are removed, the node should be available.
   */
  @Test
  public void testNodeAvailable_AfterUnregisterSession() {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
    reg.registerSession(TEST_NODE, TEST_SESSION, TEST_USER);
    reg.registerSession(TEST_NODE, "testSession2", TEST_USER);
    reg.unregisterSession(TEST_NODE, TEST_SESSION, TEST_USER);
    assertTrue(reg.getRegisteredNodeIds().contains(TEST_NODE));
  }

  /**
   * If no message is consumed, queue is removed
   */
  @Test
  public void testQueueRemovedAfterTimeout() throws InterruptedException {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(10);
    reg.registerSession(TEST_NODE, TEST_SESSION, TEST_USER);
    Thread.sleep(100);
    reg.putForAllNodes("notification");
    List<ClientNotificationMessage> consumed = consumeNoWait(reg, TEST_NODE);
    assertTrue(consumed.isEmpty());
  }

  /**
   * If messages are consumed,the queue is not removed after the timeout.
   */
  @Test
  public void testQueueNotRemovedIfConsumed() throws InterruptedException {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(100);
    reg.registerSession(TEST_NODE, TEST_SESSION, TEST_USER);
    for (int i = 0; i < 100; i++) {
      Thread.sleep(10);
      consumeNoWait(reg, TEST_NODE);
    }
    reg.putForAllNodes("notification");
    List<ClientNotificationMessage> consumed = consumeNoWait(reg, TEST_NODE);
    assertFalse(consumed.isEmpty());
  }

  /**
   * If a response is available and the notification is processed within a transaction, it should only be on
   * ServerRunContext.
   */
  @Test
  public void testTransactionalWithPiggyBack() throws Exception {
    try {
      final String currentNode = TEST_NODE;
      final String otherNode = "Node2";

      ClientNotificationCollector collector = new ClientNotificationCollector();

      ServerRunContexts.copyCurrent()
          .withClientNodeId(currentNode)
          .withClientNotificationCollector(collector)
          .run(new IRunnable() {

            @Override
            public void run() throws Exception {
              ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
              reg.registerSession(currentNode, TEST_SESSION, TEST_USER);
              reg.registerSession(otherNode, TEST_SESSION, TEST_USER);

              reg.putTransactionalForUser(TEST_USER, TEST_NOTIFICATION);
              commit();
              //collected for request
              List<ClientNotificationMessage> notifications = ClientNotificationCollector.CURRENT.get().consume();
              assertSingleTestNotification(notifications);
              //no notification for current node
              List<ClientNotificationMessage> ownRegNotifications = consumeNoWait(reg, currentNode);
              assertTrue(ownRegNotifications.isEmpty());
              //notifications for other nodes
              assertSingleTestNotification(consumeNoWait(reg, otherNode));
            }
          });

    }
    finally {
      IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.set(null);
    }
  }

  /**
   * If the notifications are already consumed, piggy back is not possible
   */
  @Test
  public void testTransactionalNoPiggyBack() throws Exception {
    final String currentNode = TEST_NODE;
    final String otherNode = "Node2";

    ClientNotificationCollector collector = new ClientNotificationCollector();
    collector.consume();
    ServerRunContexts.copyCurrent().withClientNodeId(currentNode).withClientNotificationCollector(collector).run(new IRunnable() {

      @Override
      public void run() throws Exception {
        ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
        reg.registerSession(currentNode, TEST_SESSION, TEST_USER);
        reg.registerSession(otherNode, TEST_SESSION, TEST_USER);
        reg.putTransactionalForUser(TEST_USER, TEST_NOTIFICATION);
        commit();
        //no notifications for current request (piggy back)
        List<ClientNotificationMessage> notifications = ClientNotificationCollector.CURRENT.get().consume();
        assertTrue(notifications.isEmpty());
        //notifications for current nodes
        assertSingleTestNotification(consumeNoWait(reg, currentNode));
        //notifications for other nodes
        assertSingleTestNotification(consumeNoWait(reg, otherNode));
      }
    });
  }

  private void commit() {
    ITransaction.CURRENT.get().commitPhase1();
    ITransaction.CURRENT.get().commitPhase2();
  }

  private List<ClientNotificationMessage> consumeNoWait(ClientNotificationRegistry reg, String nodeId) {
    return reg.consume(nodeId, 1, 1, TimeUnit.MILLISECONDS);
  }

  private void assertSingleTestNotification(List<ClientNotificationMessage> notifications) {
    assertFalse(notifications.isEmpty());
    assertEquals(TEST_NOTIFICATION, notifications.get(0).getNotification());
  }

}
