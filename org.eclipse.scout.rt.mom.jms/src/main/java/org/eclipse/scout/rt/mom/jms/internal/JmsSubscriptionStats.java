/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.mom.jms.internal;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.JMSException;
import javax.jms.Message;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.date.IDateProvider;

/**
 * @since 6.1
 */
public class JmsSubscriptionStats implements ISubscriptionStats {
  private final AtomicInteger m_invokeCount = new AtomicInteger();
  private final AtomicLong m_messageCount = new AtomicLong();
  private final AtomicLong m_messageNonNullCount = new AtomicLong();
  private final AtomicLong m_errorCount = new AtomicLong();
  private volatile Date m_lastMessageReceivedTimestamp;

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

  @Override
  public Date lastMessageReceivedTimestamp() {
    return m_lastMessageReceivedTimestamp;
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
    m_lastMessageReceivedTimestamp = BEANS.get(IDateProvider.class).currentMillis();
  }

  public void notifyReceiveError(JMSException e) {
    m_errorCount.incrementAndGet();
  }
}
