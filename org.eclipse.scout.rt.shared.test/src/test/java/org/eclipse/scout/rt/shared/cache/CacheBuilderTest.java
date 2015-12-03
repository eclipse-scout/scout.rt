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
package org.eclipse.scout.rt.shared.cache;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Test;

/**
 * @since 5.2
 */
public class CacheBuilderTest {
  private final static String CACHE_ID = "CacheBuilderTestCacheId";

  @Test
  public void testCacheBuilderInstance() {
    assertNotSame(BEANS.get(ICacheBuilder.class), BEANS.get(ICacheBuilder.class));
  }

  @Test
  public void testCacheBuilder() {
    @SuppressWarnings("unchecked")
    ICacheValueResolver<Integer, String> resolver = mock(AbstractCacheValueResolver.class);

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
