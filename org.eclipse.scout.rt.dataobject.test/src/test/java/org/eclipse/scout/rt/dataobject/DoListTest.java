/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Before;
import org.junit.Test;

/**
 * Various low-level tests for {@link DoList}
 */
public class DoListTest {

  protected DoList<String> m_testDoList;

  @Before
  public void before() {
    m_testDoList = new DoList<>();
    m_testDoList.add("foo");
    m_testDoList.add("bar");
    m_testDoList.add("baz");
  }

  @Test
  public void testDoListConstructor() {
    DoList<String> list = new DoList<>();
    assertTrue(list.exists());
    assertNotNull(list.get());
    assertTrue(list.get() instanceof ArrayList);
  }

  @Test
  public void testDoListDetailedConstructor() {
    ArrayList<String> list = new ArrayList<>();
    list.add("one");
    DoList<String> doList = new DoList<>("attributeName", m_lazyCreate, list);
    assertFalse(doList.exists());
    assertNotNull(doList.get());
    assertTrue(doList.get() instanceof ArrayList);
    assertSame(list, doList.get());
  }

  @Test
  public void testDoListDetailedConstructorNullInitialValue() {
    DoList<String> doList = new DoList<>("attributeName", m_lazyCreate, null);
    assertFalse(doList.exists());
    assertNotNull(doList.get());
    assertTrue(doList.get() instanceof ArrayList);
  }

  protected Consumer<DoNode<List<String>>> m_lazyCreate = attribute -> {
    /* nop */
  };

  @Test
  public void testCreateExists() {
    DoList<String> list = new DoList<>(null, m_lazyCreate, null);
    assertFalse(list.exists());
    list.create();
    assertTrue(list.exists());

    list = new DoList<>(null, m_lazyCreate, null);
    assertFalse(list.exists());
    list.set(Arrays.asList("foo", "bar"));
    assertTrue(list.exists());

    list = new DoList<>(null, m_lazyCreate, null);
    assertFalse(list.exists());
    list.get();
    assertTrue(list.exists());
  }

  @Test
  public void testOf() {
    List<String> list = Arrays.asList("foo", "bar");
    DoList<String> doList = DoList.of(list);
    assertTrue(doList.exists());
    assertEquals("foo", doList.get(0));
    assertEquals("bar", doList.get(1));
    assertSame(list, doList.get());
  }

  @Test
  public void testOfNull() {
    DoList<String> list = DoList.of(null);
    assertTrue(list.exists());
    assertTrue(list.isEmpty());
    assertEquals(new ArrayList<>(), list.get());
  }

  @Test
  public void testGet() {
    assertEquals(Arrays.asList("foo", "bar", "baz"), m_testDoList.get());
  }

  @Test
  public void testSet() {
    List<String> values = Arrays.asList("foo");
    m_testDoList.set(values);
    assertEquals(Arrays.asList("foo"), m_testDoList.get());
    assertSame(values, m_testDoList.get());

    m_testDoList.set(Collections.emptyList());
    assertEquals(Collections.emptyList(), m_testDoList.get());
    assertTrue(m_testDoList.isEmpty());
    assertNotNull(m_testDoList.get());

    m_testDoList.set(null);
    assertEquals(Collections.emptyList(), m_testDoList.get());
    assertTrue(m_testDoList.isEmpty());
    assertNotNull(m_testDoList.get());
  }

  @Test
  public void testContains() {
    assertTrue(m_testDoList.contains("foo"));
    assertTrue(m_testDoList.contains("bar"));
    assertTrue(m_testDoList.contains("baz"));
    assertFalse(m_testDoList.contains("no"));
  }

  @Test
  public void testGetByIndex() {
    assertEquals("foo", m_testDoList.get(0));
    assertEquals("bar", m_testDoList.get(1));
    assertEquals("baz", m_testDoList.get(2));
  }

  @Test
  public void testAdd() {
    m_testDoList.add("qux");
    assertEquals("qux", m_testDoList.get(3));

    m_testDoList.add(null);
    assertNull(m_testDoList.get(4));
  }

  @Test
  public void testAddAllCollection() {
    Collection<String> collection = new LinkedHashSet<>();
    collection.add("qux");
    collection.add("quux");
    m_testDoList.addAll(collection);
    assertEquals("qux", m_testDoList.get(3));
    assertEquals("quux", m_testDoList.get(4));
  }

