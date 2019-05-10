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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;

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
  protected volatile boolean m_closing;

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
    return !m_closing && m_impl != null;
  }

  public void close() {
    m_closing = true;
    synchronized (m_connectionFunction) {
      //close all sessions
      for (JmsSessionProviderWrapper s : m_sessionWrappers.keySet()) {
        s.close();
      }
      //close real connection
      if (m_impl != null) {
        try {
          m_impl.close();
        }
        catch (JMSException e) {
          BEANS.get(MomExceptionHandler.class).handle(e);
        }
        finally {
          m_impl = null;
        }
      }
    }
  }

  /**
   * Called upon failover
   */
  public void invalidate(JMSException e) {
    synchronized (m_connectionFunction) {
      try {
        try {
          //invalidate all sessions
          LOG.warn("invalidate connection and {} sessions due to '{}'", m_sessionWrappers.size(), e.getMessage());
          for (JmsSessionProviderWrapper s : m_sessionWrappers.keySet()) {
            s.invalidate();
          }
        }
        finally {
          //close real connection
          if (m_impl != null) {
            m_impl.close();
          }
        }
      }
      catch (JMSException e2) {
        BEANS.get(MomExceptionHandler.class).handle(e2);
      }
      finally {
        m_impl = null;
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
    synchronized (m_connectionFunction) {
      int retry = 0;
      while (true) {
        try {
          return tryConnectionInsideLock();
        }
        catch (JMSException e/*do not catch RuntimeException, this could catch the 'closed' exception*/) {
          retry++;
          waitForRetry(retry, e);
        }
      }
    }
  }

  protected Connection tryConnectionInsideLock() throws JMSException {
    if (m_closing) {
      throw new ProcessingException("closed");
    }
    if (m_impl != null) {
      return m_impl;
    }

    Connection tmp = null;
    try {
      tmp = m_connectionFunction.create();
      if (m_connectionRetryCount > 0) {
        tmp.setExceptionListener(new ExceptionListener() {
          @Override
          public void onException(final JMSException e1) {
            if (m_connectionRetryCount > 0) {
              LOG.info("JMS connection dropped; starting failover", e1);
              invalidate(e1);
              return;
            }
            BEANS.get(MomExceptionHandler.class).handle(e1);
          }
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
