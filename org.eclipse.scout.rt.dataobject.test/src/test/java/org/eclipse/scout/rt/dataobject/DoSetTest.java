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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Before;
import org.junit.Test;

/**
 * Various low-level tests for {@link DoSet}
 */
public class DoSetTest {

  protected DoSet<String> m_testDoSet;

  @Before
  public void before() {
    m_testDoSet = new DoSet<>();
    m_testDoSet.add("foo");
    m_testDoSet.add("bar");
    m_testDoSet.add("baz");
  }

  @Test
  public void testDoSetConstructor() {
    DoSet<String> set = new DoSet<>();
    assertTrue(set.exists());
  }

  @Test
  public void testDoSetDetailedConstructor() {
    Set<String> set = new HashSet<>();
    set.add("one");
    DoSet<String> doSet = new DoSet<>("attributeName", m_lazyCreate, set);
    assertFalse(doSet.exists());
    assertNotNull(doSet.get());
    assertTrue(doSet.get() instanceof HashSet);
    assertSame(set, doSet.get());
  }

  protected Consumer<DoNode<Set<String>>> m_lazyCreate = attribute -> {
    /* nop */
  };

  @Test
  public void testCreateExists() {
    DoSet<String> set = new DoSet<>(null, m_lazyCreate, null);
    assertFalse(set.exists());
    set.create();
    assertTrue(set.exists());

    set = new DoSet<>(null, m_lazyCreate, null);
    assertFalse(set.exists());
    set.set(CollectionUtility.hashSet("foo", "bar"));
    assertTrue(set.exists());

    set = new DoSet<>(null, m_lazyCreate, null);
    assertFalse(set.exists());
    set.get();
    assertTrue(set.exists());
  }

  @Test
  public void testOf() {
    DoSet<String> set = DoSet.of(CollectionUtility.hashSet("foo", "bar"));
    assertTrue(set.exists());
    assertTrue(set.contains("foo"));
    assertTrue(set.contains("bar"));
  }

  @Test
  public void testOfNull() {
    DoSet<String> set = DoSet.of(null);
    assertTrue(set.exists());
    assertTrue(set.isEmpty());
    assertEquals(new HashSet<>(), set.get());
  }

  @Test
  public void testGet() {
    assertEquals(CollectionUtility.hashSet("foo", "bar", "baz"), m_testDoSet.get());
  }

  @Test
  public void testSet() {
    m_testDoSet.set(CollectionUtility.hashSet("foo"));
    assertEquals(CollectionUtility.hashSet("foo"), m_testDoSet.get());

    m_testDoSet.set(Collections.emptySet());
    assertEquals(Collections.emptySet(), m_testDoSet.get());
    assertTrue(m_testDoSet.isEmpty());
    assertNotNull(m_testDoSet.get());

    m_testDoSet.set(null);
    assertEquals(Collections.emptySet(), m_testDoSet.get());
    assertTrue(m_testDoSet.isEmpty());
    assertNotNull(m_testDoSet.get());
  }

  @Test
  public void testContains() {
    assertTrue(m_testDoSet.contains("foo"));
    assertTrue(m_testDoSet.contains("bar"));
    assertTrue(m_testDoSet.contains("baz"));
    assertFalse(m_testDoSet.contains("no"));
  }

  @Test
  public void testAdd() {
    m_testDoSet.add("qux");
    assertTrue(m_testDoSet.contains("qux"));

    m_testDoSet.add(null);
    assertTrue(m_testDoSet.contains(null));
  }

  @Test
  public void testAddAllCollection() {
    Collection<String> collection = new LinkedHashSet<>();
    collection.add("qux");
    collection.add("quux");
    m_testDoSet.addAll(collection);
    assertTrue(m_testDoSet.contains("qux"));
    assertTrue(m_testDoSet.contains("quux"));
  }

  @Test
  public void testAddAllCollectionNull() {
    Collection<String> collection = null;
    m_testDoSet.addAll(collection);
    assertEquals(3, m_testDoSet.size());
  }

  @Test
  public void testAddAllArray() {
    m_testDoSet.addAll("qux", "quux");
    assertTrue(m_testDoSet.contains("qux"));
    assertTrue(m_testDoSet.contains("quux"));
  }

  @Test
  public void testAddAllArrayNull() {
    String[] strings = null;
    m_testDoSet.addAll(strings);
    assertEquals(3, m_testDoSet.size());
  }

