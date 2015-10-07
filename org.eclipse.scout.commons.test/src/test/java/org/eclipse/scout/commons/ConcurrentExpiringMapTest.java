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
package org.eclipse.scout.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.ConcurrentExpiringMap.ExpiringElement;
import org.eclipse.scout.commons.holders.IntegerHolder;
import org.junit.Test;

/**
 * @since 5.2
 */
public class ConcurrentExpiringMapTest {
  private static final long TIME_TO_LIVE_MILLISECONDS = 40;

  private ConcurrentExpiringMap<Integer, String> createMap(boolean letExpire) {
    ConcurrentExpiringMap<Integer, String> map = new ConcurrentExpiringMap<>(TIME_TO_LIVE_MILLISECONDS, TimeUnit.MILLISECONDS);
    map.put(1, "1");
    if (letExpire) {
      expire();
    }
    return map;
  }

  private ConcurrentExpiringMap<Integer, String> createMapTouchOnGet() {
    ConcurrentExpiringMap<Integer, String> map = new ConcurrentExpiringMap<>(new ConcurrentHashMap<Integer, ExpiringElement<String>>(), TIME_TO_LIVE_MILLISECONDS, true, false, 0, 0);
    map.put(1, "1");
    return map;
  }

  private ConcurrentExpiringMap<Integer, String> createMapTouchOnIterate() {
    ConcurrentExpiringMap<Integer, String> map = new ConcurrentExpiringMap<>(new ConcurrentHashMap<Integer, ExpiringElement<String>>(), TIME_TO_LIVE_MILLISECONDS, true, true, 0, 0);
    map.put(1, "1");
    return map;
  }

  private void expire() {
    sleepUninterruptibly(TIME_TO_LIVE_MILLISECONDS + 5);
  }

  private void expireHalf() {
    sleepUninterruptibly(TIME_TO_LIVE_MILLISECONDS / 2 + 5);
  }

  /**
   * In case {@link Thread#sleep(long)} is interrupted, the interrupt exception is ignored and sleep is continued.
   * <p>
   * This method is required, as when executing tests (in eclipse) {@link Thread#sleep(long)} does not sleep long
   * enough. Either because of interruption or any other scheduling reason. This method guarantees the time.
   *
   * @param millis
   *          the length of time to sleep in milliseconds
   */
  private void sleepUninterruptibly(long millis) {
    long currentMillis = System.currentTimeMillis();
    long targetMillis = currentMillis + millis;
    boolean interrupted = false;
    while (currentMillis < targetMillis) {
      try {
        Thread.sleep(targetMillis - currentMillis);
      }
      catch (InterruptedException e) {
        interrupted = true;
      }
      currentMillis = System.currentTimeMillis();
    }
    if (interrupted) {
      // at least reset interrupted status on thread
      Thread.currentThread().interrupt();
    }
  }

  @Test
  public void testGetExpires() {
    assertNull(createMap(true).get(1));
  }

  @Test
  public void testGetNotExpires() {
    assertEquals("1", createMap(false).get(1));
  }

  @Test
  public void testContainsKeyExpires() {
    assertFalse(createMap(true).containsKey(1));
  }

  @Test
  public void testContainsKeyNotExpires() {
    assertTrue(createMap(false).containsKey(1));
  }

  @Test
  public void testContainsValueExpires() {
    assertFalse(createMap(true).containsValue("1"));
  }

  @Test
  public void testContainsValueNotExpires() {
    assertTrue(createMap(false).containsValue("1"));
  }

  @Test
  public void testIsEmptyExpires() {
    assertTrue(createMap(true).isEmpty());
  }

  @Test
  public void testIsEmptyNotExpires() {
    assertFalse(createMap(false).isEmpty());
  }

  @Test
  public void testIterateEntriesExpires() {
    assertFalse(createMap(true).entrySet().iterator().hasNext());
  }

  @Test
  public void testIterateEntriesNotExpires() {
    assertEquals("1", createMap(false).entrySet().iterator().next().getValue());
  }

  @Test
  public void testSizeNeverExpires() {
    assertEquals(1, createMap(false).size());
    // we allow explicitly this inconsistency
    assertEquals(1, createMap(true).size());
  }

  @Test
  public void testRemoveExpires() {
    ConcurrentExpiringMap<Integer, String> map = createMap(true);
    assertNull(map.remove(1));
    assertFalse(map.containsKey(1));
  }

  @Test
  public void testRemoveNotExpires() {
    ConcurrentExpiringMap<Integer, String> map = createMap(false);
    assertEquals("1", map.remove(1));
    assertFalse(map.containsKey(1));
  }

