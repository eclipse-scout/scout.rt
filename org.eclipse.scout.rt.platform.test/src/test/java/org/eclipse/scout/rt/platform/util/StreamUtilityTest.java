/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;

public class StreamUtilityTest {

  @Test
  public void testToReverseListEmptyStream() {
    // empty
    LinkedList<Object> list = Stream.empty().collect(StreamUtility.toReverseList());
    assertNotNull(list);
    assertEquals(LinkedList.class, list.getClass());
    assertEquals(Collections.emptyList(), list);
  }

  @Test
  public void testToReverseListSingleElementStream() {
    LinkedList<String> list = Stream.of("a").collect(StreamUtility.toReverseList());
    assertNotNull(list);
    assertEquals(Collections.singletonList("a"), list);
  }

  @Test
  public void testToReverseListMultipleElementsStream() {
    LinkedList<String> list = Stream.of("a", "b", "c").collect(StreamUtility.toReverseList());
    assertNotNull(list);
    assertEquals(Arrays.asList("c", "b", "a"), list);
  }

  @Test
  public void testToReverseListSequentialStream() {
    doTestToReverseListIntStream(false);
  }

  @Test
  public void testToReverseListParallelStream() {
    doTestToReverseListIntStream(true);
  }

  protected void doTestToReverseListIntStream(boolean parallel) {
    final int upperLimit = 10_000;
    try (IntStream stream = IntStream.range(0, upperLimit)) {
      if (parallel) {
        stream.parallel();
      }
      int expected = upperLimit - 1;
      for (Integer actual : stream.mapToObj(Integer::valueOf).collect(StreamUtility.toReverseList())) {
        assertEquals(expected, actual.intValue());
        expected--;
      }
      assertEquals(-1, expected);
    }
  }

  protected static boolean failPredicate() {
    fail("unexpected check of predicate");
    return false; // actually value is never returned
  }

  protected static <T> T failUnaryOperator(T t) {
    fail("unexpected invocation of unaryOperator");
    return t; // actually value is never returned
  }

  @Test
  public void testToMap() {
    String[] items = new String[]{"one", "two", "three"};
    Map<String, String> map = Stream.of(items).collect(StreamUtility.toMap(t -> t, t -> t));
    assertTrue(map instanceof HashMap);
    assertEquals("one", map.get("one"));
    assertEquals("two", map.get("two"));
    assertEquals("three", map.get("three"));
  }

  @Test
  public void testToMapWithNullKey() {
    String[] items = new String[]{null, "one", "two", "three"};
    Map<String, String> map = Stream.of(items).collect(StreamUtility.toMap(t -> t, t -> ObjectUtility.nvl(t, "null")));
    assertTrue(map instanceof HashMap);
    assertEquals("null", map.get(null));
    assertEquals("one", map.get("one"));
    assertEquals("two", map.get("two"));
    assertEquals("three", map.get("three"));
  }

  @Test
  public void testToMapWithNullValue() {
    String[] items = new String[]{null, "one", "two", "three"};
    Map<String, String> map = Stream.of(items).collect(StreamUtility.toMap(t -> ObjectUtility.nvl(t, "null"), t -> t));
    assertTrue(map instanceof HashMap);
    assertNull(map.get("null"));
    assertEquals("one", map.get("one"));
    assertEquals("two", map.get("two"));
    assertEquals("three", map.get("three"));
  }

  @Test(expected = IllegalStateException.class)
  public void testToMapWithDuplicatedKey() {
    String[] items = new String[]{"one", "one", "three"};
    Stream.of(items).collect(StreamUtility.toMap(t -> t, t -> t, StreamUtility.throwingMerger()));
  }

  @Test
  public void testToMapWithDuplicatedKeyReplacingValue() {
    Pair<String, String>[] items = new Pair[]{ImmutablePair.of("key1", "valueA"), ImmutablePair.of("key2", "valueB"), ImmutablePair.of("key1", "valueC")};
    Map<String, String> result = Stream.of(items).collect(StreamUtility.toMap(Pair::getLeft, Pair::getRight, StreamUtility.replacingMerger()));
    assertEquals(2, result.size());
    assertEquals("valueC", result.get("key1"));
    assertEquals("valueB", result.get("key2"));
  }

  @Test
  public void testToMapWithDuplicatedKeyDefaultRemappingFunction() {
    Pair<String, String>[] items = new Pair[]{ImmutablePair.of("key1", "valueA"), ImmutablePair.of("key2", "valueB"), ImmutablePair.of("key1", "valueC")};
    Map<String, String> result = Stream.of(items).collect(StreamUtility.toMap(Pair::getLeft, Pair::getRight));
    assertEquals(2, result.size());
    assertEquals("valueC", result.get("key1"));
    assertEquals("valueB", result.get("key2"));
  }

  @Test
  public void testToLinkedHashMap() {
    String[] values = new String[]{"one", "two", "three"};
    LinkedHashMap<String, Integer> map = Stream.of(values).collect(StreamUtility.toLinkedHashMap(t -> t, StringUtility::length));
    assertArrayEquals(values, map.keySet().toArray());
  }

