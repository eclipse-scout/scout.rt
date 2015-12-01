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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.Service;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.server.jaxws.consumer.PortCache.PortCacheEntry;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class PortCacheTest {

  private PortProducer<Service, Object> m_portProvider;

  @Before
  public void before() {
    m_portProvider = new PortProducer<Service, Object>(null, null, null, null, null, null) {

      @Override
      public Object produce() {
        return new Object();
      }
    };
  }

  @Test
  public void testCorePoolSize() {
    Deque<PortCacheEntry<Object>> queue = new ArrayDeque<>();
    PortCache<Object> cache = new PortCache<>(5, TimeUnit.HOURS.toMillis(1), m_portProvider, queue);

    assertEquals(0, queue.size());
    cache.ensureCorePool();
    assertEquals(5, queue.size());

    Object port1 = cache.get();
    awaitAndAssertQueueSize(queue, 5);
    Object port2 = cache.get();
    awaitAndAssertQueueSize(queue, 5);
    Object port3 = cache.get();
    awaitAndAssertQueueSize(queue, 5);

    // assert different ports
    assertEquals(3, CollectionUtility.hashSet(port1, port2, port3).size());

    cache.discardExpiredPorts();
    assertEquals(5, queue.size());

    // core pool ports must not expire
    queue.getFirst().setExpirationDate(-1);
    cache.discardExpiredPorts();
    assertEquals(5, queue.size());
  }

  @Test
  public void testNoCorePoolSize() {
    Deque<PortCacheEntry<Object>> queue = new ArrayDeque<>();
    PortCache<Object> cache = new PortCache<>(0, TimeUnit.HOURS.toMillis(1), m_portProvider, queue);

    assertEquals(0, queue.size());
    cache.ensureCorePool();
    assertEquals(0, queue.size());

    cache.get();
    awaitAndAssertQueueSize(queue, 1);
    cache.get();
    awaitAndAssertQueueSize(queue, 1);
    cache.get();
    awaitAndAssertQueueSize(queue, 1);

    cache.discardExpiredPorts();
    assertEquals(1, queue.size());

    queue.getFirst().setExpirationDate(-1);
    cache.discardExpiredPorts();
    assertEquals(0, queue.size());

    cache.discardExpiredPorts();
    assertEquals(0, queue.size());
  }

  private static void awaitAndAssertQueueSize(Deque<?> queue, int expectedSize) {
    long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(15);

    while (queue.size() != expectedSize) {
      String msg = String.format("Timeout elapsed while waiting for queue size to become expected size [actualSize=%s, expectedSize=%s]", queue.size(), expectedSize);
      assertTrue(msg, deadline > System.currentTimeMillis());

      try {
        Thread.sleep(10);
      }
      catch (InterruptedException e) {
        // NOOP
      }
    }
  }
}
