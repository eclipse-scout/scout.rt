/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.reflect;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Test;

public class DefaultValueMapTest {

  @Test
  public void testIsEmptyAndSize() {
    DefaultValueMap<String, Integer> mapWithA = new DefaultValueMap<>(Map.of("A", 1));
    assertEquals(1, mapWithA.size());
    assertFalse(mapWithA.isEmpty());

    mapWithA.put("B", 2);
    assertEquals(2, mapWithA.size());
    assertFalse(mapWithA.isEmpty());

    mapWithA.put("C", 3);
    assertEquals(3, mapWithA.size());
    assertFalse(mapWithA.isEmpty());

    assertNotNull(mapWithA.put("A", 4));
    assertEquals(3, mapWithA.size());
    assertFalse(mapWithA.isEmpty());

    mapWithA.remove("B");
    assertEquals(2, mapWithA.size());
    assertFalse(mapWithA.isEmpty());

    mapWithA.remove("A");
    assertEquals(1, mapWithA.size());
    assertFalse(mapWithA.isEmpty());

    mapWithA.remove("C");
    assertEquals(0, mapWithA.size());
    assertTrue(mapWithA.isEmpty());

    mapWithA = new DefaultValueMap<>(Map.of("A", 1));
    mapWithA.remove("A");
    assertEquals(0, mapWithA.size());
    assertTrue(mapWithA.isEmpty());

    DefaultValueMap<String, Integer> emptyMap = new DefaultValueMap<>(Collections.emptyMap());
    assertEquals(0, emptyMap.size());
    assertTrue(emptyMap.isEmpty());

    emptyMap.put("A", 1);
    assertEquals(1, emptyMap.size());
    assertFalse(emptyMap.isEmpty());
  }

  @Test
  public void testKeySet() {
    DefaultValueMap<String, Integer> mapWithA = new DefaultValueMap<>(Map.of("A", 1));
    assertEquals(Set.of("A"), mapWithA.keySet());

    mapWithA.put("B", 2);
    assertEquals(Set.of("A", "B"), mapWithA.keySet());

    mapWithA.put("C", 3);
    assertEquals(Set.of("A", "B", "C"), mapWithA.keySet());

    assertNotNull(mapWithA.put("A", 4));
    assertEquals(Set.of("A", "B", "C"), mapWithA.keySet());

    mapWithA.remove("B");
    assertEquals(Set.of("A", "C"), mapWithA.keySet());

    mapWithA.remove("A");
    assertEquals(Set.of("C"), mapWithA.keySet());

    mapWithA.remove("C");
    assertEquals(Set.of(), mapWithA.keySet());

    mapWithA = new DefaultValueMap<>(Map.of("A", 1));
    mapWithA.remove("A");
    assertEquals(Set.of(), mapWithA.keySet());

    DefaultValueMap<String, Integer> emptyMap = new DefaultValueMap<>(Collections.emptyMap());
    assertEquals(Set.of(), emptyMap.keySet());

    emptyMap.put("A", 1);
    assertEquals(Set.of("A"), emptyMap.keySet());
  }

  @Test
  public void testValues() {
    DefaultValueMap<String, Integer> mapWithA = new DefaultValueMap<>(Map.of("A", 1));
    assertEquals(Set.of(1), new HashSet<>(mapWithA.values()));

    mapWithA.put("B", 2);
    assertEquals(Set.of(1, 2), new HashSet<>(mapWithA.values()));

    mapWithA.put("C", 3);
    assertEquals(Set.of(1, 2, 3), new HashSet<>(mapWithA.values()));

    assertNotNull(mapWithA.put("A", 4));
    assertEquals(Set.of(2, 3, 4), new HashSet<>(mapWithA.values()));

    mapWithA.remove("B");
    assertEquals(Set.of(3, 4), new HashSet<>(mapWithA.values()));

    mapWithA.remove("A");
    assertEquals(Set.of(3), new HashSet<>(mapWithA.values()));

    mapWithA.remove("C");
    assertEquals(Set.of(), new HashSet<>(mapWithA.values()));

    mapWithA = new DefaultValueMap<>(Map.of("A", 1));
    mapWithA.remove("A");
    assertEquals(Set.of(), new HashSet<>(mapWithA.values()));

    DefaultValueMap<String, Integer> emptyMap = new DefaultValueMap<>(Collections.emptyMap());
    assertEquals(Set.of(), new HashSet<>(emptyMap.values()));

    emptyMap.put("A", 1);
    assertEquals(Set.of(1), new HashSet<>(emptyMap.values()));
  }