  @Test
  public void testBulk() {
    Map<Integer, String> otherMap = new HashMap<Integer, String>();
    otherMap.put(2, "2");
    otherMap.put(3, "3");
    ConcurrentExpiringMap<Integer, String> map = createMap(false);
    map.putAll(otherMap);
    assertEquals(3, map.size());

    map.clear();
    assertEquals(0, map.size());
    assertFalse(map.entrySet().iterator().hasNext());
  }

  @Test
  public void testPutIfAbsent() {
    ConcurrentExpiringMap<Integer, String> map = createMap(false);
    assertEquals("1", map.putIfAbsent(1, "1.1"));
    assertEquals("1", map.get(1));
    assertNull(map.putIfAbsent(2, "2"));
    assertEquals("2", map.get(2));
  }

  @Test
  public void testRemoveKeyValue() {
    ConcurrentExpiringMap<Integer, String> map = createMap(false);
    assertFalse(map.remove(2, "1"));
    assertEquals(1, map.size());
    assertFalse(map.remove(1, "2"));
    assertEquals(1, map.size());
    assertTrue(map.remove(1, "1"));
    assertEquals(0, map.size());
  }

  @Test
  public void testReplaceOldNewValue() {
    ConcurrentExpiringMap<Integer, String> map = createMap(false);

    assertFalse(map.replace(2, "1", "2"));
    assertEquals(1, map.size());
    assertEquals("1", map.get(1));

    assertFalse(map.replace(1, "2", "2"));
    assertEquals(1, map.size());
    assertEquals("1", map.get(1));

    assertTrue(map.replace(1, "1", "2"));
    assertEquals(1, map.size());
    assertEquals("2", map.get(1));
  }

  @Test
  public void testReplace() {
    ConcurrentExpiringMap<Integer, String> map = createMap(false);

    assertNull(map.replace(2, "1"));
    assertEquals(1, map.size());
    assertEquals("1", map.get(1));

    assertEquals(map.replace(1, "2"), "1");
    assertEquals(1, map.size());
    assertEquals("2", map.get(1));
  }

  @Test
  public void testTouchOnGetContainsKeyExpires() {
    ConcurrentExpiringMap<Integer, String> map = createMapTouchOnGet();
    expireHalf();
    assertTrue(map.containsKey(1));
    expireHalf();
    assertFalse(map.containsKey(1));
  }

  @Test
  public void testTouchOnGetContainsValueExpires() {
    ConcurrentExpiringMap<Integer, String> map = createMapTouchOnGet();
    expireHalf();
    assertTrue(map.containsValue("1"));
    expireHalf();
    assertFalse(map.containsValue("1"));
  }

  @Test
  public void testTouchOnGetIterateEntriesExpires() {
    ConcurrentExpiringMap<Integer, String> map = createMapTouchOnGet();
    expireHalf();
    assertTrue(map.entrySet().iterator().hasNext());
    expireHalf();
    assertFalse(map.entrySet().iterator().hasNext());
  }

  @Test
  public void testTouchOnGetGetNotExpires() {
    ConcurrentExpiringMap<Integer, String> map = createMapTouchOnGet();
    expireHalf();
    assertEquals("1", map.get(1));
    expireHalf();
    assertEquals("1", map.get(1));
  }

  @Test
  public void testTouchOnIterateIterateEntriesNotExpires() {
    ConcurrentExpiringMap<Integer, String> map = createMapTouchOnIterate();
    expireHalf();
    assertTrue(map.entrySet().iterator().hasNext());
    expireHalf();
    assertTrue(map.entrySet().iterator().hasNext());
  }

  @Test
  public void testBoundedSize() {
    int targetSize = 10;
    final IntegerHolder countEvicted = new IntegerHolder(0);

    ConcurrentExpiringMap<Integer, String> map = new ConcurrentExpiringMap<Integer, String>(0, TimeUnit.MILLISECONDS, targetSize) {

      @Override
      protected void execEntryEvicted(Integer key, String value) {
        Integer currentCount = countEvicted.getValue();
        countEvicted.setValue(currentCount + 1);
        // assert LRU behavior
        assertEquals(currentCount, key);
      }
    };
    for (int i = 0; i < targetSize; i++) {
      map.put(i, String.valueOf(i));
    }
    assertEquals(targetSize, map.size());

    // access elements to assert LRU behavior
    sleepUninterruptibly(5);
    for (int i = 0; i < targetSize; i++) {
      sleepUninterruptibly(5);
      assertEquals(String.valueOf(i), map.get(i));
    }
    sleepUninterruptibly(5);

    // overload
    for (int i = targetSize; i < targetSize * 2; i++) {
      map.put(i, String.valueOf(i));
    }
    assertEquals(targetSize * 2 - countEvicted.getValue(), map.size());
  }
}
