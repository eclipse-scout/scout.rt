/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.clientnotification;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.platform.util.date.IDateProvider;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationProperties.NodeQueueCapacity;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of a queue for a client node, that keeps track of notifications for that node.
 */
public abstract class AbstractClientNotificationNodeQueue implements IClientNotificationNodeQueue {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractClientNotificationNodeQueue.class);

  protected final FinalValue<NodeId> m_clientNodeId = new FinalValue<>();

  protected final Long m_createTimestamp = System.currentTimeMillis();

  protected final AtomicLong m_lastConsumeAccess;

  public AbstractClientNotificationNodeQueue() {
    m_lastConsumeAccess = new AtomicLong();
  }

  @Override
  public void init(NodeId clientNodeId) {
    m_clientNodeId.set(clientNodeId);
  }

  @Override
  public NodeId getClientNodeId() {
    return m_clientNodeId.get();
  }

  @Override
  public void put(ClientNotificationMessage notification) {
    put(List.of(notification));
  }

  @Override
  public void put(Collection<? extends ClientNotificationMessage> notifications) {
    List<ClientNotificationMessage> relevantNotifications = getRelevantNotifications(notifications);
    putDroppingOld(relevantNotifications);
  }

  /**
   * Put notifications into queue and drop the oldest ones, if capacity is reached.
   */
  protected void putDroppingOld(Collection<? extends ClientNotificationMessage> notifications) {
    List<ClientNotificationMessage> droppedNotifications = new ArrayList<>();
    for (ClientNotificationMessage message : notifications) {
      LOG.debug("Put notification {} to queue. [clientNodeId={}]", message.getNotification().getClass().getSimpleName(), getClientNodeId());
      boolean inserted = offer(message);
      while (!inserted) {
        ClientNotificationMessage removed = poll();
        if (removed != null) {
          droppedNotifications.add(removed);
        }
        inserted = offer(message);
      }
    }
    if (!droppedNotifications.isEmpty()) {
      Function<Stream<? extends ClientNotificationMessage>, String> infoExtractor = s -> s
          .map(m -> m.getNotification().getClass().getSimpleName() + " -> " + m.getAddress().prettyPrint())
          .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
          .entrySet().stream()
          .sorted(Entry.<String, Long> comparingByValue().reversed())
          .map(e -> e.getKey() + " (" + e.getValue() + "x)")
          .collect(Collectors.joining(", ", "[", "]"));

      LOG.error("Notification queue capacity reached. Added {}, removed oldest {} notification messages. [clientNodeId={}, lastConsumeAccess={}, newNotifications={}, droppedNotifications={}]",
          notifications.size(), droppedNotifications.size(), getClientNodeId(), getLastConsumeAccessFormatted(), infoExtractor.apply(notifications.stream()), infoExtractor.apply(droppedNotifications.stream()));

      if (LOG.isDebugEnabled()) {
        Function<Stream<? extends ClientNotificationMessage>, String> detailInfoExtractor = s -> s
            .map(m -> m.toString())
            .collect(Collectors.joining("\n    ", "\n    ", ""));

        LOG.debug("Notification queue capacity reached. Details:\n  newNotifications={}\n  droppedNotifications={}",
            detailInfoExtractor.apply(notifications.stream()), detailInfoExtractor.apply(droppedNotifications.stream()),
            new Exception("stacktrace for further analysis"));
      }
    }
  }

  @Override
  public long getCreateTimestamp() {
    return m_createTimestamp;
  }

  /**
   * @return time since messages have last been consumed
   */
  @Override
  public long getLastConsumeAccess() {
    return m_lastConsumeAccess.get();
  }

  @Override
  public String getLastConsumeAccessFormatted() {
    if (getLastConsumeAccess() == 0) {
      return "";
    }
    return DateUtility.format(new Date(getLastConsumeAccess()), "yyyy-MM-dd HH:mm:ss.SSS");
  }

  @Override
  public List<ClientNotificationMessage> consume(int maxAmount, long maxWaitTime, TimeUnit unit) {
    m_lastConsumeAccess.set(BEANS.get(IDateProvider.class).currentUTCMillis());

    List<ClientNotificationMessage> result = getNotifications(maxAmount, maxWaitTime, unit);
    LOG.debug("Consumed {} notifications. [clientNodeId={}]", result.size(), getClientNodeId());
    return result;
  }

  protected List<ClientNotificationMessage> getNotifications(int maxAmount, long maxWaitTime, TimeUnit unit) {
    List<ClientNotificationMessage> collected = new LinkedList<>();
    try {
      //blocking wait to get first message
      ClientNotificationMessage next = poll(maxWaitTime, unit);
      if (next != null) {
        collected.add(next);
      }

      //add more available notifications
      //with short wait timeout to not go back with one notification when some are about to pop up.
      int timeout = 234; // 0 for no reschedule
      while (next != null && collected.size() < maxAmount) {
        next = poll(timeout, TimeUnit.MILLISECONDS);
        if (next != null) {
          collected.add(next);
        }
      }
    }
    catch (InterruptedException e) {
      LOG.info("Interrupted while waiting for client notification messages", e);
    }
    return collected;
  }

  protected List<ClientNotificationMessage> getRelevantNotifications(Collection<? extends ClientNotificationMessage> notificationInput) {
    return notificationInput.stream()
        .filter(msg -> isRelevant(msg.getAddress()))
        .collect(toList());
  }

  protected boolean isRelevant(IClientNotificationAddress address) {
    return address.isNotifyAllSessions()
        || address.isNotifyAllNodes()
        || CollectionUtility.hasElements(address.getSessionIds())
        || CollectionUtility.hasElements(address.getUserIds());
  }

  /**
   * @return capacity of queue. If maximum capacity is reached, messages are dropped.
   */
  protected int getConfiguredCapacity() {
    return CONFIG.getPropertyValue(NodeQueueCapacity.class);
  }

  /**
   * Inserts the specified notification into the queue.
   */
  protected abstract boolean offer(ClientNotificationMessage notification);

  /**
   * Retrieves and removes the head of the queue or returns {@code null} if this queue is empty.
   */
  protected abstract ClientNotificationMessage poll();

  /**
   * Retrieves and removes the head of the queue, waiting up to the specified wait time if necessary
   * for an element to become available or returns {@code null} if this queue is empty.
   */
  protected abstract ClientNotificationMessage poll(long timeout, TimeUnit unit) throws InterruptedException;
}
