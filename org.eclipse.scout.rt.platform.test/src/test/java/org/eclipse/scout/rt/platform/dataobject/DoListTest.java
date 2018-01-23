package org.eclipse.scout.rt.platform.dataobject;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
  }

  protected Consumer<DoNode<List<String>>> m_lazyCreate = attribute -> {
    /* nop */ };

  @Test
  public void testCreateExists() {
    DoList<String> list = new DoList<>(m_lazyCreate);
    assertFalse(list.exists());
    list.create();
    assertTrue(list.exists());

    list = new DoList<>(m_lazyCreate);
    assertFalse(list.exists());
    list.set(Arrays.asList("foo", "bar"));
    assertTrue(list.exists());

    list = new DoList<>(m_lazyCreate);
    assertFalse(list.exists());
    list.get();
    assertTrue(list.exists());
  }

  @Test
  public void testGet() {
    assertEquals(Arrays.asList("foo", "bar", "baz"), m_testDoList.get());
  }

  @Test
  public void testSet() {
    m_testDoList.set(Arrays.asList("foo"));
    assertEquals(Arrays.asList("foo"), m_testDoList.get());

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
  public void testAddAllArray() {
    m_testDoList.addAll("qux", "quux");
    assertEquals("qux", m_testDoList.get(3));
    assertEquals("quux", m_testDoList.get(4));
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
    intList.remove(1);
    assertEquals(Arrays.asList(1, 3), intList.get());
    intList.remove(new Integer(3));
    assertEquals(Arrays.asList(1), intList.get());
    intList.remove(Integer.valueOf(1));
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
  public void testRemoveAllArray() {
    assertTrue(m_testDoList.removeAll("bar"));
    assertEquals(Arrays.asList("foo", "baz"), m_testDoList.get());

    assertTrue(m_testDoList.removeAll("baz", "foo"));
    assertEquals(Arrays.asList(), m_testDoList.get());

    assertFalse(m_testDoList.removeAll("abc", "def"));
    assertEquals(Arrays.asList(), m_testDoList.get());
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
    CollectionUtility.equalsCollection(Arrays.asList("foo", "bar", "baz"), m_testDoList.parallelStream().collect(Collectors.toSet()), false);
  }

  @Test
  public void testSort() {
    m_testDoList.sort((left, right) -> left.compareTo(right));
    assertEquals(Arrays.asList("bar", "baz", "foo"), m_testDoList.get());

    m_testDoList.sort((left, right) -> right.compareTo(left));
    assertEquals(Arrays.asList("foo", "baz", "bar"), m_testDoList.get());
  }

  protected Function<String, DoValue<String>> listValueAccessor = new Function<String, DoValue<String>>() {
    @Override
    public DoValue<String> apply(String input) {
      for (String item : m_testDoList.get()) {
        if (item.equals(input)) {
          DoValue<String> itemWrapped = new DoValue<>();
          itemWrapped.set(input);
          return itemWrapped;
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
    assertEquals("bar", m_testDoList.findFirst((input) -> input.equals("bar")));
    assertEquals(null, m_testDoList.findFirst((input) -> input.equals("myCustomSearchTerm")));
  }

  @Test
  public void testFind() {
    assertEquals(Arrays.asList("bar"), m_testDoList.find((input) -> input.equals("bar")));
    assertEquals(Arrays.asList("bar", "baz"), m_testDoList.find((input) -> input.equals("bar") || input.equals("baz")));
  }
}