  @Test
  public void testRemove() {
    assertTrue(m_testDoSet.remove("bar"));
    assertEquals(CollectionUtility.hashSet("foo", "baz"), m_testDoSet.get());

    assertTrue(m_testDoSet.remove("foo"));
    assertEquals(CollectionUtility.hashSet("baz"), m_testDoSet.get());

    assertFalse(m_testDoSet.remove("bar"));
    assertEquals(CollectionUtility.hashSet("baz"), m_testDoSet.get());

    assertFalse(m_testDoSet.remove("notExistingElement"));
    assertEquals(CollectionUtility.hashSet("baz"), m_testDoSet.get());

    assertFalse(m_testDoSet.remove(null));
    assertEquals(CollectionUtility.hashSet("baz"), m_testDoSet.get());

    assertTrue(m_testDoSet.remove("baz"));
    assertEquals(CollectionUtility.hashSet(), m_testDoSet.get());
  }

  @Test
  public void testRemoveIntList() {
    DoSet<Integer> intSet = new DoSet<>();
    intSet.add(1);
    intSet.add(2);
    intSet.add(3);
    intSet.remove(1); // by value
    assertEquals(CollectionUtility.hashSet(2, 3), intSet.get());
    intSet.remove(Integer.valueOf(3)); // by value
    assertEquals(CollectionUtility.hashSet(2), intSet.get());
    intSet.remove(Integer.valueOf(2)); // by value
    assertTrue(intSet.isEmpty());
  }

  @Test
  public void testRemoveAllCollection() {
    assertTrue(m_testDoSet.removeAll(CollectionUtility.hashSet("bar")));
    assertEquals(CollectionUtility.hashSet("foo", "baz"), m_testDoSet.get());

    assertTrue(m_testDoSet.removeAll(CollectionUtility.hashSet("baz", "foo")));
    assertEquals(CollectionUtility.hashSet(), m_testDoSet.get());

    assertFalse(m_testDoSet.removeAll(CollectionUtility.hashSet("abc", "def")));
    assertEquals(CollectionUtility.hashSet(), m_testDoSet.get());
  }

  @Test
  public void testRemoveAllWildcardCollection() {
    // text removeAll using "Collection<? extends String>"
    m_testDoSet.clear();
    m_testDoSet.add("foo");
    assertEquals(CollectionUtility.hashSet("foo"), m_testDoSet.get());
    Collection<? extends String> elementsToRemove = new ArrayList<>(CollectionUtility.hashSet("foo"));
    m_testDoSet.removeAll(elementsToRemove);
    assertEquals(CollectionUtility.hashSet(), m_testDoSet.get());
  }

  @Test
  public void testRemoveAllCollectionNull() {
    Collection<String> collection = null;
    assertFalse(m_testDoSet.removeAll(collection));
    assertEquals(3, m_testDoSet.size());
  }

  @Test
  public void testRemoveAllArray() {
    assertTrue(m_testDoSet.removeAll("bar"));
    assertEquals(CollectionUtility.hashSet("foo", "baz"), m_testDoSet.get());

    assertTrue(m_testDoSet.removeAll("baz", "foo"));
    assertEquals(CollectionUtility.hashSet(), m_testDoSet.get());

    assertFalse(m_testDoSet.removeAll("abc", "def"));
    assertEquals(CollectionUtility.hashSet(), m_testDoSet.get());
  }

  @Test
  public void testRemoveAllArrayNull() {
    String[] strings = null;
    assertFalse(m_testDoSet.removeAll(strings));
    assertEquals(3, m_testDoSet.size());
  }

  @Test
  public void testUpdateAllCollection() {
    m_testDoSet.updateAll(CollectionUtility.hashSet("a", "b"));
    assertEquals(CollectionUtility.hashSet("a", "b"), m_testDoSet.get());

    m_testDoSet.updateAll(Collections.emptyList());
    assertEquals(CollectionUtility.hashSet(), m_testDoSet.get());
    assertTrue(m_testDoSet.isEmpty());

    m_testDoSet.updateAll(CollectionUtility.hashSet("a", "b"));
    m_testDoSet.updateAll((Collection<String>) null);
    assertEquals(CollectionUtility.hashSet(), m_testDoSet.get());
    assertTrue(m_testDoSet.isEmpty());

    Collection<? extends String> newValues = new ArrayList<>(CollectionUtility.hashSet("foo"));
    m_testDoSet.updateAll(newValues);
    assertEquals(CollectionUtility.hashSet("foo"), m_testDoSet.get());
  }

