/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

/**
 * JUnit tests for {@link CollectionUtility}
 */
public class CollectionUtilityTest {

  /**
   * Test for {@link CollectionUtility#equalsCollection(Collection<? extends T> c1, Collection<? extends T> c2)} and
   * {@link CollectionUtility#equalsCollection(Collection<? extends T> c1, Collection<? extends T> c2, boolean
   * considerElementPosition)}
   */
  @Test
  public void testEqualsCollection() {
    Queue<Object> s = createQueue("a", "b");
    assertEquals(true, CollectionUtility.equalsCollection(null, null));
    assertEquals(true, CollectionUtility.equalsCollection(s, s));
    assertEquals(false, CollectionUtility.equalsCollection(s, null));
    assertEquals(false, CollectionUtility.equalsCollection(null, s));

    assertEquals(true, CollectionUtility.equalsCollection(createQueue("a", "b"), createQueue("a", "b")));
    assertEquals(true, CollectionUtility.equalsCollection(createQueue("a", "b"), createQueue("b", "a")));
    assertEquals(false, CollectionUtility.equalsCollection(createQueue("a", "b"), createQueue("a")));
    assertEquals(false, CollectionUtility.equalsCollection(createQueue("a", "b"), createQueue("x", "y")));

    assertEquals(true, CollectionUtility.equalsCollection(createQueue("a", "b"), createQueue("a", "b"), true));
    assertEquals(false, CollectionUtility.equalsCollection(createQueue("a", "b"), createQueue("b", "a"), true));
    assertEquals(false, CollectionUtility.equalsCollection(createQueue("a", "b"), createQueue("a"), true));
    assertEquals(false, CollectionUtility.equalsCollection(createQueue("a", "b"), createQueue("x", "y"), true));
  }

  /**
   * Test for {@link CollectionUtility#equalsCollection(List<? extends T> c1, List<? extends T> c2)}
   */
  @Test
  public void testEqualsCollectionList() {
    List<Object> l = createList("a", "b");
    assertEquals(true, CollectionUtility.equalsCollection(null, null));
    assertEquals(true, CollectionUtility.equalsCollection(l, l));
    assertEquals(false, CollectionUtility.equalsCollection(l, null));
    assertEquals(false, CollectionUtility.equalsCollection(null, l));

    assertEquals(true, CollectionUtility.equalsCollection(createList("a", "b"), createList("a", "b")));
    assertEquals(false, CollectionUtility.equalsCollection(createList("a", "b"), createList("a")));
    assertEquals(false, CollectionUtility.equalsCollection(createList("a", "b"), createList("b", "a")));
  }

  /**
   * Test for {@link CollectionUtility#firstElement(Object)}
   */
  @Test
  public void testFirstElementObject() {
    // null
    assertNull(CollectionUtility.firstElement((Object) null));
    // empty
    assertNull(CollectionUtility.firstElement(new Object()));
    // Cover switch to firstElement(Collection)
    assertEquals(TriState.FALSE, CollectionUtility.firstElement((Object) EnumSet.of(TriState.FALSE)));
  }

  /**
   * Test for {@link CollectionUtility#firstElement(Collection) }
   */
  @Test
  public void testFirstElementCollection() {
    // null
    assertNull(CollectionUtility.firstElement((Collection<?>) null));
    // empty
    assertNull(CollectionUtility.firstElement((EnumSet.noneOf(TriState.class))));
    // one element
    assertEquals(TriState.FALSE, CollectionUtility.firstElement(EnumSet.of(TriState.FALSE)));
    // two elements
    assertEquals(TriState.FALSE, CollectionUtility.firstElement(EnumSet.of(TriState.UNDEFINED, TriState.FALSE))); // EnumSet in order of Enum definition
  }

  /**
   * Test for {@link CollectionUtility#firstElement(List) }
   */
  @Test
  public void testFirstElementList() {
    // null
    assertNull(CollectionUtility.firstElement((List<?>) null));
    // empty
    assertNull(CollectionUtility.firstElement((new ArrayList<Object>())));
    // one element
    assertEquals((Long) 1L, CollectionUtility.firstElement(new ArrayList<Long>() {
      private static final long serialVersionUID = 1L;
      {
        add(1L);
      }
    }));
    // two elements
    assertEquals((Long) 1L, CollectionUtility.firstElement(new ArrayList<Long>() {
      private static final long serialVersionUID = 1L;
      {
        add(1L);
        add(2L);
      }
    }));
    // many elements
    assertEquals((Long) 1L, CollectionUtility.firstElement(new ArrayList<Long>() {
      private static final long serialVersionUID = 1L;
      {
        add(1L);
        add(2L);
        add(3L);
        add(4L);
      }
    }));
  }

  /**
   * Test for {@link CollectionUtility#firstElement(SortedMap)}
   */
  @Test
  public void testFirstElementSortedMap() {
    // null
    assertNull(CollectionUtility.firstElement((SortedMap<?, ?>) null));
    // empty
    assertNull(CollectionUtility.firstElement((Collections
        .unmodifiableSortedMap(new TreeMap<Object, Object>()))));
    // one element
    assertEquals("ABC", CollectionUtility.firstElement(new TreeMap<Integer, String>() {
      private static final long serialVersionUID = 1L;

      {
        put(1, "ABC");
        put(2, "ZZZ");
      }
    }));
    // many elements
    assertEquals("-1", CollectionUtility.firstElement(new TreeMap<Integer, String>() {
      private static final long serialVersionUID = 1L;

      {
        put(1, "ABC");
        put(2, "ZZZ");
        put(0, "000");
        put(-1, "-1");
      }
    }));
  }

