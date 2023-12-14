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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;

import org.eclipse.scout.rt.mom.api.IMom.ConnectionRetryCountProperty;
import org.eclipse.scout.rt.mom.api.IMom.ConnectionRetryIntervalMillisProperty;
import org.eclipse.scout.rt.mom.api.IMom.SessionRetryIntervalMillisProperty;
import org.eclipse.scout.rt.mom.api.IMomImplementor;
import org.eclipse.scout.rt.mom.jms.ICreateJmsConnection;
import org.eclipse.scout.rt.mom.jms.IJmsSessionProvider;
import org.eclipse.scout.rt.mom.jms.JmsMomImplementor.MomExceptionHandler;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMS connection wrapper that supports client side connection failover
 *
 * @since 6.1
 */
public class JmsConnectionWrapper {
  private static final Logger LOG = LoggerFactory.getLogger(JmsConnectionWrapper.class);
  /**
   * There is no ConcurrentHashSet. This is the non-null value for {@link #m_sessionWrappers}
   */
  private static final Object NOT_NULL = new Object();

  protected final Map<JmsSessionProviderWrapper, Object> m_sessionWrappers = new ConcurrentHashMap<>();
  protected ICreateJmsConnection m_connectionFunction;
  protected int m_connectionRetryCount;
  protected long m_connectionRetryIntervalMillis;
  protected long m_sessionRetryIntervalMillis;

  protected volatile Connection m_impl;
  protected final AtomicBoolean m_closing = new AtomicBoolean();

  public JmsConnectionWrapper(Map<Object, Object> properties) {
    m_connectionRetryCount = ObjectUtility.nvl(
        TypeCastUtility.castValue(properties.get(IMomImplementor.CONNECTION_RETRY_COUNT), Integer.class),
        CONFIG.getPropertyValue(ConnectionRetryCountProperty.class));
    m_connectionRetryIntervalMillis = ObjectUtility.nvl(
        TypeCastUtility.castValue(properties.get(IMomImplementor.CONNECTION_RETRY_INTERVAL_MILLIS), Integer.class),
        CONFIG.getPropertyValue(ConnectionRetryIntervalMillisProperty.class));
    m_sessionRetryIntervalMillis = ObjectUtility.nvl(
        TypeCastUtility.castValue(properties.get(IMomImplementor.SESSION_RETRY_INTERVAL_MILLIS), Integer.class),
        CONFIG.getPropertyValue(SessionRetryIntervalMillisProperty.class));
  }

  public boolean isClosing() {
    return m_closing.get();
  }

  public ICreateJmsConnection getConnectionFunction() {
    return m_connectionFunction;
  }

  public JmsConnectionWrapper withConnectionFunction(ICreateJmsConnection connectionFunction) {
    m_connectionFunction = connectionFunction;
    return this;
  }

  public int getConnectionRetryCount() {
    return m_connectionRetryCount;
  }

  /**
   * Number of times to retry connecting. A value of 0 disables connection-failover
   */
  public JmsConnectionWrapper withConnectionRetryCount(int connectionRetryCount) {
    m_connectionRetryCount = connectionRetryCount;
    return this;
  }

  public long getConnectionRetryIntervalMillis() {
    return m_connectionRetryIntervalMillis;
  }

  /**
   * Interval in milliseconds between reconnection attempts
   */
  public JmsConnectionWrapper withConnectionRetryIntervalMillis(long connectionRetryIntervalMillis) {
    m_connectionRetryIntervalMillis = connectionRetryIntervalMillis;
    return this;
  }

  public long getSessionRetryIntervalMillis() {
    return m_sessionRetryIntervalMillis;
  }

  /**
   * If {@link #withConnectionRetryCount(int)} is enabled then every call in {@link IJmsSessionProvider} tries a second
   * time on {@link JMSException}. This is the interval to wait inbetween these two attempts.
   */
  public JmsConnectionWrapper withSessionRetryIntervalMillis(long sessionRetryIntervalMillis) {
    m_sessionRetryIntervalMillis = sessionRetryIntervalMillis;
    return this;
  }

  public boolean isConnected() {
    return !m_closing.get() && m_impl != null;
  }

