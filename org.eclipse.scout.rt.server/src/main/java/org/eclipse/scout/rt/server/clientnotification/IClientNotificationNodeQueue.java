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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

/**
 * Interface for a client notification queue for a client node, that keeps track of notifications for that node.
 */
@Bean
public interface IClientNotificationNodeQueue {

  /**
   * Initialize node queue for given {@code clientNodeId}.
   */
  void init(NodeId clientNodeId);

  /**
   * @return {@link NodeId} associated with this queue.
   */
  NodeId getClientNodeId();

  /**
   * Put notification into queue.
   */
  void put(ClientNotificationMessage notification);

  /**
   * Put notifications into queue.
   */
  void put(Collection<? extends ClientNotificationMessage> notifications);

  /**
   * @return list of next notifications up to a limit of given {@code maxAmount}. Waits for given {@code maxWaitTime} if no messages are available.
   */
  List<ClientNotificationMessage> consume(int maxAmount, long maxWaitTime, TimeUnit unit);

  /**
   * @return timestamp of queue creation.
   */
  long getCreateTimestamp();

  /**
   * @return timestamp of last message consume invocation.
   */
  long getLastConsumeAccess();

  /**
   * @return formatted timestamp of last message consume invocation.
   */
  String getLastConsumeAccessFormatted();

  /**
   * @return size of queue.
   */
  long size();

  /**
   * Disposes this object and releases any associated resources.
   */
  default void dispose() {
  }
}