  @Test
  public void testRemoveByKeySet() {
    DefaultValueMap<String, Integer> mapWithA = new DefaultValueMap<>(Map.of("A", 1));
    mapWithA.put("B", 2);
    mapWithA.put("C", 3);

    //noinspection RedundantCollectionOperation
    mapWithA.keySet().remove("A");
    assertEquals(Set.of("B", "C"), mapWithA.keySet());

    //noinspection RedundantCollectionOperation
    mapWithA.keySet().remove("C");
    assertEquals(Set.of("B"), mapWithA.keySet());

    mapWithA.put("A", 4);
    assertEquals(Set.of("A", "B"), mapWithA.keySet());

    //noinspection RedundantCollectionOperation
    mapWithA.keySet().remove("A");
    assertEquals(Set.of("B"), mapWithA.keySet());
  }

  @Test
  public void testGetAndPut() {
    DefaultValueMap<String, Integer> mapWithA = new DefaultValueMap<>(Map.of("A", 1));
    assertEquals(1, (int) mapWithA.get("A"));
    assertNull(mapWithA.get("B"));

    assertNull(mapWithA.put("B", 2));
    assertEquals(2, (int) mapWithA.get("B"));

    assertEquals(1, (int) mapWithA.remove("A"));
    assertNull(mapWithA.get("A"));

    assertNull(mapWithA.put("A", 1));
    assertEquals(1, (int) mapWithA.get("A"));

    assertEquals(1, (int) mapWithA.put("A", 3));
    assertEquals(3, (int) mapWithA.get("A"));

    assertTrue(mapWithA.containsKey("A"));
    assertEquals(3, (int) mapWithA.remove("A"));
    assertFalse(mapWithA.containsKey("A"));
    assertNull(mapWithA.get("A"));
    assertNull(mapWithA.put("A", 4));
    assertEquals(4, (int) mapWithA.get("A"));
  }

  @Test
  public void testPutAll() {
    DefaultValueMap<String, Integer> mapWithA = new DefaultValueMap<>(Map.of("A", 1));
    assertEquals(1, mapWithA.size());

    mapWithA.putAll(Map.of("B", 2, "C", 3));
    assertEquals(3, mapWithA.size());
  }

  @Test
  public void testConstruction() {
    // ctor is implicitly tested by other tests as well, this test is especially intended to test the ctor with startEmpty=true argument
    DefaultValueMap<String, Integer> emptyMap = new DefaultValueMap<>(Map.of("A", 1), true);
    DefaultValueMap<String, Integer> nonEmptyMap = new DefaultValueMap<>(Map.of("A", 1), false);

    assertNotEquals(emptyMap, nonEmptyMap);
    assertEquals(Map.of("A", 1), nonEmptyMap);
    assertEquals(Collections.emptyMap(), emptyMap);

    nonEmptyMap.remove("A");
    assertEquals(emptyMap, nonEmptyMap);

    emptyMap.put("A", 1);
    assertNotEquals(nonEmptyMap, emptyMap);

    emptyMap.put("A", 2);
    emptyMap.put("B", 3);

    assertEquals(Map.of("A", 2, "B", 3), emptyMap);
  }