  @Test
  public void testAddAllCollectionNull() {
    Collection<String> collection = null;
    m_testDoList.addAll(collection);
    assertEquals(3, m_testDoList.size());
  }

  @Test
  public void testAddAllArray() {
    m_testDoList.addAll("qux", "quux");
    assertEquals("qux", m_testDoList.get(3));
    assertEquals("quux", m_testDoList.get(4));
  }

  @Test
  public void testAddAllArrayNull() {
    String[] strings = null;
    m_testDoList.addAll(strings);
    assertEquals(3, m_testDoList.size());
  }

  @Test
  public void testRemove() {
    assertTrue(m_testDoList.remove("bar"));
    assertEquals(Arrays.asList("foo", "baz"), m_testDoList.get());

    assertTrue(m_testDoList.remove("foo"));
    assertEquals(Arrays.asList("baz"), m_testDoList.get());

    assertFalse(m_testDoList.remove("bar"));
    assertEquals(Arrays.asList("baz"), m_testDoList.get());

    assertFalse(m_testDoList.remove("notExistingElement"));
    assertEquals(Arrays.asList("baz"), m_testDoList.get());

    assertFalse(m_testDoList.remove(null));
    assertEquals(Arrays.asList("baz"), m_testDoList.get());

    assertTrue(m_testDoList.remove("baz"));
    assertEquals(Arrays.asList(), m_testDoList.get());
  }

  @Test
  public void testRemoveByIndex() {
    String element = m_testDoList.remove(1);
    assertEquals("bar", element);
    assertEquals(Arrays.asList("foo", "baz"), m_testDoList.get());

    element = m_testDoList.remove(0);
    assertEquals("foo", element);
    assertEquals(Arrays.asList("baz"), m_testDoList.get());

    element = m_testDoList.remove(0);
    assertEquals("baz", element);
    assertEquals(Arrays.asList(), m_testDoList.get());
  }

  @Test
  public void testRemoveIntList() {
    DoList<Integer> intList = new DoList<>();
    intList.add(1);
    intList.add(2);
    intList.add(3);
    intList.remove(1); // by index, 2 is removed
    assertEquals(Arrays.asList(1, 3), intList.get());
    intList.remove(Integer.valueOf(3)); // by value
    assertEquals(Arrays.asList(1), intList.get());
    intList.remove(Integer.valueOf(1)); // by value
    assertTrue(intList.isEmpty());
  }

  @Test
  public void testRemoveAllCollection() {
    assertTrue(m_testDoList.removeAll(Arrays.asList("bar")));
    assertEquals(Arrays.asList("foo", "baz"), m_testDoList.get());

    assertTrue(m_testDoList.removeAll(Arrays.asList("baz", "foo")));
    assertEquals(Arrays.asList(), m_testDoList.get());

    assertFalse(m_testDoList.removeAll(Arrays.asList("abc", "def")));
    assertEquals(Arrays.asList(), m_testDoList.get());
  }

  @Test
  public void testRemoveAllWildcardCollection() {
    // text removeAll using "Collection<? extends String>"
    m_testDoList.clear();
    m_testDoList.add("foo");
    assertEquals(Arrays.asList("foo"), m_testDoList.get());
    Collection<? extends String> elementsToRemove = new ArrayList<>(Arrays.asList("foo"));
    m_testDoList.removeAll(elementsToRemove);
    assertEquals(Arrays.asList(), m_testDoList.get());
  }

  @Test
  public void testRemoveAllCollectionNull() {
    Collection<String> collection = null;
    assertFalse(m_testDoList.removeAll(collection));
    assertEquals(3, m_testDoList.size());
  }

  @Test
  public void testRemoveAllArray() {
    assertTrue(m_testDoList.removeAll("bar"));
    assertEquals(Arrays.asList("foo", "baz"), m_testDoList.get());

    assertTrue(m_testDoList.removeAll("baz", "foo"));
    assertEquals(Arrays.asList(), m_testDoList.get());

    assertFalse(m_testDoList.removeAll("abc", "def"));
    assertEquals(Arrays.asList(), m_testDoList.get());
  }

  @Test
  public void testRemoveAllArrayNull() {
    String[] strings = null;
    assertFalse(m_testDoList.removeAll(strings));
    assertEquals(3, m_testDoList.size());
  }

