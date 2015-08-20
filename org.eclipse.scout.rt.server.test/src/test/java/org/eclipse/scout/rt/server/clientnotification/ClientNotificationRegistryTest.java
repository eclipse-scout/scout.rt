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
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link ClientNotificationRegistry}
 */
@RunWith(PlatformTestRunner.class)
public class ClientNotificationRegistryTest {
  private static final String TEST_NOTIFICATION = "testNotification";
  private static final String TEST_SESSION = "testSessionId";
  private static final String TEST_USER = "User1";

  /**
   * Tests that a Notification for all nodes is consumed by multiple test nodes
   */
  @Test
  public void testNotificationsForAllNodes() {
    ClientNotificationRegistry reg = new ClientNotificationRegistry();
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
    ClientNotificationRegistry reg = new ClientNotificationRegistry();
    reg.registerSession("Node1", TEST_SESSION, TEST_USER);
    reg.registerSession("Node2", TEST_SESSION, TEST_USER);
    reg.registerSession("Node2", "otherSession", TEST_USER);
    reg.registerSession("Node3", "otherSession", TEST_USER);
    reg.putForSession(TEST_SESSION, TEST_NOTIFICATION);

    List<ClientNotificationMessage> notificationsN1 = consumeNoWait(reg, "Node1");
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
    ClientNotificationRegistry reg = new ClientNotificationRegistry();
    reg.registerSession("testNodeId", TEST_SESSION, TEST_USER);
    reg.unregisterSession("testNodeId", TEST_SESSION, TEST_USER);
    reg.putForSession(TEST_SESSION, TEST_NOTIFICATION);
    List<ClientNotificationMessage> notificationsNode = consumeNoWait(reg, "testNodeId");
    assertTrue(notificationsNode.isEmpty());
  }

  /**
   * Tests that a notification for a single user is only consumed by nodes that have the user registered.
   */
  @Test
  public void testNotificationsSingleUser() {
    ClientNotificationRegistry reg = new ClientNotificationRegistry();
    reg.registerSession("Node1", TEST_SESSION, TEST_USER);
    reg.registerSession("Node2", TEST_SESSION, "User2");
    reg.registerSession("Node2", "otherSession", TEST_USER);
    reg.registerSession("Node3", "otherSession", "User2");
    reg.putForUser(TEST_USER, TEST_NOTIFICATION);

    List<ClientNotificationMessage> notificationsN1 = consumeNoWait(reg, "Node1");
    List<ClientNotificationMessage> notificationsN2 = consumeNoWait(reg, "Node2");
    List<ClientNotificationMessage> notificationsN3 = consumeNoWait(reg, "Node3");
    assertSingleTestNotification(notificationsN1);
    assertSingleTestNotification(notificationsN2);
    assertTrue(notificationsN3.isEmpty());
  }

  /**
   * Tests that multiple notificaitions for a user are consumed in the correct order.
   */
  @Test
  public void testMultipleNotifications() throws Exception {
    ClientNotificationRegistry reg = new ClientNotificationRegistry();
    reg.registerSession("Node1", TEST_SESSION, TEST_USER);
    reg.putForUser(TEST_USER, TEST_NOTIFICATION);
    reg.putForUser(TEST_USER, "notification2");

    List<ClientNotificationMessage> notificationsN1 = reg.consume("Node1", 10, 1, TimeUnit.MILLISECONDS);
    assertEquals(2, notificationsN1.size());
    assertEquals(TEST_NOTIFICATION, notificationsN1.get(0).getNotification());
    assertEquals("notification2", notificationsN1.get(1).getNotification());
  }

  /**
   * If a response is available and the notification is processed within a transaction, it should only be on
   * ServerRunContext.
   */
  @Test
  public void testTransactionalWithPiggyBack() throws Exception {
    try {
      IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.set(mock(HttpServletResponse.class));

      final String currentNode = "Node1";
      final String otherNode = "Node2";

      ServerRunContexts.copyCurrent().withNotificationNodeId(currentNode).withTransactionalClientNotificationCollector(new TransactionalClientNotificationCollector()).run(new IRunnable() {

        @Override
        public void run() throws Exception {
          ClientNotificationRegistry reg = new ClientNotificationRegistry();
          reg.registerSession(currentNode, TEST_SESSION, TEST_USER);
          reg.registerSession(otherNode, TEST_SESSION, TEST_USER);

          reg.putTransactionalForUser(TEST_USER, TEST_NOTIFICATION);
          commit();
          //collected for request
          List<ClientNotificationMessage> notifications = TransactionalClientNotificationCollector.CURRENT.get().values();
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
   * If no response is available and the notification is processed within a transaction, it should only be on
   * ServerRunContext.
   */
  @Test
  public void testTransactionalNoPiggyBack() throws Exception {
    final String currentNode = "Node1";
    final String otherNode = "Node2";

    ServerRunContexts.copyCurrent().withNotificationNodeId(currentNode).withTransactionalClientNotificationCollector(new TransactionalClientNotificationCollector()).run(new IRunnable() {

      @Override
      public void run() throws Exception {
        ClientNotificationRegistry reg = new ClientNotificationRegistry();
        reg.registerSession(currentNode, TEST_SESSION, TEST_USER);
        reg.registerSession(otherNode, TEST_SESSION, TEST_USER);
        reg.putTransactionalForUser(TEST_USER, TEST_NOTIFICATION);
        commit();
        //no notifications for current request (piggy back)
        List<ClientNotificationMessage> notifications = TransactionalClientNotificationCollector.CURRENT.get().values();
        assertTrue(notifications.isEmpty());
        //notifications for current nodes
        assertSingleTestNotification(consumeNoWait(reg, currentNode));
        //notifications for other nodes
        assertSingleTestNotification(consumeNoWait(reg, otherNode));
      }
    });
  }

  private void commit() throws ProcessingException {
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
