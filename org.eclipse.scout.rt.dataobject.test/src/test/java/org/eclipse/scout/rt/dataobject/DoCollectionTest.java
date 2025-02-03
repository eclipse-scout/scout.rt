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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Before;
import org.junit.Test;

/**
 * Various low-level tests for {@link DoCollection}
 */
public class DoCollectionTest {

  protected DoCollection<String> m_testDoCollection;

  @Before
  public void before() {
    m_testDoCollection = new DoCollection<>();
    m_testDoCollection.add("foo");
    m_testDoCollection.add("bar");
    m_testDoCollection.add("baz");
  }

  @Test
  public void testDoSetConstructor() {
    DoCollection<String> collection = new DoCollection<>();
    assertTrue(collection.exists());
  }

  @Test
  public void testDoCollectionDetailedConstructor() {
    Collection<String> collection = new ArrayList<>();
    collection.add("one");
    DoCollection<String> doCollection = new DoCollection<>("attributeName", m_lazyCreate, collection);
    assertFalse(doCollection.exists());
    assertNotNull(doCollection.get());
    assertTrue(doCollection.get() instanceof ArrayList);
    assertSame(collection, doCollection.get());
  }

  protected Consumer<DoNode<Collection<String>>> m_lazyCreate = attribute -> {
    /* nop */
  };

  @Test
  public void testCreateExists() {
    DoCollection<String> collection = new DoCollection<>(null, m_lazyCreate, null);
    assertFalse(collection.exists());
    collection.create();
    assertTrue(collection.exists());

    collection = new DoCollection<>(null, m_lazyCreate, null);
    assertFalse(collection.exists());
    collection.set(Arrays.asList("foo", "bar"));
    assertTrue(collection.exists());

    collection = new DoCollection<>(null, m_lazyCreate, null);
    assertFalse(collection.exists());
    collection.get();
    assertTrue(collection.exists());
  }

  @Test
  public void testOf() {
    DoCollection<String> collection = DoCollection.of(Arrays.asList("foo", "bar"));
    assertTrue(collection.exists());
    assertTrue(collection.contains("foo"));
    assertTrue(collection.contains("bar"));
  }

  @Test
  public void testOfNull() {
    DoCollection<String> collection = DoCollection.of(null);
    assertTrue(collection.exists());
    assertTrue(collection.isEmpty());
    assertCollectionEquals(new ArrayList<>(), collection.get());
  }

  @Test
  public void testGet() {
    assertEquals(Arrays.asList("foo", "bar", "baz"), m_testDoCollection.get());
  }

  @Test
  public void testSet() {
    m_testDoCollection.set(Arrays.asList("foo"));
    assertCollectionEquals(Arrays.asList("foo"), m_testDoCollection.get());

    m_testDoCollection.set(Collections.emptyList());
    assertCollectionEquals(Collections.emptyList(), m_testDoCollection.get());
    assertTrue(m_testDoCollection.isEmpty());
    assertNotNull(m_testDoCollection.get());

    m_testDoCollection.set(null);
    assertCollectionEquals(Collections.emptyList(), m_testDoCollection.get());
    assertTrue(m_testDoCollection.isEmpty());
    assertNotNull(m_testDoCollection.get());
  }

  @Test
  public void testContains() {
    assertTrue(m_testDoCollection.contains("foo"));
    assertTrue(m_testDoCollection.contains("bar"));
    assertTrue(m_testDoCollection.contains("baz"));
    assertFalse(m_testDoCollection.contains("no"));
  }

  @Test
  public void testAdd() {
    m_testDoCollection.add("qux");
    assertTrue(m_testDoCollection.contains("qux"));

    m_testDoCollection.add(null);
    assertTrue(m_testDoCollection.contains(null));
  }

  @Test
  public void testAddAllCollection() {
    Collection<String> collection = new LinkedHashSet<>();
    collection.add("qux");
    collection.add("quux");
    m_testDoCollection.addAll(collection);
    assertTrue(m_testDoCollection.contains("qux"));
    assertTrue(m_testDoCollection.contains("quux"));
  }