  @Test
  public void testUpdateAllArray() {
    m_testDoSet.updateAll("a", "b");
    assertEquals(CollectionUtility.hashSet("a", "b"), m_testDoSet.get());

    m_testDoSet.updateAll(Collections.emptyList());
    assertEquals(CollectionUtility.hashSet(), m_testDoSet.get());
    assertTrue(m_testDoSet.isEmpty());

    m_testDoSet.updateAll("a", "b");
    m_testDoSet.updateAll((String[]) null);
    assertEquals(CollectionUtility.hashSet(), m_testDoSet.get());
    assertTrue(m_testDoSet.isEmpty());
  }

  @Test
  public void testClear() {
    assertEquals(3, m_testDoSet.size());
    m_testDoSet.clear();
    assertEquals(0, m_testDoSet.size());
    m_testDoSet.clear();
    assertEquals(0, m_testDoSet.size());
  }

  @Test
  public void testSize() {
    assertEquals(3, m_testDoSet.size());
    m_testDoSet.add("foo"); // already part of set -> same size
    assertEquals(3, m_testDoSet.size());
    m_testDoSet.clear();
    assertEquals(0, m_testDoSet.size());
  }

  @Test
  public void testIsEmpty() {
    assertFalse(m_testDoSet.isEmpty());
    m_testDoSet.clear();
    assertTrue(m_testDoSet.isEmpty());
  }

  @Test
  public void testIterator() {
    Set<String> actual = new HashSet<>();
    Iterator<String> iter = m_testDoSet.iterator();
    assertTrue(iter.hasNext());
    actual.add(iter.next());
    assertTrue(iter.hasNext());
    actual.add(iter.next());
    assertTrue(iter.hasNext());
    actual.add(iter.next());
    assertFalse(iter.hasNext());
    assertEquals(CollectionUtility.hashSet("foo", "bar", "baz"), actual);
  }

  @Test
  public void testIterable() {
    Set<String> actual = new HashSet<>();
    for (String element : m_testDoSet) {
      actual.add(element);
    }
    assertEquals(CollectionUtility.hashSet("foo", "bar", "baz"), actual);
  }

  @Test
  public void testStream() {
    assertEquals(CollectionUtility.hashSet("foo", "bar", "baz"), m_testDoSet.stream().collect(Collectors.toSet()));
  }

  @Test
  public void testParallelStream() {
    assertEquals(CollectionUtility.hashSet("foo", "bar", "baz"), m_testDoSet.parallelStream().collect(Collectors.toSet()));
  }

  /**
   * Methods from {@link DoSet} must not create the node if it's not necessary.
   */
  @Test
  public void testIdempotentMethodCalls() {
    DoSet<String> set = new DoSet<>(null, m_lazyCreate, null);
    assertFalse(set.exists());

    assertNotNull(set.toString());
    assertFalse(set.exists());
  }

  @Test
  public void testAttributeName() {
    assertNull(new DoSet<>().getAttributeName());
    assertNull(DoSet.of(Collections.emptySet()).getAttributeName());
    assertEquals("foo", new DoSet<>("foo", null, null).getAttributeName());
  }

  @Test
  public void testEqualsHashCode() {
    DoSet<String> set1 = new DoSet<>();
    set1.add("foo");
    set1.add("baz");

    DoSet<String> set2 = new DoSet<>();
    set2.add("baz");
    set2.add("foo");

    assertEquals(set1, set2); // order of elements is not relevant for equality
    assertEquals(set2, set1);
    assertEquals(set1, set1);
    assertEquals(set1.hashCode(), set2.hashCode());

    set1.add("bar");
    assertNotEquals(set1, set2);

    set2.add("bar");
    assertEquals(set1, set2);
    assertEquals(set1.hashCode(), set2.hashCode());

    assertNotEquals(null, set1);
    assertNotEquals(set1, new Object());
  }

  @Test
  public void testSetBehavior() {
    DoSet<String> set = new DoSet<>();
    set.add("foo");
    set.add("bar");
    set.add("bar");
    set.add("baz");

    assertEquals(3, set.size());
    assertTrue(set.contains("foo"));
    assertTrue(set.contains("bar"));
    assertTrue(set.contains("baz"));

    set.remove("bar"); // remove unique instance
    assertEquals(2, set.size());
    assertTrue(set.contains("foo"));
    assertFalse(set.contains("bar"));
    assertTrue(set.contains("baz"));
  }
}
