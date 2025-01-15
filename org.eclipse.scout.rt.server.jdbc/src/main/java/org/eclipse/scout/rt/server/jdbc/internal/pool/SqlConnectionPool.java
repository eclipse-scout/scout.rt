/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jdbc.internal.pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.job.FixedDelayScheduleBuilder;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.opentelemetry.IHistogramViewHintProvider;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.TimingUtility;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.server.jdbc.AbstractSqlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;

/**
 * System-wide connection pool for pooling connections There is one pool for every ISqlService sub class type If
 * possible, every scout Session is provided with always the same connection it had in the last request this class is
 * thread-safe
 */
@Bean
@SuppressWarnings("squid:S1166")
public class SqlConnectionPool {
  private static final Logger LOG = LoggerFactory.getLogger(SqlConnectionPool.class);

  private static final AttributeKey<String> POOL_NAME = AttributeKey.stringKey("pool.name");
  private static final AttributeKey<String> CONNECTION_STATE = AttributeKey.stringKey("state");
  private static final String OTEL_METRIC_DB_CLIENT_CONNECTIONS_WAIT_TIME = "db.client.connections.wait_time";

  private volatile boolean m_destroyed;
  private final String m_identity = UUID.randomUUID().toString();

  /*
   * Instance
   */
  private final Object m_poolLock = new Object();
  private final Set<PoolEntry> m_idleEntries = new HashSet<>();
  private final Set<PoolEntry> m_busyEntries = new HashSet<>();
  private volatile String m_name;
  private volatile int m_poolSize;
  private volatile long m_connectionLifetime;
  private volatile long m_connectionBusyTimeout;
  private final AtomicBoolean m_initialized = new AtomicBoolean(false);
  /*
   * OpenTelemetry
   */
  private DoubleHistogram m_connectionWaitTime;
  private Attributes m_defaultAttributes;

  public void initialize(String name, int poolSize, long connectionLifetime, long connectionBusyTimeout) {
    Assertions.assertTrue(m_initialized.compareAndSet(false, true), "already initialized");
    m_name = name;
    m_poolSize = poolSize;
    m_connectionLifetime = connectionLifetime;
    m_connectionBusyTimeout = connectionBusyTimeout;
    startManagePool();
    initMetrics();
  }

