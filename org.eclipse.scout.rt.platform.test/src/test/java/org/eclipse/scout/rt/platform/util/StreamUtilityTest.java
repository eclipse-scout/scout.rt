/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Test;

public class StreamUtilityTest {

  @Test
  public void testIterateNullParameters() {
    // null initialElement is ok
    assertEquals(
        Arrays.asList(null, 0, 1, 2, 3, 4),
        StreamUtility.<Integer> iterate(null, i -> i == null || i < 5, i -> i == null ? 0 : i + 1).collect(Collectors.toList()));

    try {
      StreamUtility.<Integer> iterate(null, null, i -> i + 1);
      fail("null hasNext predicate is not supported");
    }
    catch (AssertionException expected) {
      // expected
    }

    try {
      StreamUtility.<Integer> iterate(null, i -> i < 10, null);
      fail("null next function is not supported");
    }
    catch (AssertionException expected) {
      // expected
    }
  }

  @Test
  public void testIterate() {
    assertEquals(0, StreamUtility.iterate(0, i -> false, i -> failUnaryOperator(i)).count());
    assertEquals(Arrays.asList(0), StreamUtility.iterate(0, i -> i <= 0, i -> i + 1).collect(Collectors.toList()));
    assertEquals(Arrays.asList(0, 1), StreamUtility.iterate(0, i -> i <= 1, i -> i + 1).collect(Collectors.toList()));

    CircuitBreaker<Integer> cb = new CircuitBreaker<Integer>();
    try {
      StreamUtility.iterate(0, i -> i <= 0, cb::next).count();
      fail("expecting " + IllegalStateException.class.getName());
    }
    catch (IllegalStateException expected) {
      // expected
    }
  }

  @Test
  public void testIterateSequential() {
    doTestIterate(false);
  }

  @Test
  public void testIterateParallel() {
    doTestIterate(true);
  }

  protected void doTestIterate(boolean parallel) {
    final int upperLimit = 10_000;
    try (Stream<Integer> stream = StreamUtility.iterate(0, i -> i < upperLimit, i -> i + 1)) {
      if (parallel) {
        stream.parallel();
      }
      int expected = 0;
      for (Integer actual : stream.collect(Collectors.toList())) {
        assertEquals(expected, actual.intValue());
        expected++;
      }
      assertEquals(upperLimit, expected);
    }
  }

  protected static class CircuitBreaker<T> {
    private int m_counter = 0;

    public T next(T current) {
      if (m_counter > 5_000) {
        throw new IllegalStateException("endless loop (" + m_counter + " invocations)");
      }
      m_counter++;
      return current;
    }
  }

  @Test
  public void testTakeWhileEmptyStream() {
    assertEquals(0, StreamUtility.takeWhile(Stream.empty(), x -> failPredicate()).count()); // predicate is not expected to be tested
  }

  @Test
  public void testTakeWhileSingleElementStream() {
    assertEquals(1, StreamUtility.takeWhile(Stream.of("a"), x -> true).count());
    assertEquals(0, StreamUtility.takeWhile(Stream.of("a"), x -> false).count());
  }

  @Test
  public void testTakeWhileMultipleElementsStream() {
    assertEquals(2, StreamUtility.takeWhile(Stream.of("a", "b"), x -> true).count());
    assertEquals(1, StreamUtility.takeWhile(Stream.of("a", "b"), x -> "a".equals(x)).count());

    // takeWhile is not a filter, i.e. if the takeWhile predicate states false, no more elemnts are taken
    assertEquals(1, StreamUtility.takeWhile(Stream.of("a", "b", "a"), x -> "a".equals(x)).count());
  }

  @Test
  public void testTakeWhileSequentialStream() {
    doTestTakeWhileIntStream(false, false);
    doTestTakeWhileIntStream(false, true);
  }

  @Test
  public void testTakeWhileParallelStream() {
    doTestTakeWhileIntStream(true, false);
    doTestTakeWhileIntStream(true, true);
  }

  protected void doTestTakeWhileIntStream(boolean parallelSourceStream, boolean parallelTakeWhileStream) {
    final int upperLimit = 10_000;
    final int takeWhileLimit = 5_000;
    try (IntStream intStream = IntStream.range(0, upperLimit)) {
      if (parallelSourceStream) {
        intStream.parallel();
      }
      Stream<Integer> takeWhileStream = StreamUtility.takeWhile(intStream.mapToObj(Integer::valueOf), i -> i < takeWhileLimit);
      if (parallelTakeWhileStream) {
        takeWhileStream.parallel();
      }
      int expected = 0;
      for (Integer actual : takeWhileStream.collect(Collectors.toList())) {
        assertEquals(expected, actual.intValue());
        expected++;
      }
      assertEquals(takeWhileLimit, expected);
    }
  }

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
  public void testToLinkedHashMap() {
    String[] values = new String[]{"one", "two", "three"};
    LinkedHashMap<String, Integer> map = Stream.of(values).collect(StreamUtility.toLinkedHashMap(t -> t, t -> t.length()));
    assertArrayEquals(values, map.keySet().toArray());
  }

  @Test(expected = IllegalStateException.class)
  public void testToLinkedHashMapDuplicatedKey() {
    String[] values = new String[]{"one", "one"};
    Stream.of(values).collect(StreamUtility.toLinkedHashMap(t -> t, t -> t));
  }

}
