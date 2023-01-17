/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.cache;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.cache.AllCacheEntryFilter;
import org.eclipse.scout.rt.platform.cache.ICacheEntryFilter;
import org.eclipse.scout.rt.platform.cache.InvalidateCacheNotification;
import org.eclipse.scout.rt.platform.cache.KeyCacheEntryFilter;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.services.common.code.AbstractCodeType;
import org.junit.Test;

/**
 * Tests the coalesce functionality of {@link InvalidateCacheNotificationCoalescer}
 *
 * @since 5.2
 */
public class InvalidateCacheNotificationCoalescerTest {

  private final static String CACHE_ID_1 = "CacheTestId1";
  private final static String CACHE_ID_2 = "CacheTestId2";
  private final static String CACHE_ID_3 = "CacheTestId3";

  @Test
  public void testCoalesceEmptySet() {
    InvalidateCacheNotificationCoalescer coalescer = new InvalidateCacheNotificationCoalescer();
    List<InvalidateCacheNotification> res = coalescer.coalesce(new ArrayList<>());
    assertTrue(res.isEmpty());
  }

  @Test
  public void testCoalesceNotificationsSet() {
    InvalidateCacheNotificationCoalescer coalescer = new InvalidateCacheNotificationCoalescer();
    ICacheEntryFilter<Object, Object> filter1 = new KeyCacheEntryFilter<>(CollectionUtility.<Object> arrayList(CodeType1.class));
    ICacheEntryFilter<Object, Object> filter2 = new KeyCacheEntryFilter<>(CollectionUtility.<Object> arrayList(CodeType2.class));
    List<InvalidateCacheNotification> testList = CollectionUtility.arrayList(
        new InvalidateCacheNotification(CACHE_ID_1, filter1),
        new InvalidateCacheNotification(CACHE_ID_1, filter2),
        new InvalidateCacheNotification(CACHE_ID_2, filter2),
        new InvalidateCacheNotification(CACHE_ID_3, new AllCacheEntryFilter<>()),
        new InvalidateCacheNotification(CACHE_ID_3, filter2));
    List<InvalidateCacheNotification> res = coalescer.coalesce(testList);
    assertEquals(3, res.size());
    for (InvalidateCacheNotification notification : res) {
      if (CACHE_ID_1.equals(notification.getCacheId())) {
        Set<?> keys = ((KeyCacheEntryFilter<?, ?>) notification.getFilter()).getKeys();
        assertEquals(2, keys.size());
        assertTrue(keys.contains(CodeType1.class));
        assertTrue(keys.contains(CodeType2.class));
      }
      else if (CACHE_ID_2.equals(notification.getCacheId())) {
        Set<?> keys = ((KeyCacheEntryFilter<?, ?>) notification.getFilter()).getKeys();
        assertEquals(1, keys.size());
        assertTrue(keys.contains(CodeType2.class));
      }
      else if (CACHE_ID_3.equals(notification.getCacheId())) {
        assertTrue(notification.getFilter() instanceof AllCacheEntryFilter);
      }
      else {
        fail("invalid cacheId" + notification.getCacheId());
      }
    }
  }

  class CodeType1 extends AbstractCodeType<Long, Long> {
    private static final long serialVersionUID = 1L;

    @Override
    public Long getId() {
      return 0L;
    }
  }

  class CodeType2 extends AbstractCodeType<Long, Long> {
    private static final long serialVersionUID = 1L;

    @Override
    public Long getId() {
      return 0L;
    }
  }
}
