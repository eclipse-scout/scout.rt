/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.cache;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Test;

/**
 * @since 5.2
 */
public class CacheBuilderTest {
  private static final String CACHE_ID = "CacheBuilderTestCacheId";

  @Test
  public void testCacheBuilderInstance() {
    assertNotSame(BEANS.get(ICacheBuilder.class), BEANS.get(ICacheBuilder.class));
  }

  @Test
  public void testCacheBuilder() {
    @SuppressWarnings("unchecked")
    ICacheValueResolver<Integer, String> resolver = mock(ICacheValueResolver.class);

    @SuppressWarnings("unchecked")
    ICacheBuilder<Integer, String> cacheBuilder = BEANS.get(ICacheBuilder.class);

    ICache<Integer, String> cache =
        cacheBuilder.withCacheId(CACHE_ID).withMaxConcurrentResolve(2).withValueResolver(resolver).withAdditionalCustomWrapper(TestCacheWrapper.class).withAdditionalCustomWrapper(BoundedResolveCacheWrapper.class, 12).build();

    assertTrue(cache instanceof BoundedResolveCacheWrapper);
    ICache<Integer, String> cacheDelegate = ((AbstractCacheWrapper<Integer, String>) cache).getDelegate();
    assertTrue(cacheDelegate instanceof TestCacheWrapper);
    cacheDelegate = ((AbstractCacheWrapper<Integer, String>) cacheDelegate).getDelegate();
    assertTrue(cacheDelegate instanceof BoundedResolveCacheWrapper);
    cacheDelegate = ((AbstractCacheWrapper<Integer, String>) cacheDelegate).getDelegate();
    assertTrue(cacheDelegate instanceof BasicCache);
  }

  protected static class TestCacheWrapper extends AbstractCacheWrapper<Integer, String> {

    public TestCacheWrapper(ICache<Integer, String> delegate) {
      super(delegate);
    }
  }
}
