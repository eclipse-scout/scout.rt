/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.clientnotification;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationProperties.NodeQueueCapacity;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A queue for a client node, that keeps track of notifications for that node.
 */
@Bean
public class ClientNotificationNodeQueue {
  private static final Logger LOG = LoggerFactory.getLogger(ClientNotificationNodeQueue.class);

  private final FinalValue<String> m_nodeId = new FinalValue<>();

  private final int m_capacity;
  private final BlockingDeque<ClientNotificationMessage> m_notifications;
  private final AtomicLong m_lastConsumeAccess;

  public ClientNotificationNodeQueue() {
    this(CONFIG.getPropertyValue(NodeQueueCapacity.class));
  }

  public ClientNotificationNodeQueue(int capacity) {
    m_capacity = capacity;
    m_notifications = new LinkedBlockingDeque<>(capacity);
    m_lastConsumeAccess = new AtomicLong(System.currentTimeMillis());
  }

  public void setNodeId(String nodeId) {
    m_nodeId.set(nodeId);
  }

  public String getNodeId() {
    return m_nodeId.get();
  }

  /**
   * @return capacity of queue. If maximum capacity is reached, messages are dropped.
   */
  public int getCapacity() {
    return m_capacity;
  }

  public void put(ClientNotificationMessage notification) {
    put(CollectionUtility.arrayList(notification));
  }

  public void put(Collection<? extends ClientNotificationMessage> notificationInput) {
    List<ClientNotificationMessage> notifications = getRelevantNotifications(notificationInput);
    putDroppingOld(notifications);
  }

  /**
   * Put notifications into queue and drop the oldest ones, if capacity is reached.
   */
  private void putDroppingOld(Collection<? extends ClientNotificationMessage> notifications) {
    List<ClientNotificationMessage> droppedNotifications = new ArrayList<>();
    for (ClientNotificationMessage message : notifications) {
      boolean inserted = m_notifications.offer(message);
      while (!inserted) {
        ClientNotificationMessage removed = m_notifications.poll();
        if (removed != null) {
          droppedNotifications.add(removed);
        }
        inserted = m_notifications.offer(message);
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
          notifications.size(), droppedNotifications.size(), getNodeId(), getLastConsumeAccessFormatted(), infoExtractor.apply(notifications.stream()), infoExtractor.apply(droppedNotifications.stream()));

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

  /**
   * @return time since messages have last been consumed
   */
  public long getLastConsumeAccess() {
    return m_lastConsumeAccess.get();
  }

  public String getLastConsumeAccessFormatted() {
    return DateUtility.format(new Date(getLastConsumeAccess()), "yyyy-MM-dd HH:mm:ss.SSS");
  }

  public List<ClientNotificationMessage> consume(int maxAmount, long maxWaitTime, TimeUnit unit) {
    m_lastConsumeAccess.set(System.currentTimeMillis());

    List<ClientNotificationMessage> result = getNotifications(maxAmount, maxWaitTime, unit);
    LOG.debug("consumed {} notifications. [clientNodeId={}]", result.size(), getNodeId());
    return result;
  }

  protected List<ClientNotificationMessage> getNotifications(int maxAmount, long maxWaitTime, TimeUnit unit) {
    List<ClientNotificationMessage> collected = new LinkedList<>();
    try {
      //blocking wait to get first message
      ClientNotificationMessage next = m_notifications.poll(maxWaitTime, unit);
      if (next != null) {
        collected.add(next);
      }

      //add more available notifications
      //with short wait timeout to not go back with one notification when some are about to pop up.
      int timeout = 234; // 0 for no reschedule
      while (next != null && collected.size() < maxAmount) {
        next = m_notifications.poll(timeout, TimeUnit.MILLISECONDS);
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

  private List<ClientNotificationMessage> getRelevantNotifications(Collection<? extends ClientNotificationMessage> notificationInput) {
    return notificationInput.stream()
        .filter(msg -> isRelevant(msg.getAddress()))
        .collect(toList());
  }

  public boolean isRelevant(IClientNotificationAddress address) {
    return address.isNotifyAllSessions()
        || address.isNotifyAllNodes()
        || CollectionUtility.hasElements(address.getSessionIds())
        || CollectionUtility.hasElements(address.getUserIds());
  }
}