  @Test
  public void testToLinkedHashMapDuplicatedKey() {
    Pair<String, String>[] items = new Pair[]{ImmutablePair.of("key1", "valueA"), ImmutablePair.of("key2", "valueB"), ImmutablePair.of("key1", "valueC")};
    Map<String, String> map = Stream.of(items).collect(StreamUtility.toLinkedHashMap(Pair::getLeft, Pair::getRight));
    assertEquals("valueC", map.get("key1"));
    assertEquals("valueB", map.get("key2"));
  }

  @Test
  public void testToMapWithRemapping() {
    String[] items = new String[]{"one", "two", "three", "three"};
    Map<String, String> map = Stream.of(items).collect(StreamUtility.toMap(HashMap::new, t -> t, t -> t, (u, v) -> u + v));
    assertTrue(map instanceof HashMap);
    assertEquals("one", map.get("one"));
    assertEquals("two", map.get("two"));
    assertEquals("threethree", map.get("three"));
  }

  @Test
  public void testToMapWithRemappingAndNullValue() {
    String[][] items = new String[][]{{"key1", "one"}, {"key2", "two"}, {"key3", null}, {"key3", "three"}, {"key4", "four-A"}, {"key4", "four-B"}, {"key5", "five"}, {"key5", null}};
    Map<String, String> map = Stream.of(items).collect(StreamUtility.toMap(HashMap::new, t -> t[0], t -> t[1], (u, v) -> u + v));
    assertTrue(map instanceof HashMap);
    assertEquals("one", map.get("key1"));
    assertEquals("two", map.get("key2"));
    assertEquals("nullthree", map.get("key3"));
    assertEquals("four-Afour-B", map.get("key4"));
    assertEquals("fivenull", map.get("key5"));
  }

  @Test
  public void testToMapParallelStream() {
    String[] values = new String[]{"one", "two", "three"};
    Map<String, Integer> map = Stream.of(values).parallel().collect(StreamUtility.toMap(t -> t, StringUtility::length));
    assertEquals(Integer.valueOf(3), map.get("one"));
    assertEquals(Integer.valueOf(3), map.get("two"));
    assertEquals(Integer.valueOf(5), map.get("three"));
  }

  @Test(expected = IllegalStateException.class)
  public void testThrowingMerger() {
    // Test list: Two items have the key "a"
    List<ImmutablePair<String, String>> items = Arrays.asList(
        new ImmutablePair<>("a", "1"),
        new ImmutablePair<>("b", "2"),
        new ImmutablePair<>("a", "3"));
    items.stream().collect(Collectors.toMap(
        i -> i.getLeft(),
        Function.identity(),
        StreamUtility.throwingMerger()));
  }

  @Test
  public void testIgnoringMerger() {
    // Test list: Two items have the key "a"
    List<ImmutablePair<String, String>> items = Arrays.asList(
        new ImmutablePair<>("a", "1"),
        new ImmutablePair<>("b", "2"),
        new ImmutablePair<>("a", "3"));
    Map<Object, ImmutablePair<String, String>> map = items.stream().collect(Collectors.toMap(
        i -> i.getLeft(),
        Function.identity(),
        StreamUtility.ignoringMerger()));
    assertEquals(2, map.size());
    assertEquals("1", map.get("a").getRight()); // <--
    assertEquals("2", map.get("b").getRight());
  }

  @Test
  public void testReplacingMerger() {
    // Test list: Two items have the key "a"
    List<ImmutablePair<String, String>> items = Arrays.asList(
        new ImmutablePair<>("a", "1"),
        new ImmutablePair<>("b", "2"),
        new ImmutablePair<>("a", "3"));
    Map<Object, Pair<String, String>> map = items.stream().collect(Collectors.toMap(
        i -> i.getLeft(),
        Function.identity(),
        StreamUtility.replacingMerger()));
    assertEquals(2, map.size());
    assertEquals("3", map.get("a").getRight()); // <--
    assertEquals("2", map.get("b").getRight());
  }

  @Test
  public void testConcat() {
    assertEquals(0, StreamUtility.concat().count());
    assertEquals(0, StreamUtility.concat((Stream<?>[]) null).count());
    assertEquals(0, StreamUtility.concat((Stream<?>) null).count());
    assertEquals(0, StreamUtility.concat(null, null).count());

    assertEquals(List.of(10, 20, 30, 12, 11, 10), StreamUtility.concat(IntStream.of(10, 20, 30).boxed(), IntStream.of(12, 11, 10).boxed()).collect(Collectors.toList()));
    assertEquals(List.of(10, 20, 33, 44, 5, 6), StreamUtility.concat(IntStream.of(10, 20).boxed(), IntStream.of(33, 44).boxed(), IntStream.of(5, 6).boxed()).collect(Collectors.toList()));
    assertEquals(List.of(10, 20, 5, 6), StreamUtility.concat(IntStream.of(10, 20).boxed(), Stream.empty(), null, IntStream.of(5, 6).boxed()).collect(Collectors.toList()));
  }
}
