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
package org.eclipse.scout.rt.server.jdbc.internal.pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.server.jdbc.AbstractSqlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * System-wide connection pool for pooling connections There is one pool for every ISqlService sub class type If
 * possible, every scout Session is provided with always the same connection it had in the last request this class is
 * thread-safe
 */
public final class SqlConnectionPool {
  private static final Logger LOG = LoggerFactory.getLogger(SqlConnectionPool.class);

  /*
   * Pool factory per service type (top-level class)
   */
  private static final Object poolStoreLock = new Object();
  private static final Map<Class, SqlConnectionPool> poolStore = new HashMap<Class, SqlConnectionPool>();

  public static SqlConnectionPool getPool(Class serviceType, int poolSize, long connectionLifetime, long connectionBusyTimeout) {
    synchronized (poolStoreLock) {
      SqlConnectionPool pool = poolStore.get(serviceType);
      if (pool == null) {
        pool = new SqlConnectionPool(serviceType, poolSize, connectionLifetime, connectionBusyTimeout);
        poolStore.put(serviceType, pool);
      }
      return pool;
    }
  }

  /*
   * Instance
   */
  private final Object m_poolLock = new Object();
  private final Set<PoolEntry> m_idleEntries = new HashSet<PoolEntry>();
  private final Set<PoolEntry> m_busyEntries = new HashSet<PoolEntry>();
  private final Class m_serviceType;
  private final int m_poolSize;
  private final long m_connectionLifetime;
  private final long m_connectionBusyTimeout;

  private SqlConnectionPool(Class serviceType, int poolSize, long connectionLifetime, long connectionBusyTimeout) {
    m_serviceType = serviceType;
    m_poolSize = poolSize;
    m_connectionLifetime = connectionLifetime;
    m_connectionBusyTimeout = connectionBusyTimeout;
    Thread t = new Thread("SqlConnectionPool[" + m_serviceType.getName() + "].managePool") {
      @Override
      public void run() {
        while (true) {
          try {
            Thread.sleep(60000L);
          }
          catch (InterruptedException ie) {
          }
          managePool();
        }
      }
    };
    t.setDaemon(true);
    t.start();
  }

  public Connection leaseConnection(AbstractSqlService service) throws Exception {
    managePool();
    synchronized (m_poolLock) {
      PoolEntry candidate = null;
      while (candidate == null) {
        if (candidate == null) {
          // get next available conn
          for (Iterator it = m_idleEntries.iterator(); it.hasNext();) {
            candidate = (PoolEntry) it.next();
            break;
          }
        }
        if (candidate == null) {
          if (m_idleEntries.size() + m_busyEntries.size() < m_poolSize) {
            // create new connection
            PoolEntry test = new PoolEntry();
            test.conn = new SqlConnectionBuilder().createJdbcConnection(service);
            if (LOG.isInfoEnabled()) {
              LOG.info("created jdbc connection " + test.conn);
            }
            service.callbackAfterConnectionCreated(test.conn);
            test.createTime = System.currentTimeMillis();
            m_idleEntries.add(test);
            candidate = test;
          }
        }
        if (candidate == null) {
          // wait
          try {
            m_poolLock.wait();
          }
          catch (InterruptedException ie) {
            throw ie;
          }
        }
        // test candidate connection
        if (candidate != null) {
          try {
            service.callbackTestConnection(candidate.conn);
          }
          catch (Throwable t) {
            // remove candidate from idle pool and close it
            m_idleEntries.remove(candidate);
            LOG.warn("closing dirty connection: " + candidate.conn);
            try {
              candidate.conn.close();
            }
            catch (Throwable fatal) {
            }
            candidate = null;
          }
        }
      }// end while
       // move to busy pool
      m_idleEntries.remove(candidate);
      candidate.leaseBegin = System.currentTimeMillis();
      candidate.leaseCount++;
      m_busyEntries.add(candidate);
      if (LOG.isDebugEnabled()) {
        LOG.debug("lease   " + candidate.conn);
      }
      return candidate.conn;
    }
  }

  public void releaseConnection(Connection conn) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("release " + conn);
    }
    synchronized (m_poolLock) {
      PoolEntry candidate = null;
      for (Iterator it = m_busyEntries.iterator(); it.hasNext();) {
        PoolEntry e = (PoolEntry) it.next();
        if (e.conn == conn) {
          candidate = e;
          it.remove();
          break;
        }
      }
      // check close status of connection
      if (candidate != null) {
        try {
          if (candidate.conn.isClosed()) {
            candidate = null;
          }
        }
        catch (Throwable e) {
          // ignore
          candidate = null;
        }
      }
      // check error status of connection
      if (candidate != null) {
        try {
          if (candidate.conn.getWarnings() != null) {
            /*
             * connection is normally valid again after clearing the warnings.
             * Since oracle is not supporting warnings, the subsequent call has no effect!
             */
            candidate.conn.clearWarnings();
            // candidate=null;
          }
        }
        catch (Throwable e) {
          // ignore
          candidate = null;
        }
      }
      // all checks passed, candidate is either non-null or null, in the latter
      // case conn is a dirty connection
      // back to idle pool
      if (candidate != null) {
        // move to idle pool
        candidate.leaseBegin = 0;
        m_idleEntries.add(candidate);
      }
      else {
        LOG.warn("closing dirty connection: " + conn);
        try {
          conn.close();
        }
        catch (SQLException e) {
          // ignored
        }
      }
      m_poolLock.notifyAll();
    }
    managePool();
  }

  public String getInventory() {
    StringBuilder buf = new StringBuilder();
    SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSSS");
    synchronized (m_poolLock) {
      buf.append("Total connections: " + (m_busyEntries.size() + m_idleEntries.size()));
      buf.append("\n");
      buf.append("Busy: " + m_busyEntries.size());
      buf.append("\n");
      for (PoolEntry e : m_busyEntries) {
        buf.append("  class=" + e.conn.getClass().getName() + ", created=" + fmt.format(new Date(e.createTime)) + ", leaseCount=" + e.leaseCount + ", leaseBegin=" + fmt.format(new Date(e.leaseBegin)));
        buf.append("\n");
      }
      buf.append("Idle: " + m_idleEntries.size());
      buf.append("\n");
      for (PoolEntry e : m_idleEntries) {
        buf.append("  class=" + e.conn.getClass().getName() + ", created=" + fmt.format(new Date(e.createTime)) + ", leaseCount=" + e.leaseCount);
        buf.append("\n");
      }
    }
    return buf.toString();
  }

  /**
   * Thread worker to manage pool
   */
  private void managePool() {
    try {
      synchronized (m_poolLock) {
        // close old idle connections
        for (Iterator it = m_idleEntries.iterator(); it.hasNext();) {
          PoolEntry e = (PoolEntry) it.next();
          if (System.currentTimeMillis() - e.createTime > m_connectionLifetime) {
            ConnectionCloseThread t = new ConnectionCloseThread("CloseOldIdleConnection for " + m_serviceType.getName(), e.conn);
            t.start();
            e.conn = null;
            it.remove();
          }
        }
        // close timed out busy connections
        for (Iterator it = m_busyEntries.iterator(); it.hasNext();) {
          PoolEntry e = (PoolEntry) it.next();
          if (System.currentTimeMillis() - e.leaseBegin > m_connectionBusyTimeout) {
            ConnectionCloseThread t = new ConnectionCloseThread("CloseTimeoutBusyConnection for " + m_serviceType.getName(), e.conn);
            t.start();
            e.conn = null;
            it.remove();
          }
        }
      }
    }
    catch (Throwable t) {
      LOG.warn(null, t);
    }
  }
}
