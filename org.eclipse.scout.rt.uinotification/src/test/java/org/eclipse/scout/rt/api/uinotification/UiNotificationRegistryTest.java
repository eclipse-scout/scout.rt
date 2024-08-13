/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.uinotification;

import static org.eclipse.scout.rt.api.uinotification.UiNotificationPutOptions.noTransaction;
import static org.eclipse.scout.rt.api.uinotification.UiNotificationRegistry.SUBSCRIPTION_START_ID;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.api.data.uinotification.TopicDo;
import org.eclipse.scout.rt.api.data.uinotification.UiNotificationDo;
import org.eclipse.scout.rt.dataobject.DataObjectHelper;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.holders.BooleanHolder;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class UiNotificationRegistryTest {
  private UiNotificationRegistry m_registry;

  @Before
  public void before() {
    m_registry = new UiNotificationRegistry();
    m_registry.setCleanupJobInterval(0);
    m_registry.setIdGenerator(new TestIdGenerator());
  }

  @Test
  public void testGetListenerCount() {
    assertEquals(0, m_registry.getListenerCount("topic"));
    m_registry.addListener("test", e -> {
    });
    assertEquals(0, m_registry.getListenerCount("topic"));
    UiNotificationListener listener = e -> {
    };
    m_registry.addListener("topic", listener);
    assertEquals(1, m_registry.getListenerCount("topic"));
    m_registry.addListener("topic", e -> {
    });
    assertEquals(2, m_registry.getListenerCount("topic"));
    m_registry.removeListener("topic", listener);
    assertEquals(1, m_registry.getListenerCount("topic"));
  }

  @Test
  public void testComputeNotificationHandlerMaxDelay() {
    // test lower bounds
    assertEquals(0, m_registry.computeNotificationHandlerMaxDelay(0, 30));

    assertEquals(4, m_registry.computeNotificationHandlerMaxDelay(100, 30));
    assertEquals(20, m_registry.computeNotificationHandlerMaxDelay(100, 5));

    // test upper bounds
    assertEquals(60, m_registry.computeNotificationHandlerMaxDelay(1000, 5));
  }

  @Test
  public void testIsNotificationRelevantForUser() {
    UiNotificationMessageDo withUser = BEANS.get(UiNotificationMessageDo.class).withUser("usr");
    assertTrue(m_registry.isNotificationRelevantForUser(withUser, "usr"));
    assertFalse(m_registry.isNotificationRelevantForUser(withUser, "other"));

    UiNotificationMessageDo exceptUser = BEANS.get(UiNotificationMessageDo.class).withExcludedUserIds("ex1", "ex2");
    assertTrue(m_registry.isNotificationRelevantForUser(exceptUser, "usr"));
    assertTrue(m_registry.isNotificationRelevantForUser(exceptUser, "other"));
    assertFalse(m_registry.isNotificationRelevantForUser(exceptUser, "ex1"));
    assertFalse(m_registry.isNotificationRelevantForUser(exceptUser, "ex2"));

    UiNotificationMessageDo noFilter = BEANS.get(UiNotificationMessageDo.class);
    assertTrue(m_registry.isNotificationRelevantForUser(noFilter, "usr"));
    assertTrue(m_registry.isNotificationRelevantForUser(noFilter, "other"));
  }

  @Test
  public void testGet() {
    m_registry.put("topic", createMessage(), noTransaction());
    UiNotificationDo notification = getNewestNotification("topic");

    // Return a notification that marks subscription start
    assertEquals(Arrays.asList(notification), m_registry.get(Arrays.asList(createGetAllTopic("topic")), null));

    // There are no user specific notifications in the queue -> same result as ab above
    assertEquals(Arrays.asList(notification), m_registry.get(Arrays.asList(createGetAllTopic("topic")), "otto"));
  }

  @Test
  public void testGetWrongTopic() {
    m_registry.put("topic", createMessage(), noTransaction());

    assertEquals(new ArrayList<>(), m_registry.get(Arrays.asList(createGetAllTopic("asdf")), null));
  }

  @Test
  public void testGetInitial() {
    m_registry.put("topic", null, createMessage(), noTransaction());
    m_registry.put("topic", null, createMessage(), noTransaction());
    m_registry.put("topic", null, createMessage(), noTransaction());
    UiNotificationDo notification3 = getNewestNotification("topic");

    // Returns last notification with subscription start marker
    assertEquals(Arrays.asList(asSubscriptionStartNotification(notification3)), m_registry.get(Arrays.asList(createTopic("topic")), null));

    // Does not return any notifications because no new ones have been added since subscription
    assertEquals(new ArrayList<>(), m_registry.get(Arrays.asList(createTopic("topic", getNewestNotification("topic"))), null));
  }

  @Test
  public void testGetWithLastNotification() {
    m_registry.put("topic", createMessage(), noTransaction());
    UiNotificationDo lastNotification = getNewestNotification("topic");

    // All notifications are already read
    assertEquals(new ArrayList<>(), m_registry.get(Arrays.asList(createTopic("topic", lastNotification)), null));

    m_registry.put("topic", createMessage("a"), noTransaction());
    UiNotificationDo notification2 = getNewestNotification("topic");

    m_registry.put("topic", createMessage("b"), noTransaction());
    UiNotificationDo notification3 = getNewestNotification("topic");
    assertEquals(Arrays.asList(notification2, notification3), m_registry.get(Arrays.asList(createTopic("topic", lastNotification)), null));

    // No new notifications available
    assertEquals(new ArrayList<>(), m_registry.get(Arrays.asList(createTopic("topic", notification3)), null));
  }

  @Test
  public void testGetWithLastNotificationsExpired() {
    ((TestIdGenerator) m_registry.getIdGenerator()).getSequence().set(11);

    UiNotificationDo lastNotification = new UiNotificationDo().withId("10").withNodeId(m_registry.currentNodeId()).withCreationTime(DateUtility.addDays(new Date(), -1));

    // Registry is empty because previous messages are expired and removed
    assertEquals(new ArrayList<>(), m_registry.get(Arrays.asList(createTopic("topic", lastNotification)), null));

    m_registry.put("topic", createMessage("a"), noTransaction());
    UiNotificationDo notification = getNewestNotification("topic");

    m_registry.put("topic", createMessage("b"), noTransaction());
    UiNotificationDo notification2 = getNewestNotification("topic");
    assertEquals(Arrays.asList(notification, notification2), m_registry.get(Arrays.asList(createTopic("topic", lastNotification)), null));

    // No new notifications available
    assertEquals(new ArrayList<>(), m_registry.get(Arrays.asList(createTopic("topic", getNewestNotification("topic"))), null));
  }

  @Test
  public void testGetMultipleTopicsWithLastNotifications() {
    IDoEntity message = createMessage();
    m_registry.put("topic a", message, noTransaction()); // 1

    // First call has lastNotifications set to null -> subscribe
    List<UiNotificationDo> subscriptions = m_registry.get(Arrays.asList(createTopic("topic a"), createTopic("topic b")), null);
    assertEquals(Arrays.asList(createSubscriptionStartNotification("topic a"), createInitialNotification("topic b")), subscriptions);

    m_registry.put("topic a", createMessage("a"), noTransaction()); // 2
    m_registry.put("topic b", createMessage("b"), noTransaction()); // 3

    // topic a: lastId is 1 (id of last notification for that topic) because there was already one notification which must not be sent
    // topic b: lastId is -1 because there was no notification -> all notifications for that topic will be returned
    UiNotificationDo lastNotificationA = subscriptions.get(0);
    UiNotificationDo lastNotificationB = subscriptions.get(1);
    List<UiNotificationDo> expected = Arrays.asList(getNewestNotification("topic a"), getNewestNotification("topic b"));
    assertEquals(expected, m_registry.get(Arrays.asList(createTopic("topic a", lastNotificationA), createTopic("topic b", lastNotificationB)), null));
  }

  @Test
  public void testGetWithLastNotificationAndUser() {
    m_registry.put("topic a", "otto", createMessage(), noTransaction()); // 1
    m_registry.put("topic a", "max", createMessage(), noTransaction()); // 2
    m_registry.put("topic a", null, createMessage(), noTransaction()); // 3

    List<UiNotificationDo> subscriptions = m_registry.get(Arrays.asList(createTopic("topic a")), "otto");
    assertEquals(Arrays.asList(createSubscriptionStartNotification("topic a")), subscriptions);

    m_registry.put("topic a", createMessage("a new"), noTransaction()); // 4
    UiNotificationDo notification4 = getNewestNotification("topic a");

    m_registry.put("topic a", "otto", createMessage("a new user"), noTransaction()); // 5
    UiNotificationDo notification5 = getNewestNotification("topic a");

    m_registry.put("topic a", "max", createMessage("a new user max"), noTransaction()); // 6

    UiNotificationDo lastNotification = subscriptions.get(0);
    List<UiNotificationDo> expected = Arrays.asList(notification4, notification5);
    assertEquals(expected, m_registry.get(Arrays.asList(createTopic("topic a", lastNotification)), "otto"));
  }

  @Test
  public void testGetWithLastNotificationSameTime() {
    m_registry.put("topic", createMessage(), noTransaction());
    UiNotificationDo lastNotification1 = m_registry.getNotifications().get("topic").get(0).getNotification();
    lastNotification1.withCreationTime(DateUtility.parse("20220922 140000.123", "yyyyMMdd HHmmss.SSS"));

    m_registry.put("topic", createMessage(), noTransaction());
    UiNotificationDo lastNotification2 = m_registry.getNotifications().get("topic").get(1).getNotification();
    lastNotification2.withCreationTime(DateUtility.parse("20220922 140000.123", "yyyyMMdd HHmmss.SSS"));

    // Notification must not be returned, having two notifications with the same time is not allowed and should not be possible, see UiNotificationRegistry.updateNotificationCreationTime
    assertEquals(new ArrayList<>(), m_registry.get(Arrays.asList(createTopic("topic", lastNotification1)), null));
    assertEquals(new ArrayList<>(), m_registry.get(Arrays.asList(createTopic("topic", lastNotification2)), null));
  }

  @Test
  public void testGetWithLastNotificationMultipleNodes() {
    // Notifications registered -> subscription contains creationDate 0 and id "-1") -> Next request will return every notification
    List<UiNotificationDo> subscriptions = m_registry.get(Arrays.asList(createTopic("topic")), null);
    assertEquals(Arrays.asList(createInitialNotification("topic")), subscriptions);

    m_registry.put("topic", createMessage(), noTransaction());
    UiNotificationDo lastNotificationNode1 = m_registry.getNotifications().get("topic").get(0).getNotification();
    lastNotificationNode1.withNodeId("node1");
    lastNotificationNode1.withCreationTime(DateUtility.parse("20220922 140000", "yyyyMMdd HHmmss"));

    m_registry.put("topic", createMessage(), noTransaction());
    UiNotificationDo lastNotificationNode2 = m_registry.getNotifications().get("topic").get(1).getNotification();
    lastNotificationNode2.withNodeId("node2");
    lastNotificationNode2.withCreationTime(DateUtility.parse("20220922 135959", "yyyyMMdd HHmmss")); // The time of Node 2 is slightly behind Node 1

    // Return notifications from both nodes
    assertEquals(Arrays.asList(lastNotificationNode1, lastNotificationNode2), m_registry.get(Arrays.asList(createTopic("topic", subscriptions.get(0))), null));

    m_registry.put("topic", createMessage(), noTransaction());
    UiNotificationDo lastNotification2Node1 = m_registry.getNotifications().get("topic").get(2).getNotification();
    lastNotification2Node1.withNodeId("node1");
    lastNotification2Node1.withCreationTime(DateUtility.parse("20220922 140001", "yyyyMMdd HHmmss"));

    m_registry.put("topic", createMessage(), noTransaction());
    UiNotificationDo lastNotification2Node2 = m_registry.getNotifications().get("topic").get(3).getNotification();
    lastNotification2Node2.withNodeId("node2");
    lastNotification2Node2.withCreationTime(DateUtility.parse("20220922 140000", "yyyyMMdd HHmmss"));

    // Return notifications from both nodes
    assertEquals(Arrays.asList(lastNotification2Node1, lastNotification2Node2), m_registry.get(Arrays.asList(createTopic("topic", lastNotificationNode1, lastNotificationNode2)), null));

    // Add new notification for a node that is unknown to client
    m_registry.put("topic", createMessage(), noTransaction());
    UiNotificationDo lastNotificationNode3 = m_registry.getNotifications().get("topic").get(4).getNotification();
    lastNotificationNode3.withNodeId("node3");
    lastNotificationNode3.withCreationTime(DateUtility.parse("20220922 140010", "yyyyMMdd HHmmss"));

    assertEquals(Arrays.asList(lastNotificationNode3), m_registry.get(Arrays.asList(createTopic("topic", lastNotification2Node1, lastNotification2Node2)), null));

    // No new notifications available
    assertEquals(new ArrayList<>(), m_registry.get(Arrays.asList(createTopic("topic", lastNotification2Node1, lastNotification2Node2, lastNotificationNode3)), null));
  }

  @Test
  public void testGetWithLastNotificationMultipleNodesExistingTopics() {
    // There are notifications for the topic created by node 1 and node 2, but not node 3
    m_registry.put("topic", createMessage(), noTransaction());
    UiNotificationDo lastNotificationNode1 = m_registry.getNotifications().get("topic").get(0).getNotification();
    lastNotificationNode1.withNodeId("node1");
    lastNotificationNode1.withCreationTime(DateUtility.parse("20220922 140000", "yyyyMMdd HHmmss"));

    m_registry.put("topic", createMessage(), noTransaction());
    UiNotificationDo lastNotificationNode2 = m_registry.getNotifications().get("topic").get(1).getNotification();
    lastNotificationNode2.withNodeId("node2");
    lastNotificationNode2.withCreationTime(DateUtility.parse("20220922 135959", "yyyyMMdd HHmmss")); // The time of Node 2 is slightly behind Node 1

    UiNotificationDo node1Subscription = asSubscriptionStartNotification(lastNotificationNode1);
    UiNotificationDo node2Subscription = asSubscriptionStartNotification(lastNotificationNode2);

    // The last two notifications (one for each node) are returned marked as subscription start
    List<UiNotificationDo> subscriptions = m_registry.get(Arrays.asList(createTopic("topic")), null);
    assertTrue(CollectionUtility.equalsCollection(Arrays.asList(node1Subscription, node2Subscription), subscriptions, false));

    // No new notifications yet
    assertEquals(new ArrayList<>(), m_registry.get(Arrays.asList(createTopic("topic", subscriptions.get(0), subscriptions.get(1))), null));

    // Old, obsolete notification by node1 -> It must not be returned
    m_registry.put("topic", createMessage(), noTransaction());
    UiNotificationDo lastNotification2Node1Old = m_registry.getNotifications().get("topic").get(2).getNotification();
    lastNotification2Node1Old.withNodeId("node1");
    lastNotification2Node1Old.withCreationTime(DateUtility.parse("20220922 120000", "yyyyMMdd HHmmss"));

    // Valid notification by node1
    m_registry.put("topic", createMessage(), noTransaction());
    UiNotificationDo lastNotification3Node1 = m_registry.getNotifications().get("topic").get(3).getNotification();
    lastNotification3Node1.withNodeId("node1");
    lastNotification3Node1.withCreationTime(DateUtility.parse("20220922 140010", "yyyyMMdd HHmmss"));

    // Server 3 has a bad time, but since there weren't any notifications created by that node at the subscription time, it will be returned
    m_registry.put("topic", createMessage(), noTransaction());
    UiNotificationDo lastNotificationNode3 = m_registry.getNotifications().get("topic").get(4).getNotification();
    lastNotificationNode3.withNodeId("node3");
    lastNotificationNode3.withCreationTime(DateUtility.parse("20220922 130055", "yyyyMMdd HHmmss"));

    // Return valid one from node 1 and new one from node3
    assertEquals(Arrays.asList(lastNotification3Node1, lastNotificationNode3), m_registry.get(Arrays.asList(createTopic("topic", node1Subscription, node2Subscription)), null));

    // No new notifications available
    assertEquals(new ArrayList<>(), m_registry.get(Arrays.asList(createTopic("topic", lastNotification3Node1, node2Subscription, lastNotificationNode3)), null));
  }

  @Test
  public void testGetOrWait() {
    IDoEntity message = createMessage();

    BooleanHolder completed = new BooleanHolder();
    CompletableFuture<List<UiNotificationDo>> future = m_registry.getOrWait(Arrays.asList(createGetAllTopic("topic")), null);
    future.thenApply(notifications -> {
      assertEquals(Arrays.asList(getNewestNotification("topic")), notifications);
      completed.setValue(true);
      return notifications;
    });

    // Waiting for notifications
    assertEquals(1, m_registry.getListeners("topic").list().size());

    m_registry.put("topic", message, noTransaction());
    assertNull(m_registry.getListeners("topic"));

    assertEquals(true, completed.getValue());
  }

  @Test
  public void testGetOrWaitPutBeforeWait() throws ExecutionException, InterruptedException, TimeoutException {
    // Put before listening -> getAllOrWait should return it immediately
    m_registry.put("topic", createMessage(), noTransaction());

    BooleanHolder completed = new BooleanHolder();
    CompletableFuture<List<UiNotificationDo>> future = m_registry.getOrWait(Arrays.asList(createGetAllTopic("topic")), null);
    future.thenApply(notifications -> {
      assertEquals(Arrays.asList(getNewestNotification("topic")), notifications);
      completed.setValue(true);
      return notifications;
    });
    // Not waiting for notifications
    assertNull(m_registry.getListeners("topic"));

    // Wait for future to complete
    future.get(1, TimeUnit.SECONDS);

    assertEquals(true, completed.getValue());
  }

  @Test
  public void testGetOrWaitInitial() throws ExecutionException, InterruptedException, TimeoutException {
    BooleanHolder completed = new BooleanHolder();
    CompletableFuture<List<UiNotificationDo>> future = m_registry.getOrWait(Arrays.asList(createTopic("topic")), null);
    future.thenApply(notifications -> {
      // Returns only subscription start notification if lastNotification is null
      assertEquals(Arrays.asList(createInitialNotification("topic")), notifications);
      completed.setValue(true);
      return notifications;
    });
    // Not waiting for notifications
    assertNull(m_registry.getListeners("topic"));

    // Wait for future to complete
    future.get(1, TimeUnit.SECONDS);

    assertEquals(true, completed.getValue());
  }

  @Test
  public void testGetOrWaitMultiple() {
    IDoEntity message = createMessage();

    // A listener for topic
    BooleanHolder completed = new BooleanHolder(false);
    CompletableFuture<List<UiNotificationDo>> future = m_registry.getOrWait(Arrays.asList(createGetAllTopic("topic")), null);
    future.thenApply(notifications -> {
      assertEquals(Arrays.asList(getNewestNotification("topic")), notifications);
      assertEquals("1", getNewestNotification("topic").getId());
      completed.setValue(true);
      return notifications;
    });
    assertEquals(1, m_registry.getListeners("topic").list().size());

    // Another listener for topic
    BooleanHolder completed2 = new BooleanHolder(false);
    CompletableFuture<List<UiNotificationDo>> future2 = m_registry.getOrWait(Arrays.asList(createGetAllTopic("topic")), null);
    future2.thenApply(notifications -> {
      assertEquals(Arrays.asList(getNewestNotification("topic")), notifications);
      assertEquals("1", getNewestNotification("topic").getId());
      completed2.setValue(true);
      return notifications;
    });
    assertEquals(2, m_registry.getListeners("topic").list().size());

    // Listener for topic b
    BooleanHolder completed3 = new BooleanHolder(false);
    CompletableFuture<List<UiNotificationDo>> future3 = m_registry.getOrWait(Arrays.asList(createGetAllTopic("topic b")), null);
    future3.thenApply(notifications -> {
      assertEquals(Arrays.asList(getNewestNotification("topic b")), notifications);
      assertEquals("3", getNewestNotification("topic b").getId());
      completed3.setValue(true);
      return notifications;
    });
    assertEquals(2, m_registry.getListeners("topic").list().size());
    assertEquals(1, m_registry.getListeners("topic b").list().size());

    // Another listener for topic b
    BooleanHolder completed4 = new BooleanHolder(false);
    CompletableFuture<List<UiNotificationDo>> future4 = m_registry.getOrWait(Arrays.asList(createGetAllTopic("topic b")), "otto");
    future4.thenApply(notifications -> {
      assertEquals(Arrays.asList(getNewestNotification("topic b")), notifications);
      assertEquals("2", getNewestNotification("topic b").getId());
      completed4.setValue(true);
      return notifications;
    });
    assertEquals(2, m_registry.getListeners("topic").list().size());
    assertEquals(2, m_registry.getListeners("topic b").list().size());

    // Triggers both listeners for topic
    m_registry.put("topic", message, noTransaction());
    assertNull(m_registry.getListeners("topic"));
    assertEquals(2, m_registry.getListeners("topic b").list().size());
    assertEquals(true, completed.getValue());
    assertEquals(true, completed2.getValue());

    // Triggers only Otto's listener for topic b
    m_registry.put("topic b", "otto", message, noTransaction());
    assertEquals(1, m_registry.getListeners("topic b").list().size());
    assertEquals(false, completed3.getValue());
    assertEquals(true, completed4.getValue());

    // Triggers other listener for topic b
    m_registry.put("topic b", message, noTransaction());
    assertNull(m_registry.getListeners("topic b"));
    assertEquals(true, completed3.getValue());
  }

  @Test
  public void testGetOrWaitCancel() {
    BooleanHolder completed = new BooleanHolder();
    CompletableFuture<List<UiNotificationDo>> future = m_registry.getOrWait(Arrays.asList(createGetAllTopic("topic")), null, 500);
    future.thenApply(notifications -> {
      assertTrue(notifications.isEmpty());
      completed.setValue(true);
      return notifications;
    });

    // Waiting for notifications
    assertEquals(1, m_registry.getListeners("topic").list().size());

    try {
      // Wait until timeout is reached
      future.get();
    }
    catch (InterruptedException | ExecutionException e) {
      // nop
    }

    assertNull(m_registry.getListeners("topic"));
    assertEquals(true, completed.getValue());
  }

  @Test
  public void testCleanup() throws InterruptedException {
    assertTrue(m_registry.getNotifications().isEmpty());

    m_registry.put("topic", null, createMessage(), new UiNotificationPutOptions().withTimeout(TimeUnit.MILLISECONDS.toMillis(500)).withTransactional(false));
    m_registry.put("topic", null, createMessage(), new UiNotificationPutOptions().withTimeout(TimeUnit.MILLISECONDS.toMillis(50)).withTransactional(false));
    assertEquals(2, m_registry.getNotifications().get("topic").size());

    Thread.sleep(51);
    m_registry.cleanup();
    assertEquals(1, m_registry.getNotifications().get("topic").size());

    Thread.sleep(450);
    m_registry.cleanup();
    assertTrue(m_registry.getNotifications().isEmpty());
  }

  @Test
  public void testTransactional() {
    m_registry.put("topic", createMessage());

    // Empty because transaction has not been committed yet
    assertEquals(new ArrayList<>(), m_registry.get(Arrays.asList(createGetAllTopic("topic")), null));

    ITransaction.CURRENT.get().commitPhase1();
    ITransaction.CURRENT.get().commitPhase2();

    assertEquals(Arrays.asList(getNewestNotification("topic")), m_registry.get(Arrays.asList(createGetAllTopic("topic")), null));

    // Somehow transaction is not closed when a new test runs -> remove member manually to not influence other tests
    ITransaction.CURRENT.get().unregisterMember(UiNotificationTransactionMember.TRANSACTION_MEMBER_ID);
  }

  @Test
  public void testTransactionalRollback() {
    m_registry.put("topic", createMessage());

    // Empty because transaction has not been committed yet
    assertEquals(new ArrayList<>(), m_registry.get(Arrays.asList(createGetAllTopic("topic")), null));

    ITransaction.CURRENT.get().rollback();
    ITransaction.CURRENT.get().commitPhase1();
    ITransaction.CURRENT.get().commitPhase2();

    // Still empty because of rollback
    assertEquals(new ArrayList<>(), m_registry.get(Arrays.asList(createGetAllTopic("topic")), null));

    // Create a new message and this time commit the transaction
    IDoEntity message2 = createMessage();
    m_registry.put("topic", createMessage());

    ITransaction.CURRENT.get().commitPhase1();
    ITransaction.CURRENT.get().commitPhase2();

    // Only message2 is in the registry
    UiNotificationDo notification2 = getNewestNotification("topic");
    assertEquals(message2, notification2.getMessage());
    assertEquals(Arrays.asList(notification2), m_registry.get(Arrays.asList(createGetAllTopic("topic")), null));

    // Somehow transaction is not closed when a new test runs -> remove member manually to not influence other tests
    ITransaction.CURRENT.get().unregisterMember(UiNotificationTransactionMember.TRANSACTION_MEMBER_ID);
  }

  @Test
  public void testPublishOverCluster() {
    var clusterService = mock(IUiNotificationClusterService.class);
    List<IBean<?>> beans = BeanTestingHelper.get().registerBeans(new BeanMetaData(IUiNotificationClusterService.class).withInitialInstance(clusterService));
    try {
      // create new registry, because cluster service is registered in its constructor
      var registry = new UiNotificationRegistry();
      registry.setCleanupJobInterval(0);
      registry.setIdGenerator(new TestIdGenerator());

      verify(clusterService, times(0)).publish(any());

      registry.put("topic", createMessage(), UiNotificationPutOptions.noTransaction());
      verify(clusterService, times(1)).publish(any());

      registry.put("topic", createMessage(), UiNotificationPutOptions.noTransaction().withPublishOverCluster(true));
      verify(clusterService, times(2)).publish(any());

      registry.put("topic", createMessage(), UiNotificationPutOptions.noTransaction().withPublishOverCluster(false));
      verify(clusterService, times(2)).publish(any());

      registry.put("topic", createMessage(), UiNotificationPutOptions.noClusterSync().withTransactional(false));
      verify(clusterService, times(2)).publish(any());
    }
    finally {
      BeanTestingHelper.get().unregisterBeans(beans);
    }
  }

  protected IDoEntity createMessage(String value) {
    IDoEntity message = new DoEntity();
    message.put("dummy", value);
    return message;
  }

  protected IDoEntity createMessage() {
    return createMessage("value");
  }

  protected UiNotificationDo createInitialNotification(String topic) {
    return new UiNotificationDo()
        .withId(SUBSCRIPTION_START_ID)
        .withTopic(topic)
        .withCreationTime(new Date(0))
        .withNodeId(m_registry.currentNodeId())
        .withSubscriptionStart(true);
  }

  protected TopicDo createTopic(String topic, UiNotificationDo... lastNotifications) {
    return new TopicDo().withName(topic).withLastNotifications(lastNotifications);
  }

  protected TopicDo createGetAllTopic(String topic) {
    return createTopic(topic, new UiNotificationDo().withId(SUBSCRIPTION_START_ID).withCreationTime(new Date(0)).withNodeId(m_registry.currentNodeId()));
  }

  private class TestIdGenerator extends IdGenerator {
    private AtomicInteger m_sequence = new AtomicInteger(1);

    @Override
    public String generate() {
      return m_sequence.getAndIncrement() + "";
    }

    public AtomicInteger getSequence() {
      return m_sequence;
    }
  }

  protected UiNotificationDo getNewestNotification(String topic) {
    List<UiNotificationMessageDo> notifications = m_registry.getNotifications().get(topic);
    if (notifications == null) {
      return null;
    }
    UiNotificationMessageDo lastElement = notifications.get(notifications.size() - 1);
    return BEANS.get(DataObjectHelper.class).clone(lastElement.getNotification());
  }

  protected UiNotificationDo createSubscriptionStartNotification(String topic) {
    return asSubscriptionStartNotification(getNewestNotification(topic));
  }

  protected UiNotificationDo asSubscriptionStartNotification(UiNotificationDo notification) {
    notification = BEANS.get(DataObjectHelper.class).clone(notification);
    notification.withSubscriptionStart(true);
    notification.remove("message");
    return notification;
  }
}