  @Test
  public void testContainsKey() {
    DefaultValueMap<String, Integer> emptyMap = new DefaultValueMap<>(Map.of("A", 1), true);
    DefaultValueMap<String, Integer> nonEmptyMap = new DefaultValueMap<>(Map.of("A", 1), false);

    assertFalse(emptyMap.containsKey("A"));
    assertTrue(nonEmptyMap.containsKey("A"));

    emptyMap.put("A", 1);
    assertTrue(emptyMap.containsKey("A"));
    assertFalse(emptyMap.containsKey("B"));

    emptyMap.put("B", 2);
    assertTrue(emptyMap.containsKey("A"));
    assertTrue(emptyMap.containsKey("B"));

    emptyMap.remove("A");
    assertFalse(emptyMap.containsKey("A"));
    assertTrue(emptyMap.containsKey("B"));

    emptyMap.put("A", 3);
    assertTrue(emptyMap.containsKey("A"));
    assertTrue(emptyMap.containsKey("B"));
  }

  @Test
  public void testContainsValue() {
    DefaultValueMap<String, Integer> emptyMap = new DefaultValueMap<>(Map.of("A", 1), true);
    DefaultValueMap<String, Integer> nonEmptyMap = new DefaultValueMap<>(Map.of("A", 1), false);

    assertFalse(emptyMap.containsValue(1));
    assertTrue(nonEmptyMap.containsValue(1));

    nonEmptyMap.remove("A");
    assertFalse(nonEmptyMap.containsValue(1));

    emptyMap.put("A", 1);
    assertTrue(emptyMap.containsValue(1));
    assertFalse(emptyMap.containsValue(2));
    assertFalse(emptyMap.containsValue(3));

    emptyMap.put("B", 2);
    assertTrue(emptyMap.containsValue(1));
    assertTrue(emptyMap.containsValue(2));
    assertFalse(emptyMap.containsValue(3));

    emptyMap.remove("A");
    assertFalse(emptyMap.containsValue(1));
    assertTrue(emptyMap.containsValue(2));
    assertFalse(emptyMap.containsValue(3));

    emptyMap.put("A", 3);
    assertFalse(emptyMap.containsValue(1));
    assertTrue(emptyMap.containsValue(2));
    assertTrue(emptyMap.containsValue(3));
  }

  @Test
  public void testEntrySet() {
    DefaultValueMap<String, Integer> emptyMap = new DefaultValueMap<>(Map.of("A", 1), true);
    Set<Entry<String, Integer>> emptyMapEntries = emptyMap.entrySet();
    assertFalse(emptyMapEntries.iterator().hasNext());
    emptyMapEntries.forEach(e -> fail());

    emptyMap.put("A", 1);
    assertEquals(1, emptyMapEntries.size());

    emptyMap.put("B", 2);
    assertEquals(2, emptyMapEntries.size());

    emptyMapEntries.remove(CollectionUtility.firstElement(emptyMapEntries));
    assertEquals(1, emptyMap.size());

    Iterator<Entry<String, Integer>> emptyMapEntriesIterator = emptyMapEntries.iterator();
    assertNotNull(emptyMapEntriesIterator.next());
    emptyMapEntriesIterator.remove();
    assertTrue(emptyMap.isEmpty());

    DefaultValueMap<String, Integer> nonEmptyMap = new DefaultValueMap<>(Map.of("A", 1), false);
    Set<Entry<String, Integer>> nonEmptyMapEntries = nonEmptyMap.entrySet();
    assertTrue(nonEmptyMapEntries.iterator().hasNext());
    nonEmptyMapEntries.forEach(e -> assertEquals("A", e.getKey()));

    nonEmptyMap.put("A", 1);
    assertEquals(1, nonEmptyMapEntries.size());

    nonEmptyMap.put("B", 2);
    assertEquals(2, nonEmptyMapEntries.size());

    nonEmptyMapEntries.remove(CollectionUtility.firstElement(nonEmptyMapEntries));
    assertEquals(1, nonEmptyMap.size());

    Iterator<Entry<String, Integer>> nonEmptyMapEntriesIterator = nonEmptyMapEntries.iterator();
    assertNotNull(nonEmptyMapEntriesIterator.next());
    nonEmptyMapEntriesIterator.remove();
    assertTrue(nonEmptyMap.isEmpty());
  }
}
