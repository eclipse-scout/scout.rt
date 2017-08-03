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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

/**
 * JUnit tests for {@link CollectionUtility}
 */
public class CollectionUtilityTest {

  @Test
  public void testEqualsCollectionNullAndEmpty() {
    Queue<Object> s = createQueue("a", "b");
    assertTrue(CollectionUtility.equalsCollection(null, null));
    assertTrue(CollectionUtility.equalsCollection(s, s));
    assertFalse(CollectionUtility.equalsCollection(s, null));
    assertFalse(CollectionUtility.equalsCollection(null, s));
  }

  @Test
  public void testEqualsCollection() {
    assertTrue(CollectionUtility.equalsCollection(createQueue("a", "b"), createQueue("a", "b")));
    assertTrue(CollectionUtility.equalsCollection(createQueue("a", "b"), createQueue("b", "a")));
    assertFalse(CollectionUtility.equalsCollection(createQueue("a", "b"), createQueue("a")));
    assertFalse(CollectionUtility.equalsCollection(createQueue("a", "b"), createQueue("x", "y")));
    assertFalse(CollectionUtility.equalsCollection(createQueue("a", "b"), createQueue("a", "a")));
    assertFalse(CollectionUtility.equalsCollection(createQueue("a", "a", "b"), createQueue("a", "b", "b")));
  }

  @Test
  public void testEqualsCollectionConsinderingElementPosition() {
    assertTrue(CollectionUtility.equalsCollection(createQueue("a", "b"), createQueue("a", "b"), true));
    assertFalse(CollectionUtility.equalsCollection(createQueue("a", "b"), createQueue("b", "a"), true));
    assertFalse(CollectionUtility.equalsCollection(createQueue("a", "b"), createQueue("a"), true));
    assertFalse(CollectionUtility.equalsCollection(createQueue("a", "b"), createQueue("a", "a"), true));
    assertFalse(CollectionUtility.equalsCollection(createQueue("a", "b"), createQueue("x", "y"), true));
    assertFalse(CollectionUtility.equalsCollection(createQueue("a", "a", "b"), createQueue("a", "b", "b"), true));
  }

  @Test
  public void testEqualsCollectionMixedCollectionTypes() {
    assertTrue(CollectionUtility.equalsCollection(CollectionUtility.hashSet("a", "b"), createList("a", "b")));
    assertFalse(CollectionUtility.equalsCollection(CollectionUtility.hashSet("a", "b", "c"), createList("a", "b", "b")));
  }

