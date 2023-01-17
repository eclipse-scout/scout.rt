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

import static org.junit.Assert.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.server.jaxws.consumer.PortCache.PortCacheEntry;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class PortCacheTest {

  private IPortProvider<Object> m_portProvider;

  @Before
  public void before() {
    m_portProvider = () -> new Object();
  }

  @Test
  public void testCorePoolSize() {
    Deque<PortCacheEntry<Object>> queue = new ArrayDeque<>();
    PortCache<Object> cache = new PortCache<>(5, TimeUnit.HOURS.toMillis(1), m_portProvider, queue);

    assertEquals(0, queue.size());
    cache.ensureCorePool();
    assertEquals(5, queue.size());

    Object port1 = cache.provide();
    awaitAndAssertQueueSize(queue, 5);
    Object port2 = cache.provide();
    awaitAndAssertQueueSize(queue, 5);
    Object port3 = cache.provide();
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

    cache.provide();
    awaitAndAssertQueueSize(queue, 1);
    cache.provide();
    awaitAndAssertQueueSize(queue, 1);
    cache.provide();
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

      SleepUtil.sleepSafe(10, TimeUnit.MILLISECONDS);
    }
  }
}