  @Test
  public void testAddAllCollectionNull() {
    Collection<String> collection = null;
    m_testDoCollection.addAll(collection);
    assertEquals(3, m_testDoCollection.size());
  }

  @Test
  public void testAddAllArray() {
    m_testDoCollection.addAll("qux", "quux");
    assertTrue(m_testDoCollection.contains("qux"));
    assertTrue(m_testDoCollection.contains("quux"));
  }

  @Test
  public void testAddAllArrayNull() {
    String[] strings = null;
    m_testDoCollection.addAll(strings);
    assertEquals(3, m_testDoCollection.size());
  }

  @Test
  public void testRemove() {
    assertTrue(m_testDoCollection.remove("bar"));
    assertCollectionEquals(Arrays.asList("foo", "baz"), m_testDoCollection.get());

    assertTrue(m_testDoCollection.remove("foo"));
    assertCollectionEquals(Arrays.asList("baz"), m_testDoCollection.get());

    assertFalse(m_testDoCollection.remove("bar"));
    assertCollectionEquals(Arrays.asList("baz"), m_testDoCollection.get());

    assertFalse(m_testDoCollection.remove("notExistingElement"));
    assertCollectionEquals(Arrays.asList("baz"), m_testDoCollection.get());

    assertFalse(m_testDoCollection.remove(null));
    assertCollectionEquals(Arrays.asList("baz"), m_testDoCollection.get());

    assertTrue(m_testDoCollection.remove("baz"));
    assertCollectionEquals(Arrays.asList(), m_testDoCollection.get());
  }

  @Test
  public void testRemoveIntList() {
    DoCollection<Integer> intCollection = new DoCollection<>();
    intCollection.add(1);
    intCollection.add(2);
    intCollection.add(3);
    intCollection.remove(1); // by value
    assertCollectionEquals(Arrays.asList(2, 3), intCollection.get());
    intCollection.remove(Integer.valueOf(3)); // by value
    assertCollectionEquals(Arrays.asList(2), intCollection.get());
    intCollection.remove(Integer.valueOf(2)); // by value
    assertTrue(intCollection.isEmpty());
  }

  @Test
  public void testRemoveAllCollection() {
    assertTrue(m_testDoCollection.removeAll(Arrays.asList("bar")));
    assertCollectionEquals(Arrays.asList("foo", "baz"), m_testDoCollection.get());

    assertTrue(m_testDoCollection.removeAll(Arrays.asList("baz", "foo")));
    assertCollectionEquals(Arrays.asList(), m_testDoCollection.get());

    assertFalse(m_testDoCollection.removeAll(Arrays.asList("abc", "def")));
    assertCollectionEquals(Arrays.asList(), m_testDoCollection.get());
  }

  @Test
  public void testRemoveAllWildcardCollection() {
    // text removeAll using "Collection<? extends String>"
    m_testDoCollection.clear();
    m_testDoCollection.add("foo");
    assertCollectionEquals(Arrays.asList("foo"), m_testDoCollection.get());
    Collection<? extends String> elementsToRemove = new ArrayList<>(Arrays.asList("foo"));
    m_testDoCollection.removeAll(elementsToRemove);
    assertCollectionEquals(Arrays.asList(), m_testDoCollection.get());
  }

  @Test
  public void testRemoveAllCollectionNull() {
    Collection<String> collection = null;
    assertFalse(m_testDoCollection.removeAll(collection));
    assertEquals(3, m_testDoCollection.size());
  }

  @Test
  public void testRemoveAllArray() {
    assertTrue(m_testDoCollection.removeAll("bar"));
    assertCollectionEquals(Arrays.asList("foo", "baz"), m_testDoCollection.get());

    assertTrue(m_testDoCollection.removeAll("baz", "foo"));
    assertCollectionEquals(Arrays.asList(), m_testDoCollection.get());

    assertFalse(m_testDoCollection.removeAll("abc", "def"));
    assertCollectionEquals(Arrays.asList(), m_testDoCollection.get());
  }

