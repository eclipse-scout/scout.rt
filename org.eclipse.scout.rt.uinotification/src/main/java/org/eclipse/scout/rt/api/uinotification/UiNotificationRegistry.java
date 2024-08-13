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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.api.data.uinotification.TopicDo;
import org.eclipse.scout.rt.api.data.uinotification.UiNotificationDo;
import org.eclipse.scout.rt.api.uinotification.UiNotificationConfigProperties.NotificationHandlerThroughputProperty;
import org.eclipse.scout.rt.api.uinotification.UiNotificationConfigProperties.RegistryCleanupJobIntervalProperty;
import org.eclipse.scout.rt.api.uinotification.UiNotificationConfigProperties.UiNotificationExpirationTimeProperty;
import org.eclipse.scout.rt.api.uinotification.UiNotificationConfigProperties.UiNotificationWaitTimeoutProperty;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.job.FixedDelayScheduleBuilder;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.security.SecurityUtility;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.platform.util.event.FastListenerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class UiNotificationRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(UiNotificationRegistry.class);
  public static final String SUBSCRIPTION_START_ID = "-1";

  private IdGenerator m_idGenerator = new IdGenerator();
  private ReadWriteLock m_lock = new ReentrantReadWriteLock();
  /**
   * Contains all notifications per topic including notifications that are created by other cluster nodes.
   * <p>
   * The order is not relevant for the implementation, however, notifications created this registry are actually ordered
   * by {@link UiNotificationDo#creationTime()} because only one notification can be inserted at a time, and it will be
   * added to the end of the list. Notifications received from other cluster nodes are also added at the end of the
   * list, but they may not arrive in the same order as inserted in the registry of the other cluster node.
   * <p>
   * The list will be cleaned up regularly by {@link #m_cleanupJob}.
   */
  private Map<String, List<UiNotificationMessageDo>> m_notifications = new HashMap<>();
  private final Map<String, FastListenerList<UiNotificationListener>> m_listeners = new HashMap<>();
  private IFuture<Void> m_cleanupJob;
  private long m_cleanupJobInterval = CONFIG.getPropertyValue(RegistryCleanupJobIntervalProperty.class);
  private IUiNotificationClusterService m_clusterService;
  private Date m_lastCreationTime;

  public UiNotificationRegistry() {
    m_clusterService = BEANS.opt(IUiNotificationClusterService.class);
    if (m_clusterService == null) {
      LOG.info("No implementation for IUiNotificationClusterService found. UI notifications won't be delivered to other cluster nodes.");
    }
  }

  /**
   * @see #getOrWait(List, String, long)
   */
  public CompletableFuture<List<UiNotificationDo>> getOrWait(List<TopicDo> topics, String user) {
    return getOrWait(topics, user, CONFIG.getPropertyValue(UiNotificationWaitTimeoutProperty.class));
  }

  /**
   * Checks if there are notifications for the given topics since the {@link UiNotificationDo#creationTime()} of the
   * {@link TopicDo#lastNotifications}, and if there are, completes the future immediately with these notifications.
   * Otherwise, a listener is added to the registry which will be notified when new notifications are put into the
   * registry. Once this happens, the future will be completed with the new notifications.
   *
   * @return a future that will be completed when new a notification is put into the registry for the given topics. It
   *         will also complete with an empty list of notifications if the given timeout expires.
   */
  public CompletableFuture<List<UiNotificationDo>> getOrWait(List<TopicDo> topics, String user, long timeout) {
    List<String> topicNames = topics.stream().map(topic -> topic.getName()).collect(Collectors.toList());
    List<UiNotificationDo> notifications = get(topics, user);
    if (!notifications.isEmpty() || timeout <= 0) {
      LOG.info("Returning {} notifications for topics {} and user {} without waiting.", notifications.size(), topicNames, user);
      return CompletableFuture.completedFuture(notifications);
    }

    CompletableFuture<List<UiNotificationDo>> future = new CompletableFuture<>();
    final UiNotificationListener listener = event -> {
      List<UiNotificationDo> newNotifications = get(topics, user);
      if (!newNotifications.isEmpty()) {
        LOG.info("New notifications received for topics {} and user {}.", topicNames, user);
        future.complete(newNotifications);
      }
    };
    addListeners(topicNames, listener);

    LOG.debug("Waiting for new notifications for topics {} and user {}.", topicNames, user);

    return future.thenApply(uiNotificationDos -> {
      removeListeners(topicNames, user, listener);
      return uiNotificationDos;
    }).orTimeout(timeout, TimeUnit.MILLISECONDS).exceptionally(throwable -> {
      removeListeners(topicNames, user, listener);
      if (throwable instanceof TimeoutException) {
        LOG.debug("Timeout reached, stop waiting");
      }
      else {
        LOG.error("Exception while waiting for notifications", throwable);
      }
      return new ArrayList<>();
    });
  }

  /**
   * @return the notifications for the given topics since the {@link UiNotificationDo#creationTime()} of the
   *         {@link TopicDo#lastNotifications}.
   */
  public List<UiNotificationDo> get(List<TopicDo> topics, String user) {
    List<UiNotificationDo> notifications = new ArrayList<>();
    for (TopicDo topic : topics) {
      notifications.addAll(get(topic.getName(), user, topic.getLastNotifications()));
    }
    return notifications;
  }

  /**
   * Computes the time window (maximum delay) for notification handlers of the given topic. Such handlers should use
   * this time frame to delay requests back to the server to flatten the peak load.
   *
   * @param topic
   *          The topic for which the delay should be computed.
   * @return The delay window (maximum time) in seconds. Is in the range [0, 60].
   */
  public long computeNotificationHandlerDelayWindow(String topic) {
    int listenerCount = getListenerCount(topic);
    int reloadThroughputPerSecond = Math.max(CONFIG.getPropertyValue(NotificationHandlerThroughputProperty.class), 1); // must be > 0
    return computeNotificationHandlerMaxDelay(listenerCount, reloadThroughputPerSecond);
  }

  protected long computeNotificationHandlerMaxDelay(int numListeners, int reloadThroughputPerSecond) {
    var delayWindow = (numListeners + reloadThroughputPerSecond - 1) / reloadThroughputPerSecond; // = ceil(numListeners/reloadThroughputPerSecond)
    return Math.max(Math.min(delayWindow, 60), 0); // max 1min delay window
  }

  protected List<UiNotificationDo> get(String topic, String user, final List<UiNotificationDo> lastKnownNotifications) {
    m_lock.readLock().lock();
    try {
      Stream<UiNotificationDo> notificationStream = getNotifications().getOrDefault(topic, new ArrayList<>()).stream()
          .filter(elem -> isNotificationRelevantForUser(elem, user))
          .map(elem -> elem.getNotification());

      // Return notifications that just act as subscription start markers
      if (lastKnownNotifications.isEmpty()) {
        return createSubscriptionStartNotifications(topic, notificationStream);
      }

      // If the last element is SUBSCRIPTION_START_ID, return all elements
      if (lastKnownNotifications.size() == 1 && SUBSCRIPTION_START_ID.equals(lastKnownNotifications.get(0).getId())) {
        return notificationStream.collect(Collectors.toList());
      }

      // Group last known notifications by nodeId
      Map<String, UiNotificationDo> lastKnownNotificationsByNode = lastKnownNotifications.stream()
          .collect(Collectors.toMap(UiNotificationDo::getNodeId, Function.identity()));

      // Return all elements that were created after the last known notifications
      return notificationStream
          .filter(notification -> {
            UiNotificationDo lastKnownNotification = lastKnownNotificationsByNode.get(notification.getNodeId());
            long creationTime = notification.getCreationTime().getTime();
            long lastKnownNotificationTime = lastKnownNotification == null ? 0 : lastKnownNotification.getCreationTime().getTime();
            return creationTime > lastKnownNotificationTime;
          })
          .collect(Collectors.toList());
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  protected boolean isNotificationRelevantForUser(UiNotificationMessageDo notification, String requestingUser) {
    String notificationUser = notification.getUser();
    if (notificationUser != null) {
      // If notification contains a user it must match the given user
      return notificationUser.equals(requestingUser);
    }
    Set<String> excludedUserIds = notification.getExcludedUserIds();
    if (CollectionUtility.isEmpty(excludedUserIds)) {
      return true;
    }

    // if notification has excluded users: the current user must not be part of it
    return !excludedUserIds.contains(requestingUser);
  }

  /**
   * Creates a notification to mark the start of the subscription.
   * <p>
   * This is necessary to ensure the client receives every notification from now on even if the connection temporarily
   * drops before the first real notification can be sent. During that connection drop a notification could be added
   * that needs to be sent as soon as the connection is reestablished again.
   * <p>
   * The result can contain multiple notifications, one for each cluster node. So, if a cluster node created
   * notifications for the topic, the last one for that node will be returned. If another node did not create any
   * notifications for the topic, the result won't contain a notification for that node. The next request will then
   * return all notifications for node 1 since the last known notification and all notifications for node 2.
   */
  protected List<UiNotificationDo> createSubscriptionStartNotifications(String topic, Stream<UiNotificationDo> notificationStream) {
    Map<String, UiNotificationDo> lastNotificationByNode = notificationStream
        .collect(Collectors.toMap(UiNotificationDo::getNodeId, Function.identity(), (first, second) -> second));
    List<UiNotificationDo> lastNotifications = new ArrayList<>();

    if (lastNotificationByNode.isEmpty()) {
      // Next attempt will get all notifications for this topic
      lastNotifications.add(new UiNotificationDo()
          .withId(SUBSCRIPTION_START_ID)
          .withTopic(topic)
          .withCreationTime(new Date(0))
          .withNodeId(currentNodeId())
          .withSubscriptionStart(true));
    }

    for (Entry<String, UiNotificationDo> entry : lastNotificationByNode.entrySet()) {
      UiNotificationDo notification = entry.getValue();
      lastNotifications.add(new UiNotificationDo()
          .withId(notification.getId())
          .withTopic(topic)
          .withCreationTime(notification.getCreationTime())
          .withNodeId(notification.getNodeId())
          .withSubscriptionStart(true));
    }
    return lastNotifications;
  }

  /**
   * Puts a message into the registry for a specific topic.
   *
   * @see #put(String, String, IDoEntity, UiNotificationPutOptions)
   */
  public void put(String topic, IDoEntity message) {
    put(topic, null, message, null);
  }

  /**
   * Puts a message into the registry for a specific topic with custom options.
   *
   * @see #put(String, String, IDoEntity, UiNotificationPutOptions)
   */
  public void put(String topic, IDoEntity message, UiNotificationPutOptions options) {
    put(topic, null, message, options);
  }

  /**
   * Puts a message into the registry for a specific topic and user.
   *
   * @see #put(String, String, IDoEntity, UiNotificationPutOptions)
   */
  public void put(String topic, String userId, IDoEntity message) {
    put(topic, userId, message, null);
  }

  /**
   * Puts a message into the registry for a specific topic and user with custom options.
   *
   * @param topic
   *          A notification must be assigned to a topic. Must not be {@code null}.
   * @param userId
   *          If specified, only the user with this id will get the notification. May be {@code null}.
   * @param message
   *          The message part of the {@link UiNotificationDo}. May be {@code null}.
   * @param options
   *          Optional {@link UiNotificationPutOptions}. May be {@code null}.
   */
  public void put(String topic, String userId, IDoEntity message, UiNotificationPutOptions options) {
    putMessage(topic, userId, null, message, options);
  }

  /**
   * Puts a message into the registry of all users except the ones provided.
   *
   * @param topic
   *          A notification must be assigned to a topic. Must not be {@code null}.
   * @param excludedUserIds
   *          {@link Collection} of userIds which should NOT receive the message.
   * @param message
   *          The message part of the {@link UiNotificationDo}. May be {@code null}.
   * @param options
   *          Optional {@link UiNotificationPutOptions}. May be {@code null}.
   */
  public void putExcept(String topic, Collection<String> excludedUserIds, IDoEntity message, UiNotificationPutOptions options) {
    if (CollectionUtility.isEmpty(excludedUserIds)) {
      excludedUserIds = null;
    }
    putMessage(topic, null, excludedUserIds, message, options);
  }

  protected void putMessage(String topic, String userId, Collection<String> excludedUserIds, IDoEntity message, UiNotificationPutOptions options) {
    Assertions.assertNotNull(topic, "Topic must not be null");
    if (options == null) {
      options = new UiNotificationPutOptions();
    }

    UiNotificationDo notification = BEANS.get(UiNotificationDo.class)
        .withId(getIdGenerator().generate())
        .withTopic(topic)
        .withNodeId(currentNodeId())
        .withMessage(message);

    UiNotificationMessageDo metaMessage = BEANS.get(UiNotificationMessageDo.class)
        .withNotification(notification)
        .withUser(userId)
        .withExcludedUserIds(excludedUserIds)
        .withTimeout(ObjectUtility.nvl(options.getTimeout(), CONFIG.getPropertyValue(UiNotificationExpirationTimeProperty.class)));

    options = options.copy().withTimeout(0L);

    if (ObjectUtility.nvl(options.getTransactional(), true)) {
      putTransactional(metaMessage, options);
    }
    else {
      putInternal(metaMessage, options);
    }
  }

  protected void putTransactional(UiNotificationMessageDo message, UiNotificationPutOptions options) {
    ITransaction transaction = Assertions.assertNotNull(ITransaction.CURRENT.get(), "No transaction found on current calling context to register transactional ui notification {}", message);
    if (options == null) {
      options = new UiNotificationPutOptions();
    }
    options = options.copy().withTransactional(false);
    try {
      UiNotificationTransactionMember txMember = (UiNotificationTransactionMember) transaction.getMember(UiNotificationTransactionMember.TRANSACTION_MEMBER_ID);
      if (txMember == null) {
        txMember = new UiNotificationTransactionMember(this);
        transaction.registerMember(txMember);
      }
      txMember.addNotification(message, options);
    }
    catch (RuntimeException e) {
      LOG.warn("Could not register transaction member. The notification will be processed immediately", e);
      putInternal(message, options);
    }
  }

  protected void publishOverCluster(UiNotificationMessageDo message) {
    if (m_clusterService == null) {
      return;
    }
    m_clusterService.publish(message);
    LOG.info("Published ui notification with id {} for topic {} and user {} to other cluster nodes.", message.getNotification().getId(), message.getNotification().getTopic(), message.getUser());
  }

  public void handleClusterNotification(UiNotificationMessageDo message) {
    UiNotificationDo notification = message.getNotification();
    LOG.info("Received ui notification with id {} from cluster node {} for topic {} and user {}.", notification.getId(), notification.getNodeId(), notification.getTopic(), message.getUser());
    putInternal(message, UiNotificationPutOptions.noClusterSync());
  }

  protected void putInternal(UiNotificationMessageDo message, UiNotificationPutOptions options) {
    UiNotificationDo notification = message.getNotification();
    String topic = notification.getTopic();
    m_lock.writeLock().lock();
    try {
      updateNotificationCreationTime(notification);

      List<UiNotificationMessageDo> uiNotifications = getNotifications().computeIfAbsent(topic, key -> new ArrayList<>());
      uiNotifications.add(message);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Added new ui notification {} for topic {}. New size: {}", notification, topic, uiNotifications.size());
      }
      else {
        LOG.info("Added new ui notification with id {} for topic {}. New size: {}", notification.getId(), topic, uiNotifications.size());
      }

      triggerEvent(topic, notification);
      startCleanupJob(); // inside lock to ensure cleanup job will be started only once
    }
    finally {
      m_lock.writeLock().unlock();
    }
    if (options == null || ObjectUtility.nvl(options.getPublishOverCluster(), true)) {
      publishOverCluster(message);
    }
  }

  protected void updateNotificationCreationTime(UiNotificationDo notification) {
    if (!currentNodeId().equals(notification.getNodeId())) {
      // Ignore notifications created by other nodes
      return;
    }
    // Ensure creation time is unique per node
    notification.withCreationTime(new Date());
    if (m_lastCreationTime != null && notification.getCreationTime().getTime() <= m_lastCreationTime.getTime()) {
      notification.withCreationTime(DateUtility.addMilliseconds(m_lastCreationTime, 1));
    }
    m_lastCreationTime = notification.getCreationTime();
  }

  public void addListener(String topic, UiNotificationListener listener) {
    synchronized (m_listeners) {
      FastListenerList<UiNotificationListener> listeners = m_listeners.computeIfAbsent(topic, k -> new FastListenerList<>());
      listeners.add(listener);
    }
  }

  public void removeListener(String topic, UiNotificationListener listener) {
    synchronized (m_listeners) {
      FastListenerList<UiNotificationListener> listeners = getListeners(topic);
      if (listeners == null) {
        return;
      }
      listeners.remove(listener);
      if (listeners.isEmpty()) {
        m_listeners.remove(topic);
      }
    }
  }

  /**
   * Retrieves how many observers are listening for the given topic.
   * <p>
   * Note: a listener is not the same as a subscriber, so it must not be used to check whether a notification should be
   * put into the registry. A listener count of 0 does not mean that nobody is currently interested in that topic. A
   * listener is added temporarily during a poll request and removed when the request returns. So, if every poll request
   * is returning, the listener count may be 0, but the clients may still be interested and immediately start new poll
   * requests again.
   *
   * @param topic
   *          The topic for which the listener count should be returned.
   * @return The number of listeners for the given topic.
   */
  protected int getListenerCount(String topic) {
    var listenerList = m_listeners.get(topic);
    if (listenerList == null || listenerList.isEmpty()) {
      return 0;
    }
    return listenerList.size();
  }

  public void addListeners(List<String> topics, UiNotificationListener listener) {
    for (String topicName : topics) {
      addListener(topicName, listener);
    }
  }

  public void removeListeners(List<String> topics, String user, UiNotificationListener listener) {
    for (String topicName : topics) {
      removeListener(topicName, listener);
    }
  }

  protected void triggerEvent(String topic, UiNotificationDo notification) {
    FastListenerList<UiNotificationListener> listeners = getListeners(topic);
    if (listeners == null) {
      return;
    }
    for (UiNotificationListener listener : listeners.list()) {
      listener.notificationAdded(new UiNotificationAddedEvent(this, notification));
    }
  }

  protected final FastListenerList<UiNotificationListener> getListeners(String topic) {
    synchronized (m_listeners) {
      return m_listeners.get(topic);
    }
  }

  protected final Map<String, List<UiNotificationMessageDo>> getNotifications() {
    return m_notifications;
  }

  public void startCleanupJob() {
    if (m_cleanupJob != null || getCleanupJobInterval() == 0) {
      // Already started
      return;
    }
    LOG.info("Starting cleanup job");
    m_cleanupJob = Jobs.schedule(() -> {
      BEANS.get(UiNotificationRegistry.class).cleanup();

      m_lock.readLock().lock();
      try {
        if (getNotifications().isEmpty()) {
          m_cleanupJob.cancel(false);
          m_cleanupJob = null;
          LOG.info("Cleanup job stopped.");
        }
      }
      finally {
        m_lock.readLock().unlock();
      }
    }, Jobs.newInput()
        .withName("UI Notification registry cleanup")
        .withExceptionHandling(new ExceptionHandler() {
          @Override
          public void handle(Throwable t) {
            LOG.error("Exception while running ui notification registry cleanup job", t);
          }
        }, true)
        .withExecutionTrigger(Jobs
            .newExecutionTrigger()
            .withSchedule(FixedDelayScheduleBuilder.repeatForever(getCleanupJobInterval(), TimeUnit.SECONDS))));
  }

  /**
   * Removes all expires ui notifications.
   *
   * @see UiNotificationMessageDo#getTimeout(), {@link UiNotificationDo#creationTime()}
   */
  public void cleanup() {
    m_lock.writeLock().lock();
    try {
      if (getNotifications().isEmpty()) {
        return;
      }
      LOG.debug("Cleaning up expired ui notifications. Topic count: {}.", getNotifications().size());

      long now = new Date().getTime();
      for (Entry<String, List<UiNotificationMessageDo>> entry : getNotifications().entrySet()) {
        List<UiNotificationMessageDo> notifications = entry.getValue();
        int oldSize = notifications.size();
        if (notifications.removeIf(elem -> elem.getNotification().getCreationTime().getTime() + elem.getTimeout() < now)) {
          int newSize = notifications.size();
          LOG.info("Removed {} expired notifications for topic {}. New size: {}.", oldSize - newSize, entry.getKey(), newSize);
        }
      }

      // Remove topic if there are no notifications left
      getNotifications().entrySet().removeIf(entry -> entry.getValue().isEmpty());
      LOG.debug("Clean up finished. New topic count: {}.", getNotifications().size());
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  /**
   * Configures how often the cleanup job should run.
   * <p>
   * The property needs to be set before the cleanup job is scheduled, which is, before the first notification is put.
   *
   * @param cleanupJobInterval
   *          The interval in seconds between job runs. 0 to disable the job.
   */
  public void setCleanupJobInterval(long cleanupJobInterval) {
    m_cleanupJobInterval = cleanupJobInterval;
  }

  public long getCleanupJobInterval() {
    return m_cleanupJobInterval;
  }

  public void setIdGenerator(IdGenerator idGenerator) {
    m_idGenerator = idGenerator;
  }

  public IdGenerator getIdGenerator() {
    return m_idGenerator;
  }

  /**
   * @return the hash of the current node id. Because it will be sent to the UI and may contain the name of the server,
   *         a hash is used instead of the plain node id.
   */
  public String currentNodeId() {
    return Base64Utility.encode(SecurityUtility.hash(NodeId.current().toString().getBytes()));
  }
}