  @Test
  public void testUpdateAllCollection() {
    m_testDoList.updateAll(Arrays.asList("a", "b"));
    assertEquals(Arrays.asList("a", "b"), m_testDoList.get());

    m_testDoList.updateAll(Collections.emptyList());
    assertEquals(Arrays.asList(), m_testDoList.get());
    assertTrue(m_testDoList.isEmpty());

    m_testDoList.updateAll(Arrays.asList("a", "b"));
    m_testDoList.updateAll((Collection<String>) null);
    assertEquals(Arrays.asList(), m_testDoList.get());
    assertTrue(m_testDoList.isEmpty());

    Collection<? extends String> newValues = new ArrayList<>(Arrays.asList("foo"));
    m_testDoList.updateAll(newValues);
    assertEquals(Arrays.asList("foo"), m_testDoList.get());
  }

  @Test
  public void testUpdateAllArray() {
    m_testDoList.updateAll("a", "b");
    assertEquals(Arrays.asList("a", "b"), m_testDoList.get());

    m_testDoList.updateAll(Collections.emptyList());
    assertEquals(Arrays.asList(), m_testDoList.get());
    assertTrue(m_testDoList.isEmpty());

    m_testDoList.updateAll("a", "b");
    m_testDoList.updateAll((String[]) null);
    assertEquals(Arrays.asList(), m_testDoList.get());
    assertTrue(m_testDoList.isEmpty());
  }

  @Test
  public void testClear() {
    assertEquals(3, m_testDoList.size());
    m_testDoList.clear();
    assertEquals(0, m_testDoList.size());
    m_testDoList.clear();
    assertEquals(0, m_testDoList.size());
  }

  @Test
  public void testFirst() {
    assertEquals("foo", m_testDoList.first());
    m_testDoList.remove("foo");
    assertEquals("bar", m_testDoList.first());
    m_testDoList.clear();
    assertNull(m_testDoList.first());
  }

  @Test
  public void testLast() {
    assertEquals("baz", m_testDoList.last());
    m_testDoList.remove("baz");
    assertEquals("bar", m_testDoList.last());
    m_testDoList.clear();
    assertNull(m_testDoList.last());
  }

  @Test
  public void testSize() {
    assertEquals(3, m_testDoList.size());
    m_testDoList.add("foo");
    assertEquals(4, m_testDoList.size());
    m_testDoList.clear();
    assertEquals(0, m_testDoList.size());
  }

  @Test
  public void testIsEmpty() {
    assertFalse(m_testDoList.isEmpty());
    m_testDoList.clear();
    assertTrue(m_testDoList.isEmpty());
  }

  @Test
  public void testIterator() {
    Iterator<String> iter = m_testDoList.iterator();
    assertTrue(iter.hasNext());
    assertEquals("foo", iter.next());
    assertTrue(iter.hasNext());
    assertEquals("bar", iter.next());
    assertTrue(iter.hasNext());
    assertEquals("baz", iter.next());
    assertFalse(iter.hasNext());
  }

  @Test
  public void testIterable() {
    List<String> actual = new ArrayList<>();
    for (String element : m_testDoList) {
      actual.add(element);
    }
    assertEquals(Arrays.asList("foo", "bar", "baz"), actual);
  }

  @Test
  public void testListIterator() {
    ListIterator<String> iter = m_testDoList.listIterator();
    assertFalse(iter.hasPrevious());
    assertTrue(iter.hasNext());
    assertEquals("foo", iter.next());

    assertTrue(iter.hasPrevious());
    assertTrue(iter.hasNext());
    assertEquals("bar", iter.next());

    assertTrue(iter.hasPrevious());
    assertTrue(iter.hasNext());
    assertEquals("baz", iter.next());

    assertFalse(iter.hasNext());
    assertTrue(iter.hasPrevious());
    iter.previous(); // move one back
    assertEquals("bar", iter.previous());

    assertTrue(iter.hasPrevious());
    assertTrue(iter.hasNext());
    assertEquals("foo", iter.previous());
  }

  @Test
  public void testStream() {
    assertEquals("foo", m_testDoList.stream().findFirst().get());
    assertEquals("foo", m_testDoList.stream().findAny().get());

    assertEquals(Arrays.asList("foo", "bar", "baz"), m_testDoList.stream().collect(Collectors.toList()));
  }

