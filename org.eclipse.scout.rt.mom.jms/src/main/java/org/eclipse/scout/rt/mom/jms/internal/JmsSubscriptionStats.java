/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.jms.internal;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @since 6.1
 */
public class JmsSubscriptionStats implements ISubscriptionStats {
  private final AtomicInteger m_invokeCount = new AtomicInteger();
  private final AtomicLong m_messageCount = new AtomicLong();
  private final AtomicLong m_messageNonNullCount = new AtomicLong();
  private final AtomicLong m_errorCount = new AtomicLong();

  @Override
  public boolean invokingReceive() {
    return m_invokeCount.get() > 0;
  }

  @Override
  public long receivedMessages() {
    return m_messageCount.get();
  }

  @Override
  public long receivedNonNullMessages() {
    return m_messageNonNullCount.get();
  }

  @Override
  public long receivedErrors() {
    return m_errorCount.get();
  }

  public void notifyBeforeReceive() {
    m_invokeCount.incrementAndGet();
  }

  public void notifyAfterReceive() {
    m_invokeCount.decrementAndGet();
  }

  public void notifyReceiveMessage(Message m) {
    m_messageCount.getAndIncrement();
    if (m != null) {
      m_messageNonNullCount.getAndIncrement();
    }
  }

  public void notifyReceiveError(JMSException e) {
    m_errorCount.incrementAndGet();
  }
}
