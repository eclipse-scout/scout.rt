/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.Jobs;

/**
 * LRU-Cache for webservice Ports to be reused across multiple webservice calls. This cache improves performance
 * because Port creation is an expensive operation due to WSDL/schema validation.<br/>
 * This cache is based on a 'corePoolSize', meaning that that number of Ports is created on a preemptively basis. If
 * more Ports than that number is required, they are are created on demand and additionally added to the cache until
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

  protected final IPortProvider<PORT> m_portProvider;

  protected final int m_corePoolSize;

  protected final long m_ttl;

  protected boolean m_enabled;

  /**
   * @param enabled
   *          <code>true</code> to enable caching, <code>false</code> otherwise.
   * @param corePoolSize
   *          number of Ports to have preemptively in the cache.
   * @param ttl
   *          time-to-live for a Port in the cache if the 'corePoolSize' is exceeded.
   * @param portProvider
   *          factory to create new Ports.
   */
  public PortCache(final boolean enabled, final int corePoolSize, final long ttl, final IPortProvider<PORT> portProvider) {
    this(enabled, corePoolSize, ttl, portProvider, new ConcurrentLinkedDeque<PortCacheEntry<PORT>>());

    if (m_enabled) {
      // Start periodic cleanup job.
      Jobs.scheduleAtFixedRate(new IRunnable() {

        @Override
        public void run() throws Exception {
          discardExpiredPorts();
        }
      }, 1, 1, TimeUnit.MINUTES, Jobs.newInput(RunContexts.empty()).name("JAX-WS port cache cleanup"));

      // Ensures to have at minimum 'corePortSize' Ports in the pool.
      if (corePoolSize > 0) {
        Jobs.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            ensureCorePool();
          }
        }, Jobs.newInput(null));
      }
    }
  }

  PortCache(final boolean enabled, final int corePoolSize, final long ttl, final IPortProvider<PORT> portProvider, final Deque<PortCacheEntry<PORT>> queue) {
    m_enabled = enabled;
    m_corePoolSize = corePoolSize;
    m_ttl = ttl;
    m_portProvider = portProvider;
    m_queue = queue;
  }

  /**
   * @return a new Port instance from cache. Please note, that a port should not be used concurrently across multiple
   *         threads because not being thread-safe.
   */
  public PORT get() {
    if (!m_enabled) {
      return m_portProvider.provide();
    }

    // Get oldest Port from queue.
    final PortCacheEntry<PORT> portCacheEntry = m_queue.poll();

    // Preemptively create a new port and put it into the cache.
    Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        m_queue.offer(new PortCacheEntry<>(m_portProvider.provide(), m_ttl));
      }
    });

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
      m_queue.offer(new PortCacheEntry<>(m_portProvider.provide(), m_ttl));
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

  /**
   * Factory for new port objects.
   */
  public interface IPortProvider<PORT> {

    /**
     * @return a new Port, is not <code>null</code>.
     */
    PORT provide();
  }
}
