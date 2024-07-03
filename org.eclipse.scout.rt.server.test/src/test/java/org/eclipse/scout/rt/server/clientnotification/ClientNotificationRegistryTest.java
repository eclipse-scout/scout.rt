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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterSynchronizationService;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * Tests for {@link ClientNotificationRegistry}
 */
@RunWith(PlatformTestRunner.class)
public class ClientNotificationRegistryTest {
  private static final NodeId TEST_NODE = NodeId.of("Node1");
  private static final String TEST_NOTIFICATION = "testNotification";
  private static final String TEST_USER = "User1";
  private static final int TEST_QUEUE_EXPIRE_TIMEOUT = 10 + 60 * 1000;

  /**
   * Tests that a Notification for all nodes is consumed by multiple test nodes
   */
  @Test
  public void testNotificationsForAllNodes() {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
    reg.registerNode(NodeId.of("testNodeId"));
    reg.registerNode(NodeId.of("testNodeId2"));
    reg.putForAllNodes(TEST_NOTIFICATION);
    List<ClientNotificationMessage> notificationsNode1 = consumeNoWait(reg, NodeId.of("testNodeId"));
    List<ClientNotificationMessage> notificationsNode2 = consumeNoWait(reg, NodeId.of("testNodeId2"));
    assertSingleTestNotification(notificationsNode1);
    assertSingleTestNotification(notificationsNode2);
  }

  /**
   * Register/unregister does not affect consumption.
   */
  @Test
  public void testNotificationsUnregisteredSingleSession() {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
    reg.registerNode(NodeId.of("testNodeId"));
    reg.putForUser(TEST_USER, TEST_NOTIFICATION);
    List<ClientNotificationMessage> notificationsNode = consumeNoWait(reg, NodeId.of("testNodeId"));
    assertEquals(1, notificationsNode.size());
  }

  /**
   * Tests that multiple notifications for a user are consumed in the correct order.
   */
  @Test
  public void testMultipleNotifications() {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
    reg.registerNode(TEST_NODE);
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
    reg.registerNode(NodeId.of("testNodeId"));
    reg.putForAllSessions(TEST_NOTIFICATION);
    List<ClientNotificationMessage> notificationsNode = consumeNoWait(reg, NodeId.of("testNodeId"));
    assertEquals(1, notificationsNode.size());
  }

  /**
   * Empty collection of notifications must not trigger a cluster notification.
   */
  @Test
  public void testEmptyNotificationsAreNotDistributedOverCluster() {
    final IClusterSynchronizationService mockClusterSyncService = Mockito.mock(IClusterSynchronizationService.class);
    final IBean<?> bean = BeanTestingHelper.get().registerBean(new BeanMetaData(IClusterSynchronizationService.class)
        .withInitialInstance(mockClusterSyncService)
        .withApplicationScoped(true));
    try {
      ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
      reg.registerNode(NodeId.of("testNodeId"));
      reg.publish(Collections.emptySet());
      assertEquals(Collections.emptyList(), consumeNoWait(reg, NodeId.of("testNodeId")));
      Mockito.verifyNoInteractions(mockClusterSyncService);
    }
    finally {
      BeanTestingHelper.get().unregisterBean(bean);
    }
  }

  /**
   * Empty collection of notifications must not trigger a cluster notification.
   */
  @Test
  public void testNotificationsWithoutDistributingOverCluster() {
    final IClusterSynchronizationService mockClusterSyncService = Mockito.mock(IClusterSynchronizationService.class);
    final IBean<?> bean = BeanTestingHelper.get().registerBean(new BeanMetaData(IClusterSynchronizationService.class)
        .withInitialInstance(mockClusterSyncService)
        .withApplicationScoped(true));
    try {
      ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
      reg.registerNode(NodeId.of("testNodeId"));
      reg.registerNode(NodeId.of("testNodeId2"));
      reg.putForAllNodes(TEST_NOTIFICATION, false);
      List<ClientNotificationMessage> notificationsNode1 = consumeNoWait(reg, NodeId.of("testNodeId"));
      List<ClientNotificationMessage> notificationsNode2 = consumeNoWait(reg, NodeId.of("testNodeId2"));
      assertSingleTestNotification(notificationsNode1);
      assertSingleTestNotification(notificationsNode2);
      Mockito.verifyNoInteractions(mockClusterSyncService);
    }
    finally {
      BeanTestingHelper.get().unregisterBean(bean);
    }
  }

