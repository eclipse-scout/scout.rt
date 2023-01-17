/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.consumer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.quartz.SimpleScheduleBuilder;

/**
 * Cache - actually a stash - for web service Ports. This class improves performance because Port creation is an
 * expensive operation due to WSDL/schema validation.<br/>
 * This cache is based on a 'corePoolSize', meaning that that number of Ports is created on a preemptively basis. If
 * more Ports than that number are required, they are are created on demand and additionally added to the cache until
 * expired, which is useful at a high load.
 * <p>
 * This class is thread-safe.
 *
 * @since 5.1
 */
public class PortCache<PORT> implements IPortProvider<PORT> {

  protected final Deque<PortCacheEntry<PORT>> m_queue;

  protected final IPortProvider<PORT> m_portProvider;

  protected final int m_corePoolSize;

  protected final long m_timeToLive;

  /**
   * @param corePoolSize
   *          number of Ports to have preemptively in the cache.
   * @param timeToLive
   *          time-to-live [ms] for a Port in the cache if the 'corePoolSize' is exceeded.
   * @param portProvider
   *          factory to create new Ports.
   */
  public PortCache(final int corePoolSize, final long timeToLive, final IPortProvider<PORT> portProvider) {
    this(corePoolSize, timeToLive, portProvider, new ConcurrentLinkedDeque<>());
  }

  PortCache(final int corePoolSize, final long timeToLive, final IPortProvider<PORT> portProvider, final Deque<PortCacheEntry<PORT>> queue) {
    m_corePoolSize = corePoolSize;
    m_timeToLive = timeToLive;
    m_portProvider = portProvider;
    m_queue = queue;
  }

  /**
   * Initializes the cache and asynchronously instantiates 'corePoolSize' ports.
   */
  public void init() {
    // Start periodic cleanup job.
    Jobs.schedule(this::discardExpiredPorts, Jobs.newInput()
        .withName("Cleaning up JAX-WS port cache")
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.MINUTES)
            .withSchedule(SimpleScheduleBuilder.repeatMinutelyForever())));

    // Ensures to have at minimum 'corePoolSize' Ports in the cache.
    if (m_corePoolSize > 0) {
      Jobs.schedule(this::ensureCorePool, Jobs.newInput()
          .withName("Initializing JAX-WS port cache"));
    }
  }

  @Override
  public PORT provide() {
    // Get oldest Port from queue.
    final PortCacheEntry<PORT> portCacheEntry = m_queue.poll();

    // Preemptively create a new port and put it into the cache.
    // Note: Do not invoke with current RunContext because the port will be used by any other invoker.
    Jobs.schedule(() -> {
      m_queue.offer(new PortCacheEntry<>(m_portProvider.provide(), m_timeToLive));
    }, Jobs.newInput()
        .withName("Producing PortType to be put into cache"));

    // Return port from cache, or create a new one.
    if (portCacheEntry != null) {
      return portCacheEntry.get();
    }
    else {
      return m_portProvider.provide();
    }
  }

  /**
   * Discards all expired ports from the cache, but respects the 'corePoolSize'.
   */
  protected void discardExpiredPorts() {
    while (m_queue.size() > m_corePoolSize) {
      final PortCacheEntry<PORT> candidate = m_queue.pollFirst();
      if (candidate == null) {
        break;
      }
      else if (!candidate.isExpired()) {
        m_queue.offerFirst(candidate); // Port is valid, put it back into the queue.
        break;
      }
    }
  }

  /**
   * Ensures to have at minimum 'corePoolSize' ports in the pool.
   */
  protected void ensureCorePool() {
    while (m_queue.size() < m_corePoolSize) {
      m_queue.offer(new PortCacheEntry<>(m_portProvider.provide(), m_timeToLive));
    }
  }

  /**
   * Represents a Port with its associated TTL.
   */
  protected static final class PortCacheEntry<PORT> {

    private final PORT m_port;
    private long m_expiration;

    private PortCacheEntry(final PORT port, final long ttl) {
      m_expiration = System.currentTimeMillis() + ttl;
      m_port = port;
    }

    PORT get() {
      return m_port;
    }

    boolean isExpired() {
      return m_expiration < System.currentTimeMillis();
    }

    void setExpirationDate(final long expirationDate) {
      m_expiration = expirationDate;
    }

    @Override
    public String toString() {
      final ToStringBuilder builder = new ToStringBuilder(this);
      builder.ref("port", m_port);
      builder.attr("expiration", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date(m_expiration)));
      return builder.toString();
    }
  }
}
