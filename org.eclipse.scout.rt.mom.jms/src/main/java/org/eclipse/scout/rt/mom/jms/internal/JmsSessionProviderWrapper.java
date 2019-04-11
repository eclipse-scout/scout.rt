/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.jms.internal;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.mom.jms.ICreateJmsSessionProvider;
import org.eclipse.scout.rt.mom.jms.IJmsSessionProvider;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMS session wrapper that supports client side connection failover
 *
 * @since 6.1
 */
public class JmsSessionProviderWrapper implements IJmsSessionProvider2 {
  private static final Logger LOG = LoggerFactory.getLogger(JmsSessionProviderWrapper.class);

  protected final JmsConnectionWrapper m_connectionWrapper;
  protected final boolean m_transacted;
  protected final ICreateJmsSessionProvider m_sessionProviderFunction;

  protected volatile IJmsSessionProvider m_impl;
  protected volatile TemporaryQueue m_temporaryQueue;
  protected volatile boolean m_closing;

  public JmsSessionProviderWrapper(JmsConnectionWrapper connectionWrapper, boolean transacted, ICreateJmsSessionProvider providerFunction) {
    m_connectionWrapper = connectionWrapper;
    m_transacted = transacted;
    m_sessionProviderFunction = providerFunction;
    m_connectionWrapper.registerSessionProvider(this);
  }

  @Override
  public boolean isClosing() {
    return m_closing;
  }

  @Override
  public void close() {
    m_closing = true;
    try {
      synchronized (m_sessionProviderFunction) {
        if (m_impl != null) {
          try {
            LOG.debug("Close sessionProvider {}", m_impl);
            m_impl.close();
          }
          finally {
            m_impl = null;
          }
        }
      }
    }
    finally {
      m_connectionWrapper.unregisterSessionProvider(this);
    }
  }

  /**
   * Called by {@link JmsConnectionWrapper} upon failover
   */
  public void invalidate() {
    synchronized (m_sessionProviderFunction) {
      if (m_impl != null) {
        try {
          LOG.info("Invalidate sessionProvider {}", m_impl);
          m_impl.close();
        }
        finally {
          m_impl = null;
        }
      }
    }
  }

  protected IJmsSessionProvider trySessionProvider() throws JMSException {
    synchronized (m_sessionProviderFunction) {
      if (m_closing) {
        throw new ProcessingException("closed");
      }
      if (m_impl != null) {
        return m_impl;
      }
      //create a new session
      LOG.debug("Creating sessionProvider");
      Connection c = m_connectionWrapper.getConnection();
      Session session = m_transacted ? c.createSession(true, Session.SESSION_TRANSACTED) : c.createSession(false, Session.AUTO_ACKNOWLEDGE);
      try {
        m_impl = m_sessionProviderFunction.create(session);
        LOG.debug("Created sessionProvider {}", m_impl);
        return m_impl;
      }
      catch (JMSException | RuntimeException e) {
        session.close();
        throw e;
      }
    }
  }

  protected void waitForRetry(JMSException e) throws JMSException {
    if (m_closing) {
      throw e;
    }
    if (m_connectionWrapper.getConnectionRetryCount() <= 0) {
      throw e;
    }
    long timeWaitMillis = m_connectionWrapper.getSessionRetryIntervalMillis();
    LOG.info("JMS call failed '{}'; retry in {} seconds", e, (timeWaitMillis / 1000L));
    try {
      Thread.sleep(timeWaitMillis);
    }
    catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      throw new ThreadInterruptedError("Interrupted", ie);
    }
  }

  @Override
  public Session getSession() throws JMSException {
    try {
      return trySessionProvider().getSession();
    }
    catch (JMSException e) {
      waitForRetry(e);
      return trySessionProvider().getSession();
    }
  }

  @Override
  public MessageProducer getProducer() throws JMSException {
    try {
      return trySessionProvider().getProducer();
    }
    catch (JMSException e) {
      waitForRetry(e);
      return trySessionProvider().getProducer();
    }
  }

  @Override
  public MessageConsumer getConsumer(final SubscribeInput input) throws JMSException {
    try {
      return trySessionProvider().getConsumer(input);
    }
    catch (JMSException e) {
      waitForRetry(e);
      return trySessionProvider().getConsumer(input);
    }
  }

  @Override
  public Message receive(final SubscribeInput input, long receiveTimeoutMillis) throws JMSException {
    try {
      return JmsSessionProviderMigration.receive(trySessionProvider(), input, receiveTimeoutMillis);
    }
    catch (JMSException e) {
      waitForRetry(e);
      return JmsSessionProviderMigration.receive(trySessionProvider(), input, receiveTimeoutMillis);
    }
  }

  @Override
  public TemporaryQueue getTemporaryQueue() throws JMSException {
    try {
      m_temporaryQueue = trySessionProvider().getTemporaryQueue();
    }
    catch (JMSException e) {
      waitForRetry(e);
      m_temporaryQueue = trySessionProvider().getTemporaryQueue();
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
  public ISubscriptionStats getStats() {
    IJmsSessionProvider impl = m_impl;
    if (impl instanceof IJmsSessionProvider2) {
      return ((IJmsSessionProvider2) impl).getStats();
    }
    return null;
  }
}