  /**
   * Test for {@link CollectionUtility#equalsCollection(List<? extends T> c1, List<? extends T> c2)}
   */
  @Test
  public void testEqualsCollectionList() {
    List<Object> l = createList("a", "b");
    assertTrue(CollectionUtility.equalsCollection(null, null));
    assertTrue(CollectionUtility.equalsCollection(l, l));
    assertFalse(CollectionUtility.equalsCollection(l, null));
    assertFalse(CollectionUtility.equalsCollection(null, l));

    assertTrue(CollectionUtility.equalsCollection(createList("a", "b"), createList("a", "b")));
    assertFalse(CollectionUtility.equalsCollection(createList("a", "b"), createList("a")));
    assertFalse(CollectionUtility.equalsCollection(createList("a", "b"), createList("b", "a")));
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
  public void testContains() {
    // Test for single valued list
    assertTrue(CollectionUtility.contains(createList(2L, 1L), 1L));
    assertFalse(CollectionUtility.contains(createList(2L, 3L), 1L));
    // Test for null collections
    assertFalse(CollectionUtility.contains(createList(2L, 3L), null));
    assertFalse(CollectionUtility.contains(null, 2L));
    // Test for null elements
    assertTrue(CollectionUtility.contains(createList(null, 1L), null));
  }

  @Test
  public void testContainsAll() {
    // Test for single valued list
    assertTrue(CollectionUtility.containsAll(createList(2L, 1L), 1L));
    assertTrue(CollectionUtility.containsAll(createList(1L), 1L));
    assertTrue(CollectionUtility.containsAll(createList(2L, 1L), createList(1L)));
    assertTrue(CollectionUtility.containsAll(createList(1L), createList(1L)));
    // Test for null collections
    assertTrue(CollectionUtility.containsAll(createList(2L, 3L), (Collection<?>) null));
    assertTrue(CollectionUtility.containsAll(createList(2L, 3L)));
    assertFalse(CollectionUtility.containsAll(null, 2L));
    assertTrue(CollectionUtility.containsAll(createList(2L, 3L), Collections.emptyList()));
    // Test for null elements
    assertFalse(CollectionUtility.containsAll(createList(1L), createList(1L, null)));
    assertTrue(CollectionUtility.containsAll(createList(null, 1L), createList(1L, null)));
    // Test for contained elements
    assertTrue(CollectionUtility.containsAll(createList(1L, 2L, 3L, 4L), createList(3L, 2L, 1L)));
    assertFalse(CollectionUtility.containsAll(createList(2L, 1L), createList(1L, 3L)));
  }

  @Test
  public void testContainsAny() {
    // Test for single valued list
    assertTrue(CollectionUtility.containsAny(createList(2L, 1L), createList(1L)));
    assertFalse(CollectionUtility.containsAny(createList(2L, 3L), createList(1L)));
    // Test for null collections
    assertFalse(CollectionUtility.containsAny(createList(2L, 3L), (Collection<?>) null));
    assertFalse(CollectionUtility.containsAny(null, createList(2L, 3L)));
    assertFalse(CollectionUtility.containsAny((Collection<Long>) null, (Collection<Long>) null));
    // Test for null elements
    assertTrue(CollectionUtility.containsAny(createList(null, 1L), createList(new Object[]{null})));
  }

  @Test
  public void testContainsAnyArray() {
    // Test for single valued list
    assertTrue(CollectionUtility.containsAny(createList(2L, 1L), new Object[]{1L}));
    assertFalse(CollectionUtility.containsAny(createList(2L, 3L), new Object[]{1L}));
    // Test for null collections
    assertFalse(CollectionUtility.containsAny(createList(2L, 3L), (Object[]) null));
    assertFalse(CollectionUtility.containsAny(null, new Object[]{2L, 3L}));
    assertFalse(CollectionUtility.containsAny((Collection<Object>) null, (Object[]) null));

    // Test for null elements
    assertTrue(CollectionUtility.containsAny(createList(null, 1L), createList(new Object[]{null})));
  }

  @Test
  public void testGetElement() {
    // Test that the item is returned from the position - not a copy or similar
    String item = "1";
    assertSame(item, CollectionUtility.getElement(createList("1", item, "1"), 1));
    // Test out of bounds
    assertNull(CollectionUtility.getElement(createList(1L, 2L, 3L), -1));
    assertNull(CollectionUtility.getElement(createList(1L, 2L, 3L), 3));
  }

  @Test
  public void testTruncateList() {
    List<Object> list = Collections.unmodifiableList(createList(1L, 2L, 3L, 4L, 5L));
    ArrayList<Object> newList = CollectionUtility.truncateList(list, 3);
    assertNotNull(newList);
    assertEquals(3, newList.size());
    assertEquals(1L, list.get(0));
    assertEquals(2L, list.get(1));
    assertEquals(3L, list.get(2));

    // Test size is equal to actual size
    newList = CollectionUtility.truncateList(list, 5);
    assertEquals(list, newList); // Compare collection

    // Test max size larger than actual size
    newList = CollectionUtility.truncateList(list, 6);
    assertEquals(list, newList); // Compare collection

    // Test null
    newList = CollectionUtility.truncateList(null, 1);
    assertNotNull(newList);
    assertTrue(newList.isEmpty());
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

  @Test
  public void testIsEmpty() {
    assertTrue(CollectionUtility.isEmpty((Map<?, ?>) null));
    assertTrue(CollectionUtility.isEmpty((Collection<?>) null));
    assertTrue(CollectionUtility.isEmpty(new HashMap<String, String>()));
    assertTrue(CollectionUtility.isEmpty(new ArrayList()));
    assertFalse(CollectionUtility.isEmpty(CollectionUtility.arrayList("a")));
    assertFalse(CollectionUtility.isEmpty(CollectionUtility.arrayList("a", "b")));

    Map<String, String> map = new HashMap<>();
    map.put("a", "b");
    assertFalse(CollectionUtility.isEmpty(map));
  }

  @Test
  public void testHashCode() {
    LinkedHashSet<CompositeObject> s = new LinkedHashSet<>();
    CompositeObject a = new CompositeObject("1");
    s.add(a);
    s.add(new CompositeObject("2"));
    LinkedHashSet<CompositeObject> s1 = new LinkedHashSet<>(s);
    s.remove(a);
    s.add(a);
    LinkedHashSet<CompositeObject> s2 = new LinkedHashSet<>(s);
    assertEquals(s1.hashCode(), s2.hashCode());
    assertEquals(CollectionUtility.hashCode(s1), CollectionUtility.hashCode(s2));
    assertEquals(s1, s2);
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
