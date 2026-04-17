/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.cache;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.LambdaUtility;
import org.junit.Test;

public class BoundedResolveCacheWrapperTest {

  private ICache<Integer, Integer> m_lastCreatedCache;

  protected ICache<Integer, Integer> createCache(String id, ICacheValueResolver<Integer, Integer> resolver) {
    @SuppressWarnings("unchecked")
    ICacheBuilder<Integer, Integer> cacheBuilder = BEANS.get(ICacheBuilder.class);
    ICache<Integer, Integer> cache = cacheBuilder
        .withCacheId(id)
        .withValueResolver(resolver)
        .withThreadSafe(false)
        .withReplaceIfExists(true)
        .withMaxConcurrentResolve(1) // sets the BoundedResolveCacheWrapper, default reentrant
        .build();
    cache.invalidate(new AllCacheEntryFilter<>(), true);
    m_lastCreatedCache = cache;
    return cache;
  }

  @Test
  public void testReentrantSameThread() {
    AtomicInteger counter = new AtomicInteger(0);

    ICache<Integer, Integer> cache = createCache("BoundedResolveCacheWrapperTest_testReentrantSameThread", key -> {
      int count = counter.incrementAndGet();
      if (count % 2 == 1) {
        return m_lastCreatedCache.get(key);
      }
      return count;
    });

    assertEquals(2, (int) cache.get(1));
  }

  @Test
  public void testReentrantMultipleThreads() throws InterruptedException {
    AtomicInteger counter = new AtomicInteger(0);
    CountDownLatch startedLatch = new CountDownLatch(1);
    CountDownLatch returnLatch = new CountDownLatch(1);

    ICache<Integer, Integer> cache = createCache("BoundedResolveCacheWrapperTest_testReentrantMultipleThreads", key -> LambdaUtility.invokeSafely(() -> {
      int count = counter.incrementAndGet();
      if (count % 2 == 1) {
        return m_lastCreatedCache.get(key);
      }
      startedLatch.countDown();
      returnLatch.await();
      return count;
    }));

    // start 10 jobs
    List<IFuture<Integer>> futures = IntStream.range(0, 10).mapToObj(i -> Jobs.schedule(() -> cache.get(i), Jobs.newInput())).toList();

    // await at least one job has been started
    startedLatch.await();
    assertEquals(2, counter.get());

    // continue (return first result)
    returnLatch.countDown();

    // await and verify all results
    assertEquals(
        IntStream.rangeClosed(1, 10).mapToObj(i -> 2 * i).collect(Collectors.toSet()),
        futures.stream().map(f -> LambdaUtility.invokeSafely(() -> f.awaitDoneAndGet())).collect(Collectors.toSet()));
    assertEquals(20, counter.get());
  }
}
