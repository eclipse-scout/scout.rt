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

import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Test;

/**
 * @since 5.2
 */
public class BasicCacheTest {

  protected ICache<Integer, String> createCache(String id) {
    return createCache(id, new ICacheValueResolver<Integer, String>() {
      private int m_counter;
      @Override
      public String resolve(Integer key) {
        if (13 == key) {
          return null;
        }
        if (1337 == key) {
          throw new ProcessingException("Test exception - thrown");
        }
        m_counter++;
        return key + "." + m_counter;
      }
    });
  }

  protected ICache<Integer, String> createCache(String id, ICacheValueResolver<Integer, String> resolver) {
    @SuppressWarnings("unchecked")
    ICacheBuilder<Integer, String> cacheBuilder = BEANS.get(ICacheBuilder.class);
    ICache<Integer, String> cache = cacheBuilder
        .withCacheId(id)
        .withValueResolver(resolver)
        .withThreadSafe(false)
        .withReplaceIfExists(true)
        .build();
    cache.invalidate(new AllCacheEntryFilter<>(), true);
    return cache;
  }

  @Test
  public void testCacheBasic() {
    ICache<Integer, String> cache = createCache("BasicCacheTestCacheId_testCacheBasic");

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
    cache.invalidate(new KeyCacheEntryFilter<>(CollectionUtility.arrayList(5, 8)), true);
    assertEquals("5.6", cache.get(5));
    assertEquals("8.7", cache.get(8));

    assertEquals(5, cache.getUnmodifiableMap().size());

    // test invalidate all
    cache.invalidate(new AllCacheEntryFilter<>(), true);
    assertEquals(0, cache.getUnmodifiableMap().size());
  }

  @Test
  public void testCacheNullValues() {
    ICache<Integer, String> cache = createCache("BasicCacheTestCacheId_testCacheNullValues");

    assertEquals("2.1", cache.get(2));
    assertNull(cache.get(null));

    // unresolvable keys
    assertNull(cache.get(13));

    assertEquals(1, cache.getUnmodifiableMap().size());

    Map<Integer, String> resultMap = cache.getAll(CollectionUtility.arrayList(3, null));
    assertEquals(1, resultMap.size());
    assertEquals("3.2", resultMap.get(3));

    assertEquals(2, cache.getUnmodifiableMap().size());

    cache.invalidate(new KeyCacheEntryFilter<>(CollectionUtility.arrayList(2, null)), true);
    assertEquals(1, cache.getUnmodifiableMap().size());
    assertEquals("3.2", resultMap.get(3));

    Map<Integer, String> emptyResultMap = cache.getAll(null);
    assertEquals(0, emptyResultMap.size());
    cache.invalidate(null, true);
  }

  @Test(expected = ProcessingException.class)
  public void testCacheExceptionDuringCreation() {
    ICache<Integer, String> cache = createCache("BasicCacheTestCacheId_testCacheExceptionDuringCreation");
    cache.get(1337);
  }

}