  @Test
  public void registeredNodeAvailable() {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
    reg.registerNode(TEST_NODE);
    assertTrue(reg.getRegisteredNodeIds().contains(TEST_NODE));
  }

  /**
   * If no sessions are registered, the node should not be available in the registry
   */
  @Test
  public void registeredNodeInitialsNotAvailable() {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
    assertFalse(reg.getRegisteredNodeIds().contains(TEST_NODE));
  }

  /**
   * If all sessions are gone, the node should not be available in the registry.
   */
  @Test
  public void registeredNodeNotAvailable_afterUnregister() {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
    reg.registerNode(TEST_NODE);
    reg.unregisterNode(TEST_NODE);
    assertFalse(reg.getRegisteredNodeIds().contains(TEST_NODE));
  }

  /**
   * If not sessions are removed, the node should be available.
   */
  @Test
  public void testNodeAvailable_AfterUnregisterSession() {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
    reg.registerNode(TEST_NODE);
    assertTrue(reg.getRegisteredNodeIds().contains(TEST_NODE));
  }

  /**
   * If no message is consumed, queue is removed
   */
  @Test
  public void testQueueRemovedAfterTimeout() throws InterruptedException {
    ClientNotificationRegistry reg = new ClientNotificationRegistry(10);
    reg.registerNode(TEST_NODE);
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
    reg.registerNode(TEST_NODE);
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
  public void testTransactionalWithPiggyBack() {
    try {
      final NodeId currentNode = TEST_NODE;
      final NodeId otherNode = NodeId.of("Node2");

      ClientNotificationCollector collector = new ClientNotificationCollector();

      ServerRunContexts.copyCurrent()
          .withClientNodeId(currentNode)
          .withClientNotificationCollector(collector)
          .run(() -> {
            ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
            reg.registerNode(currentNode);
            reg.registerNode(otherNode);

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
          });

    }
    finally {
      IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.set(null);
    }
  }

  /**
   * If the notifications are already consumed, piggyback is not possible
   */
  @Test
  public void testTransactionalNoPiggyBack() {
    final NodeId currentNode = TEST_NODE;
    final NodeId otherNode = NodeId.of("Node2");

    ClientNotificationCollector collector = new ClientNotificationCollector();
    collector.consume();
    ServerRunContexts.copyCurrent().withClientNodeId(currentNode).withClientNotificationCollector(collector).run(() -> {
      ClientNotificationRegistry reg = new ClientNotificationRegistry(TEST_QUEUE_EXPIRE_TIMEOUT);
      reg.registerNode(currentNode);
      reg.registerNode(otherNode);
      reg.putTransactionalForUser(TEST_USER, TEST_NOTIFICATION);
      commit();
      //no notifications for current request (piggyback)
      List<ClientNotificationMessage> notifications = ClientNotificationCollector.CURRENT.get().consume();
      assertTrue(notifications.isEmpty());
      //notifications for current nodes
      assertSingleTestNotification(consumeNoWait(reg, currentNode));
      //notifications for other nodes
      assertSingleTestNotification(consumeNoWait(reg, otherNode));
    });
  }

  private void commit() {
    ITransaction transaction = ITransaction.CURRENT.get();
    transaction.commitPhase1();
    transaction.commitPhase2();
    transaction.release();
  }

  private List<ClientNotificationMessage> consumeNoWait(ClientNotificationRegistry reg, NodeId nodeId) {
    return reg.consume(nodeId, 1, 1, TimeUnit.MILLISECONDS);
  }

  private void assertSingleTestNotification(List<ClientNotificationMessage> notifications) {
    assertFalse(notifications.isEmpty());
    assertEquals(TEST_NOTIFICATION, notifications.get(0).getNotification());
  }

}
