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
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class DefaultValueMapTest {

  @Test
  public void testIsEmptyAndSize() {
    DefaultValueMap mapWithA = new DefaultValueMap(Map.of("A", 1));
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

    mapWithA = new DefaultValueMap(Map.of("A", 1));
    mapWithA.remove("A");
    assertEquals(0, mapWithA.size());
    assertTrue(mapWithA.isEmpty());

    DefaultValueMap emptyMap = new DefaultValueMap(Collections.emptyMap());
    assertEquals(0, emptyMap.size());
    assertTrue(emptyMap.isEmpty());

    emptyMap.put("A", 1);
    assertEquals(1, emptyMap.size());
    assertFalse(emptyMap.isEmpty());
  }

  @Test
  public void testKeySet() {
    DefaultValueMap mapWithA = new DefaultValueMap(Map.of("A", 1));
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

    mapWithA = new DefaultValueMap(Map.of("A", 1));
    mapWithA.remove("A");
    assertEquals(Set.of(), mapWithA.keySet());

    DefaultValueMap emptyMap = new DefaultValueMap(Collections.emptyMap());
    assertEquals(Set.of(), emptyMap.keySet());

    emptyMap.put("A", 1);
    assertEquals(Set.of("A"), emptyMap.keySet());
  }

  @Test
  public void testValues() {
    DefaultValueMap mapWithA = new DefaultValueMap(Map.of("A", 1));
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

    mapWithA = new DefaultValueMap(Map.of("A", 1));
    mapWithA.remove("A");
    assertEquals(Set.of(), new HashSet<>(mapWithA.values()));

    DefaultValueMap emptyMap = new DefaultValueMap(Collections.emptyMap());
    assertEquals(Set.of(), new HashSet<>(emptyMap.values()));

    emptyMap.put("A", 1);
    assertEquals(Set.of(1), new HashSet<>(emptyMap.values()));
  }

  @Test
  public void testRemoveByKeySet() {
    DefaultValueMap mapWithA = new DefaultValueMap(Map.of("A", 1));
    mapWithA.put("B", 2);
    mapWithA.put("C", 3);

    mapWithA.keySet().remove("A");
    assertEquals(Set.of("B", "C"), mapWithA.keySet());

    mapWithA.keySet().remove("C");
    assertEquals(Set.of("B"), mapWithA.keySet());

    mapWithA.put("A", 4);
    assertEquals(Set.of("A", "B"), mapWithA.keySet());

    mapWithA.keySet().remove("A");
    assertEquals(Set.of("B"), mapWithA.keySet());
  }

  @Test
  public void testGetAndPut() {
    DefaultValueMap mapWithA = new DefaultValueMap(Map.of("A", 1));
    assertEquals(1, mapWithA.get("A"));
    assertNull(mapWithA.get("B"));

    mapWithA.put("B", 2);
    assertEquals(2, mapWithA.get("B"));

    mapWithA.remove("A");
    assertNull(mapWithA.get("A"));

    mapWithA.put("A", 1);
    assertEquals(1, mapWithA.get("A"));

    mapWithA.put("A", 3);
    assertEquals(3, mapWithA.get("A"));
  }

  @Test
  public void testPutAll() {
    DefaultValueMap mapWithA = new DefaultValueMap(Map.of("A", 1));
    assertEquals(1, mapWithA.size());

    mapWithA.putAll(Map.of("B", 2, "C", 3));
    assertEquals(3, mapWithA.size());
  }
}