  /**
   * Start managing pool
   */
  private void startManagePool() {
    Jobs.schedule(this::managePool, Jobs.newInput()
        .withName("Managing SQL connection pool for {}", m_name)
        .withExecutionHint(m_identity)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.MINUTES)
            .withSchedule(FixedDelayScheduleBuilder.repeatForever(1, TimeUnit.MINUTES))));
  }

  /**
   * @see <a href=
   * "https://opentelemetry.io/docs/specs/otel/metrics/semantic_conventions/database-metrics/">OpenTelemetry:
   * Semantic Conventions for Database Metrics</a>
   */
  private void initMetrics() {
    Meter meter = GlobalOpenTelemetry.get().getMeter("scout.SqlConnectionPool");

    ObservableLongMeasurement connectionsUsage = meter.upDownCounterBuilder("db.client.connections.usage")
        .setDescription("The number of connections that are currently in state described by the state attribute.")
        .setUnit("{connection}")
        .buildObserver();
    ObservableLongMeasurement maxConnections = meter.upDownCounterBuilder("db.client.connections.max")
        .setDescription("The maximum number of open connections allowed.")
        .setUnit("{connection}")
        .buildObserver();
    m_connectionWaitTime = meter.histogramBuilder(OTEL_METRIC_DB_CLIENT_CONNECTIONS_WAIT_TIME)
        .setUnit("ms")
        .setDescription("The time it took to obtain an open connection from the pool.")
        .build();

    m_defaultAttributes = Attributes.of(POOL_NAME, m_name);
    Attributes idleConnectionsAttributes = m_defaultAttributes.toBuilder().put(CONNECTION_STATE, "idle").build();
    Attributes usedConnectionsAttributes = m_defaultAttributes.toBuilder().put(CONNECTION_STATE, "used").build();
    //noinspection resource
    meter.batchCallback(() -> {
          connectionsUsage.record(m_idleEntries.size(), idleConnectionsAttributes);
          connectionsUsage.record(m_busyEntries.size(), usedConnectionsAttributes);
          maxConnections.record(m_poolSize, m_defaultAttributes);
        },
        connectionsUsage,
        maxConnections);
  }

  public Connection leaseConnection(AbstractSqlService service) throws ClassNotFoundException, SQLException {
    final long startTime = System.nanoTime();
    managePool();
    synchronized (m_poolLock) {
      Assertions.assertFalse(isDestroyed(), "{} not available because destroyed.", getClass().getSimpleName());

      PoolEntry candidate = null;
      while (candidate == null) {
        // get next available conn
        for (PoolEntry m_idleEntry : m_idleEntries) {
          candidate = m_idleEntry;
          break;
        }
        if (candidate == null && m_idleEntries.size() + m_busyEntries.size() < m_poolSize) {
          // create new connection
          PoolEntry test = new PoolEntry();
          test.conn = new SqlConnectionBuilder().createJdbcConnection(service);
          LOG.info("created jdbc connection {}", test.conn);
          service.callbackAfterConnectionCreated(test.conn);
          test.createTime = System.currentTimeMillis();
          m_idleEntries.add(test);
          candidate = test;
        }
        if (candidate == null) {
          // wait
          try {
            m_poolLock.wait();
          }
          catch (InterruptedException ie) {
            Thread.currentThread().interrupt(); // Restore the thread's interrupted status because cleared by catching {@link java.lang.InterruptedException}.
            throw new ThreadInterruptedError("Interrupted while leasing database connection");
          }
        }
        // test candidate connection
        if (candidate != null) {
          try {
            service.callbackTestConnection(candidate.conn);
          }
          catch (Exception e) {
            // remove candidate from idle pool and close it
            m_idleEntries.remove(candidate);
            LOG.warn("closing dirty connection: {}", candidate.conn, e);
            try {
              candidate.conn.close();
            }
            catch (Exception fatal) {
              LOG.warn("could not close candidate connection", fatal);
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
      LOG.debug("lease   {}", candidate.conn);
      double elapsedAcquired = TimingUtility.msElapsed(startTime);
      m_connectionWaitTime.record(elapsedAcquired, m_defaultAttributes);
      return candidate.conn;
    }
  }

  public void releaseConnection(Connection conn) {
    LOG.debug("release {}", conn);
    synchronized (m_poolLock) {
      Assertions.assertFalse(isDestroyed(), "{} not available because destroyed.", getClass().getSimpleName());

      PoolEntry candidate = null;
      for (Iterator it = m_busyEntries.iterator(); it.hasNext(); ) {
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
        catch (Exception e) {
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
          }
        }
        catch (Exception e) {
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
        LOG.warn("closing dirty connection: {}", conn);
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
      buf.append("Total connections: ").append(m_busyEntries.size() + m_idleEntries.size());
      buf.append("\n");
      buf.append("Busy: ").append(m_busyEntries.size());
      buf.append("\n");
      for (PoolEntry e : m_busyEntries) {
        buf.append("  class=").append(e.conn.getClass().getName()).append(", created=").append(fmt.format(new Date(e.createTime))).append(", leaseCount=").append(e.leaseCount).append(", leaseBegin=")
            .append(fmt.format(new Date(e.leaseBegin)));
        buf.append("\n");
      }
      buf.append("Idle: ").append(m_idleEntries.size());
      buf.append("\n");
      for (PoolEntry e : m_idleEntries) {
        buf.append("  class=").append(e.conn.getClass().getName()).append(", created=").append(fmt.format(new Date(e.createTime))).append(", leaseCount=").append(e.leaseCount);
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
        if (isDestroyed()) {
          return;
        }

        // close old idle connections
        for (Iterator it = m_idleEntries.iterator(); it.hasNext(); ) {
          PoolEntry e = (PoolEntry) it.next();
          if (System.currentTimeMillis() - e.createTime > m_connectionLifetime) {
            closeConnectionAsync(e.conn, "expired idle connection");
            e.conn = null;
            it.remove();
          }
        }
        // close timed out busy connections
        for (Iterator it = m_busyEntries.iterator(); it.hasNext(); ) {
          PoolEntry e = (PoolEntry) it.next();
          if (System.currentTimeMillis() - e.leaseBegin > m_connectionBusyTimeout) {
            closeConnectionAsync(e.conn, "timed out busy connection");
            e.conn = null;
            it.remove();
          }
        }
      }
    }
    catch (Exception t) {
      LOG.warn("Unexpected Problem while managing SQL connection pool", t);
    }
  }

  /**
   * Returns whether this SQL pool was destroyed, and cannot be used anymore.
   */
  public boolean isDestroyed() {
    return m_destroyed;
  }

  /**
   * Destroys this connection pool. Upon return, this pool cannot be used anymore.
   */
  public void destroy() {
    if (isDestroyed()) {
      return;
    }

    synchronized (m_poolLock) {
      if (isDestroyed()) {
        return; // double-checked locking
      }
      m_destroyed = true;

      // Cancel jobs.
      Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
          .andMatchExecutionHint(m_identity)
          .toFilter(), true);

      for (final PoolEntry idleEntry : m_idleEntries) {
        closeConnectionAsync(idleEntry.conn, "destroying SQL connection pool");
      }
      m_idleEntries.clear();

      for (final PoolEntry busyEntry : m_busyEntries) {
        closeConnectionAsync(busyEntry.conn, "destroying SQL connection pool");
      }
      m_busyEntries.clear();
    }
  }

  protected void closeConnectionAsync(final Connection connection, final String reason) {
    Jobs.schedule(() -> {
      LOG.info("Closing SQL connection {}", connection);
      try {
        connection.close();
      }
      catch (SQLException e) {
        LOG.error("Failed to close SQL connection [connection={}]", connection, e);
      }
    }, Jobs.newInput()
        .withName("Closing SQL connection [name={}, connection={}, reason={}]", m_name, connection, reason)
        .withExecutionHint(m_identity));
  }

  /**
   * Custom histogramm buckets for <code>db.client.connections.wait_time</code> (time unit: milliseconds).
   *
   * @see #m_connectionWaitTime
   */
  public static class WaitTimeHistogramViewHintProvider implements IHistogramViewHintProvider {

    @Override
    public String getInstrumentName() {
      return OTEL_METRIC_DB_CLIENT_CONNECTIONS_WAIT_TIME;
    }

    @Override
    public List<Double> getExplicitBuckets() {
      return List.of(1d, 2d, 5d, 10d, 25d, 50d, 100d, 500d, 1_000d, 5_000d);
    }
  }
}