  @Test
  public void testRemoveAllArrayNull() {
    String[] strings = null;
    assertFalse(m_testDoCollection.removeAll(strings));
    assertEquals(3, m_testDoCollection.size());
  }

  @Test
  public void testUpdateAllCollection() {
    m_testDoCollection.updateAll(Arrays.asList("a", "b"));
    assertCollectionEquals(Arrays.asList("a", "b"), m_testDoCollection.get());

    m_testDoCollection.updateAll(Collections.emptyList());
    assertCollectionEquals(Arrays.asList(), m_testDoCollection.get());
    assertTrue(m_testDoCollection.isEmpty());

    m_testDoCollection.updateAll(Arrays.asList("a", "b"));
    m_testDoCollection.updateAll((Collection<String>) null);
    assertCollectionEquals(Arrays.asList(), m_testDoCollection.get());
    assertTrue(m_testDoCollection.isEmpty());

    Collection<? extends String> newValues = new ArrayList<>(Arrays.asList("foo"));
    m_testDoCollection.updateAll(newValues);
    assertCollectionEquals(Arrays.asList("foo"), m_testDoCollection.get());
  }

  @Test
  public void testUpdateAllArray() {
    m_testDoCollection.updateAll("a", "b");
    assertCollectionEquals(Arrays.asList("a", "b"), m_testDoCollection.get());

    m_testDoCollection.updateAll(Collections.emptyList());
    assertCollectionEquals(Arrays.asList(), m_testDoCollection.get());
    assertTrue(m_testDoCollection.isEmpty());

    m_testDoCollection.updateAll("a", "b");
    m_testDoCollection.updateAll((String[]) null);
    assertCollectionEquals(Arrays.asList(), m_testDoCollection.get());
    assertTrue(m_testDoCollection.isEmpty());
  }

  @Test
  public void testClear() {
    assertEquals(3, m_testDoCollection.size());
    m_testDoCollection.clear();
    assertEquals(0, m_testDoCollection.size());
    m_testDoCollection.clear();
    assertEquals(0, m_testDoCollection.size());
  }

  @Test
  public void testSize() {
    assertEquals(3, m_testDoCollection.size());
    m_testDoCollection.add("foo"); // already part of collection but collection can contain duplicates
    assertEquals(4, m_testDoCollection.size());
    m_testDoCollection.clear();
    assertEquals(0, m_testDoCollection.size());
  }

  @Test
  public void testIsEmpty() {
    assertFalse(m_testDoCollection.isEmpty());
    m_testDoCollection.clear();
    assertTrue(m_testDoCollection.isEmpty());
  }

  @Test
  public void testIterator() {
    Set<String> actual = new HashSet<>();
    Iterator<String> iter = m_testDoCollection.iterator();
    assertTrue(iter.hasNext());
    actual.add(iter.next());
    assertTrue(iter.hasNext());
    actual.add(iter.next());
    assertTrue(iter.hasNext());
    actual.add(iter.next());
    assertFalse(iter.hasNext());
    assertCollectionEquals(Arrays.asList("foo", "bar", "baz"), actual);
  }

  @Test
  public void testIterable() {
    List<String> actual = new ArrayList<>();
    for (String element : m_testDoCollection) {
      actual.add(element);
    }
    assertCollectionEquals(Arrays.asList("foo", "bar", "baz"), actual);
  }

