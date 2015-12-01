/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jms.transactional;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.server.jms.IJmsMessageSerializer;
import org.eclipse.scout.rt.server.transaction.AbstractTransactionMember;
import org.eclipse.scout.rt.server.transaction.ITransactionMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scout {@link ITransactionMember} to use the JMS API transaction features. If one needs transactional behavior on JMS
 * messaging one should also consider JTA transactions.
 *
 * @param <T>
 *          the type of message that should be sent and received
 */
public class JmsTransactionMember<T> extends AbstractTransactionMember {
  private static final Logger LOG = LoggerFactory.getLogger(JmsTransactionMember.class);

  private final Connection m_connection;
  private final Session m_session;
  private final Destination m_destination;
  private final IJmsMessageSerializer<T> m_messageSerializer;
  //lazy initialized
  private MessageConsumer m_messageConsumer;
  private MessageProducer m_messageProducer;

  public JmsTransactionMember(String transactionMemberId, Connection connection, Session session, Destination destination, IJmsMessageSerializer<T> messageSerializer) {
    super(transactionMemberId);
    if (connection == null || session == null || destination == null || messageSerializer == null) {
      throw new IllegalArgumentException("None of the arguments can be null");
    }
    m_connection = connection;
    m_session = session;
    m_destination = destination;
    m_messageSerializer = messageSerializer;
  }

  protected Connection getConnection() {
    return m_connection;
  }

  protected Session getSession() {
    return m_session;
  }

  protected Destination getDestination() {
    return m_destination;
  }

  protected IJmsMessageSerializer<T> getMessageSerializer() {
    return m_messageSerializer;
  }

  protected MessageConsumer getMessageConsumer() throws JMSException {
    if (m_messageConsumer == null) {
      m_messageConsumer = m_session.createConsumer(m_destination);
    }
    return m_messageConsumer;
  }

  protected MessageProducer getMessageProducer() throws JMSException {
    if (m_messageProducer == null) {
      m_messageProducer = m_session.createProducer(m_destination);
    }
    return m_messageProducer;
  }

  public void send(T message) {
    try {
      getMessageProducer().send(getMessageSerializer().createMessage(message, getSession()));
    }
    catch (JMSException e) {
      throw new ProcessingException("Failed to send jms message", e);
    }
  }

  public T receive(long timeoutMillis) {
    try {
      Message jmsMessage;
      if (timeoutMillis > 0) {
        jmsMessage = getMessageConsumer().receive(timeoutMillis);
      }
      else if (timeoutMillis == 0) {
        jmsMessage = getMessageConsumer().receiveNoWait();
      }
      else {
        jmsMessage = getMessageConsumer().receive();
      }
      return getMessageSerializer().extractMessage(jmsMessage);
    }
    catch (JMSException e) {
      throw new ProcessingException("Failed to receive jms message", e);
    }
  }

  @Override
  public boolean needsCommit() {
    return m_messageProducer != null || m_messageConsumer != null;
  }

  @Override
  public boolean commitPhase1() {
    return true;
  }

  @Override
  public void commitPhase2() {
    try {
      getSession().commit();
    }
    catch (JMSException ex) {
      LOG.error("commit: " + getSession(), ex);
    }
  }

  @Override
  public void rollback() {
    try {
      getSession().rollback();
    }
    catch (JMSException ex) {
      LOG.error("rollback: " + getSession(), ex);
    }
  }

  @Override
  public void release() {
    try {
      // only need to close session, consumer / producer are then closed automatically
      getSession().close();
    }
    catch (JMSException ex) {
      LOG.error("release", ex);
    }
    // do not close or stop shared connection, as it might be still used by other transactions
  }
}