  public void close() {
    if (!m_closing.compareAndSet(false, true)) {
      return;
    }
    //this thread is closing
    synchronized (m_connectionFunction) {
      //close all sessions
      int n = m_sessionWrappers.size();
      LOG.info("close {} connection and {} sessions", m_impl != null ? 1 : 0, n);
      int i = 0;
      for (JmsSessionProviderWrapper s : m_sessionWrappers.keySet()) {
        i++;
        LOG.info("closing session {} of {}", i, n);
        s.close();
      }
      //close real connection
      if (m_impl == null) {
        return;
      }
      try {
        LOG.info("closing connection");
        m_impl.close();
        LOG.info("connection closed");
      }
      catch (JMSException e) {
        BEANS.get(MomExceptionHandler.class).handle(e);
      }
      finally {
        m_impl = null;
      }
    }
  }

  /**
   * Called upon failover
   */
  public void invalidate(Connection affectedConnection, JMSException e) {
    if (isClosing()) {
      return;
    }

    synchronized (m_connectionFunction) {
      if (m_impl != affectedConnection) {
        return;
      }
      if (isClosing()) {
        return;
      }
      try {
        try {
          //invalidate all sessions
          int n = m_sessionWrappers.size();
          LOG.warn("invalidate {} connection and {} sessions due to '{}'", m_impl != null ? 1 : 0, m_sessionWrappers.size(), e.getMessage());
          int i = 0;
          for (JmsSessionProviderWrapper s : m_sessionWrappers.keySet()) {
            i++;
            LOG.info("invalidating session {} of {}", i, n);
            s.invalidate();
          }
        }
        finally {
          //close real connection
          if (m_impl != null) {
            try {
              LOG.info("invalidating connection");
              m_impl.close();
              LOG.info("connection invalidated");
            }
            finally {
              m_impl = null;
            }
          }
        }
      }
      catch (JMSException e2) {
        BEANS.get(MomExceptionHandler.class).handle(e2);
      }
    }
  }

  /**
   * @return the current connection, may block until a connection is available
   *         <p>
   *         Do not keep references to this value, it may change after reconnect attempts.
   * @throws JMSException
   */
  public Connection getConnection() throws JMSException {
    int retry = 0;
    //loop without locking
    while (true) {
      try {
        return tryConnection();
      }
      catch (JMSException e/*do not catch RuntimeException, this could catch the 'closed' exception*/) {
        retry++;
        waitForRetry(retry, e);
      }
    }
  }

  protected Connection tryConnection() throws JMSException {
    synchronized (m_connectionFunction) {
      if (isClosing()) {
        throw new ProcessingException("closed");
      }
      if (m_impl != null) {
        return m_impl;
      }
      Connection tmp = null;
      try {
        tmp = m_connectionFunction.create();
        if (isClosing()) {
          throw new ProcessingException("closed");
        }
        if (m_connectionRetryCount > 0) {
          final Connection newConnection = tmp;
          tmp.setExceptionListener(e1 -> {
            if (m_connectionRetryCount > 0) {
              LOG.info("JMS connection dropped; starting failover", e1);
              invalidate(newConnection, e1);
              return;
            }
            BEANS.get(MomExceptionHandler.class).handle(e1);
          });
        }
        //success
        m_impl = tmp;
        LOG.info("JMS connection established: {}", m_impl);
        return m_impl;
      }
      finally {
        //detect failure
        if (m_impl == null && tmp != null) {
          try {
            tmp.close();
          }
          catch (JMSException e2) {
            LOG.info("Close invalid connection", e2);
          }
        }
      }
    }//end lock
  }

  /**
   * Check if retry should be done and wait some time
   *
   * @throws JMSException
   */
  protected void waitForRetry(int retry, JMSException e) throws JMSException {
    if (retry > m_connectionRetryCount) {
      if (m_connectionRetryCount > 0) {
        LOG.warn("JMS connection unavailable '{}'; fail after {} retry", e, m_connectionRetryCount);
      }
      throw e;
    }
    LOG.info("JMS connection unavailable '{}'; retry #{} in {} seconds", e, retry, (m_connectionRetryIntervalMillis / 1000L));
    try {
      Thread.sleep(m_connectionRetryIntervalMillis);
    }
    catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      throw new ThreadInterruptedError("Interrupted", ie);
    }
  }

  protected void registerSessionProvider(JmsSessionProviderWrapper w) {
    m_sessionWrappers.put(w, NOT_NULL);
  }

  protected void unregisterSessionProvider(JmsSessionProviderWrapper w) {
    m_sessionWrappers.remove(w);
  }

  @Override
  public String toString() {
    return "" + m_impl;
  }
}
