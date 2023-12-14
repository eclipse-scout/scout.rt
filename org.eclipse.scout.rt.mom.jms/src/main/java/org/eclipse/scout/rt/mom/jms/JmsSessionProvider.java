/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.jms;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TemporaryQueue;
import jakarta.jms.Topic;

import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.mom.jms.internal.ISubscriptionStats;
import org.eclipse.scout.rt.mom.jms.internal.JmsSubscriptionStats;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption.IRestorer;

public class JmsSessionProvider implements IJmsSessionProvider {

  private final Session m_session;
  private final Destination m_destination; // may be null
  private final JmsSubscriptionStats m_stats;
  private MessageProducer m_producer;
  private MessageConsumer m_consumer;

  private TemporaryQueue m_temporaryQueue;

  private volatile boolean m_closing;

  public JmsSessionProvider(Session session, Destination destination) {
    m_session = assertNotNull(session);
    m_destination = destination;
    m_stats = createJmsSubscriptionStats();
  }

  protected JmsSubscriptionStats createJmsSubscriptionStats() {
    return new JmsSubscriptionStats();
  }

  @Override
  public Session getSession() {
    return m_session;
  }

  @Override
  public MessageProducer getProducer() throws JMSException {
    if (m_producer == null) {
      m_producer = createProducer();
    }
    return m_producer;
  }

  protected MessageProducer createProducer() throws JMSException {
    return m_session.createProducer(null);
  }

  @Override
  public MessageConsumer getConsumer(SubscribeInput input) throws JMSException {
    if (m_consumer == null) {
      m_consumer = createConsumer(input);
    }
    return m_consumer;
  }

  protected MessageConsumer createConsumer(SubscribeInput input) throws JMSException {
    boolean noLocal = !input.isLocalReceipt();
    if (m_destination instanceof Topic && input.getDurableSubscriptionName() != null) {
      return m_session.createDurableSubscriber((Topic) m_destination, input.getDurableSubscriptionName(), input.getSelector(), noLocal);
    }
    return m_session.createConsumer(m_destination, input.getSelector(), noLocal);
  }

  @Override
  public TemporaryQueue getTemporaryQueue() throws JMSException {
    if (m_temporaryQueue == null) {
      m_temporaryQueue = m_session.createTemporaryQueue();
    }
    return m_temporaryQueue;
  }

  @Override
  public void deleteTemporaryQueue() throws JMSException {
    Assertions.assertTrue(m_closing, "deleteTemporaryQueue can only be called on a closing session provider.");
    if (m_temporaryQueue != null) {
      try {
        m_temporaryQueue.delete();
      }
      finally {
        m_temporaryQueue = null;
      }
    }
  }

  @Override
  public Message receive(SubscribeInput input, long receiveTimeoutMillis) throws JMSException {
    try {
      MessageConsumer consumer = getConsumer(input);
      m_stats.notifyBeforeReceive();
      Message m = receiveTimeoutMillis == 0L ? consumer.receive() : consumer.receive(receiveTimeoutMillis);
      m_stats.notifyReceiveMessage(m);
      return m;
    }
    catch (JMSException e) {
      m_stats.notifyReceiveError(e);
      throw e;
    }
    finally {
      m_stats.notifyAfterReceive();
    }
  }

  @Override
  public ISubscriptionStats getStats() {
    return m_stats;
  }

  @Override
  public boolean isClosing() {
    return m_closing;
  }

  @Override
  public void close() {
    m_closing = true;
    IRestorer interruption = ThreadInterruption.clear(); // Temporarily clear the thread's interrupted status while closing
    try {
      closeImpl();
    }
    catch (JMSException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
    finally {
      interruption.restore();
    }
  }

  protected void closeImpl() throws JMSException {
    if (m_session != null) {
      m_session.close();
    }
  }
}