  /**
   * Methods from {@link DoCollection} must not create the node if it's not necessary.
   */
  @Test
  public void testIdempotentMethodCalls() {
    DoCollection<String> collection = new DoCollection<>(null, m_lazyCreate, null);
    assertFalse(collection.exists());

    assertFalse(collection.contains("value"));
    assertFalse(collection.exists());

    // add method has a side effect

    collection.addAll((Collection<String>) null);
    assertFalse(collection.exists());

    collection.addAll((String[]) null);
    assertFalse(collection.exists());

    assertFalse(collection.remove("value"));
    assertFalse(collection.exists());

    assertFalse(collection.removeAll(Collections.singletonList("value")));
    assertFalse(collection.exists());

    assertFalse(collection.removeAll("value"));
    assertFalse(collection.exists());

    collection.updateAll((Collection<String>) null);
    assertFalse(collection.exists());

    collection.updateAll((String[]) null);
    assertFalse(collection.exists());

    collection.clear();
    assertFalse(collection.exists());

    assertEquals(0, collection.size());
    assertFalse(collection.exists());

    assertTrue(collection.isEmpty());
    assertFalse(collection.exists());

    assertEquals(0, collection.stream().count());
    assertFalse(collection.exists());

    assertFalse(collection.exists());
    assertFalse(collection.exists());

    assertEquals(0, collection.parallelStream().count());
    assertFalse(collection.exists());

    assertFalse(collection.iterator().hasNext());
    assertFalse(collection.exists());

    // findFirst(Function,VALUE) calls findFirst(Predicate)
    assertNull(collection.findFirst(x -> true));
    assertFalse(collection.exists());

    // find(Function,VALUE) calls find(Predicate)
    assertTrue(collection.find(x -> true).isEmpty());
    assertFalse(collection.exists());

    collection.valueHashCode();
    assertFalse(collection.exists());

    DoCollection<String> otherCollection = new DoCollection<>(null, m_lazyCreate, null);
    assertFalse(otherCollection.exists());
    assertTrue(collection.valueEquals(otherCollection));
    assertFalse(collection.exists());
    assertFalse(otherCollection.exists());
  }

  @Test
  public void testStream() {
    assertCollectionEquals(Arrays.asList("foo", "bar", "baz"), m_testDoCollection.stream().collect(Collectors.toSet()));
  }

  @Test
  public void testParallelStream() {
    assertCollectionEquals(Arrays.asList("foo", "bar", "baz"), m_testDoCollection.parallelStream().collect(Collectors.toSet()));
  }

  @Test
  public void testAttributeName() {
    assertNull(new DoCollection<>().getAttributeName());
    assertNull(DoCollection.of(Collections.emptyList()).getAttributeName());
    assertEquals("foo", new DoCollection<>("foo", null, null).getAttributeName());
  }

  @Test
  public void testEqualsHashCode() {
    DoCollection<String> collection1 = new DoCollection<>();
    collection1.add("foo");
    collection1.add("baz");

    DoCollection<String> collection2 = new DoCollection<>();
    collection2.add("baz");
    collection2.add("foo");

    assertEquals(collection1, collection2); // order of elements is not relevant for equality
    assertEquals(collection2, collection1);
    assertEquals(collection1, collection1);
    assertEquals(collection1.hashCode(), collection2.hashCode());

    collection1.add("bar");
    assertNotEquals(collection1, collection2);

    collection2.add("bar");
    assertEquals(collection1, collection2);
    assertEquals(collection1.hashCode(), collection2.hashCode());

    assertNotEquals(null, collection1);
    assertNotEquals(collection1, new Object());
  }

  @Test
  public void testCollectionBehavior() {
    DoCollection<String> set = new DoCollection<>();
    set.add("foo");
    set.add("bar");
    set.add("bar");
    set.add("baz");

    assertEquals(4, set.size());
    assertTrue(set.contains("foo"));
    assertTrue(set.contains("bar"));
    assertTrue(set.contains("baz"));

    set.remove("bar"); // remove first instance
    assertEquals(3, set.size());
    assertTrue(set.contains("foo"));
    assertTrue(set.contains("bar"));
    assertTrue(set.contains("baz"));

    set.remove("bar"); // remove second instance
    assertEquals(2, set.size());
    assertTrue(set.contains("foo"));
    assertTrue(set.contains("baz"));
  }

  protected <V> void assertCollectionEquals(Collection<V> expected, Collection<V> actual) {
    assertTrue("Collections are not equal. Expected: " + expected + ", actual: " + actual, CollectionUtility.equalsCollection(expected, actual, false));
  }
}
