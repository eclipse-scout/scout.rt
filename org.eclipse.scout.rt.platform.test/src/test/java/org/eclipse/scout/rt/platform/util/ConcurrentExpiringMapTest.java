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
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.holders.IntegerHolder;
import org.eclipse.scout.rt.platform.util.collection.ConcurrentExpiringMap;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link ConcurrentExpiringMap}
 *
 * @since 5.2
 */
public class ConcurrentExpiringMapTest {
  private static final long TIME_TO_LIVE_MILLISECONDS = 40;

  private TestConcurrentExpiringMap createMap(boolean expired) {
    if (expired) {
      return createMapExpired();
    }
    else {
      return createMapNotExpired();
    }
  }

  @Test
  public void testGetExpires() {
    assertNull(createMap(true).get(1));
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
  public void testGetNotExpires() {
    assertEquals("1", createMap(false).get(1));
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

  private TestConcurrentExpiringMap createMapExpired() {
    long currentTime = System.currentTimeMillis();
    TestConcurrentExpiringMap map = createTestMap();
    map.setTimestamp(currentTime - TIME_TO_LIVE_MILLISECONDS - 1);
    return map;
  }

  private TestConcurrentExpiringMap createMapNotExpired() {
    TestConcurrentExpiringMap map = createTestMap();
    long currentTime = System.currentTimeMillis();
    map.setTimestamp(currentTime);
    return map;
  }

  private TestConcurrentExpiringMap createTestMap() {
    TestConcurrentExpiringMap map = new TestConcurrentExpiringMap(TIME_TO_LIVE_MILLISECONDS, TimeUnit.MILLISECONDS);
    map.put(1, "1");
    return map;
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
    TestConcurrentExpiringMap map = createMap(false);
    map.setTimestamp(System.currentTimeMillis() + 100);

    assertNull(map.replace(2, "1"));

    assertEquals(1, map.size());
    assertEquals("1", map.get(1));

    assertEquals(map.replace(1, "2"), "1");
    assertEquals(1, map.size());
    assertEquals("2", map.get(1));
  }

  @Test
  public void testBoundedSize() {
    int targetSize = 10;
    final IntegerHolder countEvicted = new IntegerHolder(0);
    long now = System.currentTimeMillis();
    int overflowSize = targetSize * 3 / 2;

    TestConcurrentExpiringMap map = new TestConcurrentExpiringMap(0, TimeUnit.MILLISECONDS, targetSize) {

      @Override
      protected void execEntryEvicted(Integer key, String value) {
        Integer currentCount = countEvicted.getValue();
        countEvicted.setValue(currentCount + 1);
      }
    };
    map.setTimestamp(now - 1);

    //put in some expired values
    for (int i = 0; i < overflowSize - 1; i++) {
      map.put(i, String.valueOf(i));
      assertEquals(Integer.valueOf(0), countEvicted.getValue());
    }
    assertEquals(overflowSize - 1, map.size());

    // access elements to assert LRU behavior
    for (int i = 0; i < overflowSize - 1; i++) {
      assertEquals(String.valueOf(i), map.get(i));
    }
    assertEquals(Integer.valueOf(0), countEvicted.getValue());

    // overload
    for (int i = overflowSize; i < (overflowSize - 1) * 2; i++) {
      map.put(i, String.valueOf(i));
    }
    assertEquals(Integer.valueOf(overflowSize), countEvicted.getValue());
  }

  /**
   * Map with mocked timestamp
   */
  class TestConcurrentExpiringMap extends ConcurrentExpiringMap<Integer, String> {
    private List<ExpiringElement<String>> m_elements = new ArrayList<>();
    private Long m_timestamp = System.currentTimeMillis();

    public TestConcurrentExpiringMap(long timeToLiveDuration, TimeUnit timeToLiveUnit) {
      super(timeToLiveDuration, timeToLiveUnit);
    }

    public TestConcurrentExpiringMap(long timeToLiveDuration, TimeUnit timeToLiveUnit, int targetSize) {
      super(timeToLiveDuration, timeToLiveUnit, targetSize);
    }

    /**
     * Set timestamp for all elements
     */
    public void setTimestamp(long timestamp) {
      m_timestamp = timestamp;
      for (ExpiringElement<String> e : m_elements) {
        Mockito.when(e.getTimestamp()).thenReturn(timestamp);
      }
    }

    /**
     * Create a mock element with the given timestamp and value See {@link #setTimestamp(long)}.
     */
    @Override
    protected ExpiringElement<String> createElement(String value) {
      @SuppressWarnings("unchecked")
      ExpiringElement<String> element = Mockito.mock(ExpiringElement.class);
      Mockito.when(element.getValue()).thenReturn(value);
      Mockito.when(element.getTimestamp()).thenReturn(m_timestamp);
      m_elements.add(element);
      return element;
    }

  }

}
