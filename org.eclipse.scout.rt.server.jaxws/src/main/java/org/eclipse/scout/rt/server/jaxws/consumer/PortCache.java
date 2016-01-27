/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.consumer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.Service;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.quartz.SimpleScheduleBuilder;

/**
 * LRU-Cache for webservice Ports to be reused across multiple webservice calls. This cache improves performance because
 * Port creation is an expensive operation due to WSDL/schema validation.<br/>
 * This cache is based on a 'corePoolSize', meaning that that number of Ports is created on a preemptively basis. If
 * more Ports than that number are required, they are are created on demand and additionally added to the cache until
 * expired, which is useful at a high load.
 * <p>
 * Important: Ports are not thread-safe and should not be reused because JAX-WS API does not support to reset request
 * and response context.
 * <p>
 * This class is thread-safe.
 *
 * @since 5.1
 */
public class PortCache<PORT> {

  protected final Deque<PortCacheEntry<PORT>> m_queue;

  protected final PortProducer<? extends Service, PORT> m_portProducer;

  protected final int m_corePoolSize;

  protected final long m_timeToLive;

  /**
   * @param corePoolSize
   *          number of Ports to have preemptively in the cache.
   * @param timeToLive
   *          time-to-live [ms] for a Port in the cache if the 'corePoolSize' is exceeded.
   * @param portProducer
   *          factory to create new Ports.
   */
  public PortCache(final int corePoolSize, final long timeToLive, final PortProducer<? extends Service, PORT> portProducer) {
    this(corePoolSize, timeToLive, portProducer, new ConcurrentLinkedDeque<PortCacheEntry<PORT>>());
  }

  PortCache(final int corePoolSize, final long timeToLive, final PortProducer<? extends Service, PORT> portProducer, final Deque<PortCacheEntry<PORT>> queue) {
    m_corePoolSize = corePoolSize;
    m_timeToLive = timeToLive;
    m_portProducer = portProducer;
    m_queue = queue;
  }

  /**
   * Initializes the cache and asynchronously instantiates 'corePoolSize' ports.
   */
  public void init() {
    // Start periodic cleanup job.
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        discardExpiredPorts();
      }
    }, Jobs.newInput()
        .withName("Cleaning up JAX-WS port cache")
        .withRunContext(RunContexts.empty())
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.MINUTES)
            .withSchedule(SimpleScheduleBuilder.repeatMinutelyForever())));

    // Ensures to have at minimum 'corePoolSize' Ports in the cache.
    if (m_corePoolSize > 0) {
      Jobs.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          ensureCorePool();
        }
      }, Jobs.newInput()
          .withRunContext(RunContexts.empty())
          .withName("Initializing JAX-WS port cache"));
    }
  }

  /**
   * @return a new Port instance from cache. Please note, that a port should not be used concurrently across multiple
   *         threads because not being thread-safe.
   */
  public PORT get() {
    // Get oldest Port from queue.
    final PortCacheEntry<PORT> portCacheEntry = m_queue.poll();

    // Preemptively create a new port and put it into the cache.
    // Note: Do not invoke with current RunContext because the port will be used by any other invoker.
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        m_queue.offer(new PortCacheEntry<>(m_portProducer.produce(), m_timeToLive));
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withName("Producing PortType to be put into cache"));

    // Return port from cache, or create a new one.
    if (portCacheEntry != null) {
      return portCacheEntry.get();
    }
    else {
      return m_portProducer.produce();
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
      m_queue.offer(new PortCacheEntry<>(m_portProducer.produce(), m_timeToLive));
    }
  }

  /**
   * Represents a Port with its associated TTL.
   */
  protected static class PortCacheEntry<PORT> {

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