  @Test
  public void testParallelStream() {
    assertTrue(CollectionUtility.equalsCollection(Arrays.asList("foo", "bar", "baz"), m_testDoList.parallelStream().collect(Collectors.toSet()), false));
  }

  @Test
  public void testSort() {
    m_testDoList.sort(Comparator.naturalOrder());
    assertEquals(Arrays.asList("bar", "baz", "foo"), m_testDoList.get());

    m_testDoList.sort(Comparator.reverseOrder());
    assertEquals(Arrays.asList("foo", "baz", "bar"), m_testDoList.get());
  }

  protected Function<String, DoValue<String>> listValueAccessor = new Function<>() {
    @Override
    public DoValue<String> apply(String input) {
      for (String item : m_testDoList.get()) {
        if (item.equals(input)) {
          return DoValue.of(input);
        }
      }
      return null;
    }
  };

  @Test
  public void testFindFirstFunction() {
    assertEquals("bar", m_testDoList.findFirst(listValueAccessor, "bar"));
  }

  @Test
  public void testFindPredicateOfV() {
    assertEquals(Arrays.asList("bar"), m_testDoList.find(listValueAccessor, "bar"));
  }

  @Test
  public void testFindFirst() {
    assertEquals("bar", m_testDoList.findFirst("bar"::equals));
    assertNull(m_testDoList.findFirst("myCustomSearchTerm"::equals));
  }

  @Test
  public void testFind() {
    assertEquals(Arrays.asList("bar"), m_testDoList.find("bar"::equals));
    assertEquals(Arrays.asList("bar", "baz"), m_testDoList.find((input) -> "bar".equals(input) || "baz".equals(input)));
  }

  /**
   * Methods from {@link DoList} must not create the node if it's not necessary.
   */
  @Test
  public void testIdempotentMethodCalls() {
    DoList<String> list = new DoList<>(null, m_lazyCreate, null);
    assertFalse(list.exists());

    assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
    assertFalse(list.exists());

    assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
    assertFalse(list.exists());

    assertNull(list.first());
    assertFalse(list.exists());

    assertNull(list.last());
    assertFalse(list.exists());

    assertFalse(list.listIterator().hasNext());
    assertFalse(list.exists());

    list.sort(Comparator.naturalOrder());
    assertFalse(list.exists());

    assertNotNull(list.toString());
    assertFalse(list.exists());
  }

  @Test
  public void testAttributeName() {
    assertNull(new DoList<>().getAttributeName());
    assertNull(DoList.of(Collections.emptyList()).getAttributeName());
    assertEquals("foo", new DoList<>("foo", null, null).getAttributeName());
  }

  @Test
  public void testEqualsHashCode() {
    DoList<String> list1 = new DoList<>();
    list1.add("foo");

    DoList<String> list2 = new DoList<>();
    list2.add("foo");

    assertEquals(list1, list2);
    assertEquals(list2, list1);
    assertEquals(list1, list1);
    assertEquals(list1.hashCode(), list2.hashCode());

    list1.add("bar");
    assertNotEquals(list1, list2);

    list2.add("bar");
    assertEquals(list1, list2);
    assertEquals(list1.hashCode(), list2.hashCode());

    assertNotEquals(null, list1);
    assertNotEquals(list1, new Object());
    assertNotEquals(null, list1);
    assertNotEquals(list1, new Object());

    list1 = new DoList<>();
    list1.add("foo");
    list1.add("bar");

    list2 = new DoList<>();
    list2.add("bar");
    list2.add("foo");

    assertNotEquals(list1, list2); // order of elements is relevant for equality
  }

  @Test
  public void testListBehavior() {
    DoList<String> list = new DoList<>();
    list.add("foo");
    list.add("bar");
    list.add("bar");
    list.add("baz");

    assertEquals(4, list.size());
    assertEquals("foo", list.get(0));
    assertEquals("bar", list.get(1));
    assertEquals("bar", list.get(2));
    assertEquals("baz", list.get(3));

    list.remove("bar"); // remove first instance
    assertEquals(3, list.size());
    assertEquals("foo", list.get(0));
    assertEquals("bar", list.get(1));
    assertEquals("baz", list.get(2));

    list.remove("bar"); // remove second instance
    assertEquals(2, list.size());
    assertEquals("foo", list.get(0));
    assertEquals("baz", list.get(1));
  }
}
