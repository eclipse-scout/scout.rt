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

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Test;

/**
 * @since 5.2
 */
public class BasicCacheTest {
  private final static String CACHE_ID = "BasicCacheTestCacheId";

  protected ICache<Integer, String> createCache() {
    return createCache(new AbstractCacheValueResolver<Integer, String>() {
      private int m_counter = 0;

      @Override
      public String resolve(Integer key) {
        if (13 == key) {
          return null;
        }
        if (1337 == key) {
          throw new ProcessingException("Test exception - thrown");
        }
        m_counter++;
        return String.valueOf(key) + "." + String.valueOf(m_counter);
      }
    });
  }

  protected ICache<Integer, String> createCache(ICacheValueResolver<Integer, String> resolver) {
    @SuppressWarnings("unchecked")
    ICacheBuilder<Integer, String> cacheBuilder = BEANS.get(ICacheBuilder.class);
    return cacheBuilder.withCacheId(CACHE_ID).withValueResolver(resolver).withThreadSafe(false).build();
  }

  @Test
  public void testCacheBasic() {
    ICache<Integer, String> cache = createCache();

    // test get
    assertEquals("2.1", cache.get(2));
    assertEquals("2.1", cache.get(2));
    assertEquals("2.1", cache.get(2));
    assertEquals("5.2", cache.get(5));

    // test get all
    Map<Integer, String> resultMap = cache.getAll(CollectionUtility.arrayList(2, 7, 8, 9));
    assertEquals(4, resultMap.size());
    assertEquals("2.1", resultMap.get(2));
    assertEquals("2.1", cache.get(2));
    assertEquals("7.3", resultMap.get(7));
    assertEquals("7.3", cache.get(7));
    assertEquals("8.4", resultMap.get(8));
    assertEquals("8.4", cache.get(8));
    assertEquals("9.5", resultMap.get(9));
    assertEquals("9.5", cache.get(9));

    assertEquals(5, cache.getUnmodifiableMap().size());

    // test invalidate
    cache.invalidate(new KeyCacheEntryFilter<Integer, String>(CollectionUtility.arrayList(5, 8)), true);
    assertEquals("5.6", cache.get(5));
    assertEquals("8.7", cache.get(8));

    assertEquals(5, cache.getUnmodifiableMap().size());

    // test invalidate all
    cache.invalidate(new AllCacheEntryFilter<Integer, String>(), true);
    assertEquals(0, cache.getUnmodifiableMap().size());
  }

  @Test
  public void testCacheNullValues() {
    ICache<Integer, String> cache = createCache();

    assertEquals("2.1", cache.get(2));
    assertEquals(null, cache.get(null));

    // unresolvable keys
    assertEquals(null, cache.get(13));

    assertEquals(1, cache.getUnmodifiableMap().size());

    Map<Integer, String> resultMap = cache.getAll(CollectionUtility.arrayList(3, null));
    assertEquals(1, resultMap.size());
    assertEquals("3.2", resultMap.get(3));

    assertEquals(2, cache.getUnmodifiableMap().size());

    cache.invalidate(new KeyCacheEntryFilter<Integer, String>(CollectionUtility.arrayList(2, null)), true);
    assertEquals(1, cache.getUnmodifiableMap().size());
    assertEquals("3.2", resultMap.get(3));

    Map<Integer, String> emptyResultMap = cache.getAll(null);
    assertEquals(0, emptyResultMap.size());
    cache.invalidate(null, true);
  }

  @Test(expected = ProcessingException.class)
  public void testCacheExceptionDuringCreation() {
    ICache<Integer, String> cache = createCache();
    cache.get(1337);
  }

}