  /**
   * Test for {@link CollectionUtility#lastElement(List<T> c)}
   */
  @Test
  public void testLastElementList() {
    assertNull(CollectionUtility.lastElement((List<Object>) null));
    assertNull(CollectionUtility.lastElement(new ArrayList<Object>()));
    assertEquals("a", CollectionUtility.lastElement(createList("a")));
    assertEquals("b", CollectionUtility.lastElement(createList("a", "b")));
  }

  /**
   * Test for {@link CollectionUtility#slice(List, int, int)}
   */
  @Test
  public void testSlice() {
    List<String> nullList = null;
    List<String> emptyList = new ArrayList<String>();
    List<String> testList = Arrays.asList("foo", "bar", "test");
    List<String> testList_1 = Arrays.asList("foo");
    List<String> testList_23 = Arrays.asList("bar", "test");
    List<String> testList_3 = Arrays.asList("test");

    // empty and null input lists
    assertEquals(emptyList, CollectionUtility.slice(nullList, 0, 0));
    assertEquals(emptyList, CollectionUtility.slice(emptyList, 0, 0));

    // negative indices
    assertEquals(testList, CollectionUtility.slice(testList, 0, -1));
    assertEquals(testList_23, CollectionUtility.slice(testList, 1, -1));
    assertEquals(testList_3, CollectionUtility.slice(testList, -1, -1));

    // positive indices
    assertEquals(testList, CollectionUtility.slice(testList, 0, 2));
    assertEquals(testList_1, CollectionUtility.slice(testList, 0, 0));
    assertEquals(testList_23, CollectionUtility.slice(testList, 1, 2));
    assertEquals(testList_3, CollectionUtility.slice(testList, 2, 2));
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testSliceExceptionTo1() throws Exception {
    CollectionUtility.slice(Arrays.asList("foo", "bar", "test"), -4, 2);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testSliceExceptionTo2() throws Exception {
    CollectionUtility.slice(Arrays.asList("foo", "bar", "test"), 3, 2);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testSliceExceptionFrom1() throws Exception {
    CollectionUtility.slice(Arrays.asList("foo", "bar", "test"), 0, -4);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testSliceExceptionFrom2() throws Exception {
    CollectionUtility.slice(Arrays.asList("foo", "bar", "test"), 0, 3);
  }

  /**
   * Test for {@link CollectionUtility#lastElement(List<T> c)}
   */
  @Test
  public void testLastElementMap() {
    assertNull(CollectionUtility.lastElement((SortedMap<Object, Object>) null));
    assertNull(CollectionUtility.lastElement(new TreeMap<Object, Object>()));
    assertEquals("a", CollectionUtility.lastElement(createSortedMap("a")));
    assertEquals("b", CollectionUtility.lastElement(createSortedMap("a", "b")));
  }

  /**
   * Test for {@link CollectionUtility#size(Collection<T> list)}
   */
  @Test
  public void testSize() {
    assertEquals(0, CollectionUtility.size(null));
    assertEquals(0, CollectionUtility.size(new ArrayList<Object>()));
    assertEquals(1, CollectionUtility.size(createList("1")));
    assertEquals(2, CollectionUtility.size(createList("1", "2")));
  }

  @Test
  public void testEmptyArrayList() {
    ArrayList<Integer> list = CollectionUtility.emptyArrayList();
    assertNotNull(list);
    // Test that it is a mutable list.
    assertTrue(list.isEmpty());
    list.add(1);
    assertFalse(list.isEmpty());
  }

  @Test
  public void testEmptyHashSet() {
    HashSet<Integer> set = CollectionUtility.emptyHashSet();
    assertNotNull(set);
    // Test that it is a mutable set.
    assertTrue(set.isEmpty());
    set.add(1);
    assertFalse(set.isEmpty());
  }

  @Test
  public void testEmptyHashMap() {
    HashMap<Integer, String> map = CollectionUtility.emptyHashMap();
    assertNotNull(map);
    // Test that it is a mutable map.
    assertTrue(map.isEmpty());
    map.put(1, "I love Scout");
    assertFalse(map.isEmpty());
  }

  private List<Object> createList(Object... elements) {
    List<Object> list = new ArrayList<Object>();
    for (Object o : elements) {
      list.add(o);
    }
    return list;
  }

  private SortedMap<Integer, Object> createSortedMap(Object... elements) {
    SortedMap<Integer, Object> map = new TreeMap<Integer, Object>();
    int counter = 0;
    for (Object o : elements) {
      map.put(counter++, o);
    }
    return map;
  }

  private Queue<Object> createQueue(Object... elements) {
    Queue<Object> list = new ConcurrentLinkedQueue<Object>();
    for (Object o : elements) {
      list.add(o);
    }
    return list;
  }
}
