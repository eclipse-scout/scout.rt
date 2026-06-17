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

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

/**
 * A queue implementation for a client node, that keeps track of notifications for that node.
 * This implementation is based on a {@link BlockingQueue}
 */
public class ClientNotificationNodeQueue extends AbstractClientNotificationNodeQueue {

  protected final BlockingDeque<ClientNotificationMessage> m_queue;

  public ClientNotificationNodeQueue() {
    m_queue = new LinkedBlockingDeque<>(getConfiguredCapacity());
  }

  @Override
  protected ClientNotificationMessage poll() {
    return m_queue.poll();
  }

  @Override
  protected ClientNotificationMessage poll(long timeout, TimeUnit unit) throws InterruptedException {
    return m_queue.poll(timeout, unit);
  }

  @Override
  protected boolean offer(ClientNotificationMessage notification) {
    return m_queue.offer(notification);
  }

  @Override
  public long size() {
    return m_queue.size();
  }
}
