/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.shared.cache;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.HashMap;

import org.eclipse.scout.rt.platform.cache.BasicCache;
import org.eclipse.scout.rt.platform.cache.CacheRegistryService;
import org.eclipse.scout.rt.platform.cache.ICache;
import org.eclipse.scout.rt.platform.cache.ICacheRegistryService;
import org.eclipse.scout.rt.platform.cache.ICacheValueResolver;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Test;

/**
 * Test for {@link ICacheRegistryService}
 */
public class CacheRegistryServiceTest {

  @SuppressWarnings("unchecked")
  @Test
  public void testRegistry() {
    CacheRegistryService s = new CacheRegistryService();
    String testKey = "testkey";
    BasicCache<String, String> testCache = new BasicCache<>(testKey, mock(ICacheValueResolver.class), new HashMap<>());
    s.register(testCache);
    assertEquals(testCache, s.get(testKey));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testRegistryIfAbsent() {
    CacheRegistryService s = new CacheRegistryService();
    String testKey = "testkey";
    BasicCache<String, String> testCache = new BasicCache<>(testKey, mock(ICacheValueResolver.class), new HashMap<>());
    s.registerIfAbsent(testCache);
    assertEquals(testCache, s.get(testKey));
  }

  @Test(expected = AssertionException.class)
  public void testNotRegisteredCache() {
    CacheRegistryService s = new CacheRegistryService();
    assertNull(s.get("unknown"));
  }

  @Test
  public void testNotRegisteredOptCache() {
    CacheRegistryService s = new CacheRegistryService();
    assertNull(s.opt("unknown"));
  }

  @Test(expected = AssertionException.class)
  public void testDuplicateCreate() {
    CacheRegistryService s = new CacheRegistryService();
    String cacheId = "testcacheid";
    BasicCache<String, String> cache1 = new BasicCache<>(cacheId, key -> "Valuf of " + key, new HashMap<>());
    s.register(cache1);
    BasicCache<String, String> cache2 = new BasicCache<>(cacheId, key -> "Valuf of " + key, new HashMap<>());
    s.register(cache2);
  }

  @Test
  public void testDuplicateCreateIfAbsent() {
    CacheRegistryService s = new CacheRegistryService();
    String cacheId = "testcacheid";
    ICache<String, String> cache1 = new BasicCache<>(cacheId, key -> "Valuf of " + key, new HashMap<>());
    s.registerIfAbsent(cache1);
    ICache<String, String> cache2 = new BasicCache<>(cacheId, key -> "Valuf of " + key, new HashMap<>());
    ICache<String, String> cache = s.registerIfAbsent(cache2);
    assertSame(cache1, cache);
  }
}
