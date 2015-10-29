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
package org.eclipse.scout.commons.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * @since 4.2
 */
public class OrderedCollectionTest {

  private static final double MAX_ACCEPTABLE_DELTA = 0.0000001;
  private OrderedCollection<IOrdered> c;
  private IOrdered m_ordered10;
  private IOrdered m_ordered20;
  private IOrdered m_ordered30;

  @Before
  public void before() {
    c = new OrderedCollection<IOrdered>();
    m_ordered10 = new Ordered(10);
    m_ordered20 = new OtherOrdered(20);
    m_ordered30 = new Ordered(30);
  }

  @Test
  public void testSize() {
    assertEquals(0, c.size());
    c.addOrdered(m_ordered10);
    assertEquals(1, c.size());
    c.addAllOrdered(Arrays.asList(m_ordered20, m_ordered30));
    assertEquals(3, c.size());
  }

  @Test
  public void testIsEmpty() {
    assertTrue(c.isEmpty());
    c.addOrdered(m_ordered10);
    assertFalse(c.isEmpty());
    c.addAllOrdered(Arrays.asList(m_ordered20, m_ordered30));
    assertFalse(c.isEmpty());
    c.remove(m_ordered30);
    assertFalse(c.isEmpty());
    c.removeAll(Arrays.asList(m_ordered20, m_ordered10));
    assertTrue(c.isEmpty());
  }

  @Test
  public void testAddOrdered() {
    assertFalse(c.addOrdered(null));
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    assertTrue(c.addOrdered(m_ordered30));
  }

  @Test
  public void testAddOrderedWithOrder() {
    assertFalse(c.addOrdered(null, 23));
    assertTrue(c.addOrdered(m_ordered10, 15));
    assertEquals(15, m_ordered10.getOrder(), 0);
    Ordered o = new Ordered();
    assertTrue(c.addOrdered(o, 18));
    assertEquals(18, o.getOrder(), 0);
  }

  @Test
  public void testAddOrderedWithOrderSameObject() {
    Ordered o = new Ordered();
    assertTrue(c.addOrdered(o, 18));
    assertEquals(18, o.getOrder(), 0);
    c.addOrdered(o, 25);
    assertEquals(Arrays.asList(o, o), c.getOrderedList());
  }

  @Test
  public void testAddOrderedTheSameInstanceTwice() {
    assertTrue(c.addOrdered(m_ordered10));
    c.addOrdered(m_ordered10);
    assertEquals(Arrays.asList(m_ordered10, m_ordered10), c.getOrderedList());
  }

  @Test
  public void testAddOrderedTwoInstancesOfSameClassWithSameOrder() {
    Ordered o1 = new Ordered(10d);
    assertTrue(c.addOrdered(m_ordered10));
    c.addOrdered(o1);
    assertEquals(Arrays.asList(m_ordered10, o1), c.getOrderedList());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddAtNegativeIndex() {
    c.addAt(new Ordered(), -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddAtIndexOutOfBoundsEmptyCollection() {
    c.addAt(new Ordered(), 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddAtIndexOutOfBounds() {
    try {
      c.addOrdered(m_ordered10);
    }
    catch (IllegalArgumentException e) {
      fail("expected to work");
    }
    c.addAt(new Ordered(), 2);
  }

  @Test
  public void testAddAtNull() {
    assertFalse(c.addAt(null, 0));
  }

  @Test
  public void testAddAtFirst() {
    c.addOrdered(m_ordered10);
    Ordered o = new Ordered();
    assertTrue(c.addAt(o, 0));
    assertEquals(-990, o.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAtLast() {
    c.addOrdered(m_ordered10);
    Ordered o = new Ordered();
    assertTrue(c.addAt(o, 1));
    assertEquals(1010, o.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAtBetweenWithTwoElements() {
    c.addOrdered(m_ordered10);
    c.addOrdered(m_ordered20);
    Ordered o = new Ordered();
    assertTrue(c.addAt(o, 1));
    assertEquals(15, o.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAtBetweenWithTrheeElements() {
    c.addOrdered(m_ordered10);
    c.addOrdered(m_ordered20);
    c.addOrdered(m_ordered30);
    Ordered o = new Ordered();
    assertTrue(c.addAt(o, 2));
    assertEquals(25, o.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddAllAtNegativeIndex() {
    c.addAllAt(Collections.singleton(new Ordered()), -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddAllAtIndexOutOfBoundsEmptyCollection() {
    c.addAllAt(Collections.singleton(new Ordered()), 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddAllAtIndexOutOfBounds() {
    try {
      c.addOrdered(m_ordered10);
    }
    catch (IllegalArgumentException t) {
      fail("expected to work");
    }
    c.addAllAt(Collections.singleton(new Ordered()), 2);
  }

  @Test
  public void testAddAllAtNull() {
    assertFalse(c.addAllAt(null, 0));
  }

  @Test
  public void testAddAllAtFirst() {
    c.addOrdered(m_ordered10);
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    assertTrue(c.addAllAt(Arrays.asList(o1, o2), 0));
    assertEquals(-1990, o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(-990, o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllAtLast() {
    c.addOrdered(m_ordered10);
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    assertTrue(c.addAllAt(Arrays.asList(o1, o2), 1));
    assertEquals(1010, o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(2010, o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllAtBetweenWithTwoElements() {
    c.addOrdered(m_ordered10);
    c.addOrdered(m_ordered20);
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    assertTrue(c.addAllAt(Arrays.asList(o1, o2), 1));
    assertEquals(10d + 1d * (10d / 3d), o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(10d + 2d * (10d / 3d), o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllAtBetweenWithTrheeElements() {
    c.addOrdered(m_ordered10);
    c.addOrdered(m_ordered20);
    c.addOrdered(m_ordered30);
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    assertTrue(c.addAllAt(Arrays.asList(o1, o2), 2));
    assertEquals(20d + 1d * (10d / 3d), o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(20d + 2d * (10d / 3d), o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllOrdered() {
    assertFalse(c.addAllOrdered(null));
    assertFalse(c.addAllOrdered(Collections.<IOrdered> emptyList()));
    assertFalse(c.addAllOrdered(Collections.<IOrdered> singleton(null)));
    assertTrue(c.addAllOrdered(Collections.singletonList(m_ordered10)));
    assertTrue(c.addAllOrdered(Arrays.asList(m_ordered20, m_ordered30)));
  }

  @Test
  public void testAddAllOrderedTheSameInstanceTwice() {
    List<IOrdered> list = Arrays.asList(m_ordered10, m_ordered10);
    c.addAllOrdered(list);
    assertEquals(list, c.getOrderedList());
  }

  @Test
  public void testAddAllOrderedTwoInstancesOfSameClassWithSameOrder() {
    List<IOrdered> list = Arrays.asList(m_ordered10, new Ordered(10d));
    c.addAllOrdered(list);
    assertEquals(list, c.getOrderedList());
  }

  @Test
  public void testRemoveObject() {
    assertFalse(c.remove(null));
    assertTrue(c.addOrdered(m_ordered10));
    assertFalse(c.remove(null));
    assertFalse(c.remove(m_ordered20));
    assertTrue(c.remove(m_ordered10));
    assertTrue(c.isEmpty());
  }

  @Test
  public void testRemoveObjectOrderChanged() {
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    assertTrue(c.addOrdered(m_ordered30));
    m_ordered10.setOrder(45);
    assertTrue(c.remove(m_ordered10));
    assertEquals(2, c.size());
  }

  @Test
  public void testRemoveAll() {
    assertFalse(c.removeAll(null));
    assertFalse(c.removeAll(Collections.emptyList()));
    assertFalse(c.removeAll(Collections.singletonList(null)));
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    assertTrue(c.addOrdered(m_ordered30));
    assertTrue(c.removeAll(Arrays.asList(null, m_ordered20)));
    assertEquals(2, c.size());
    assertTrue(c.removeAll(Arrays.asList(m_ordered30, m_ordered10)));
    assertTrue(c.isEmpty());
  }

  @Test
  public void testRemoveAllOrderChangedOneElement() {
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    assertTrue(c.addOrdered(m_ordered30));
    m_ordered10.setOrder(45);
    assertTrue(c.removeAll(Collections.singleton(m_ordered10)));
    assertEquals(2, c.size());
  }

  @Test
  public void testRemoveAllOrderChangedTwoElementsExcludingTheOneWithChangedOrder() {
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    assertTrue(c.addOrdered(m_ordered30));
    m_ordered10.setOrder(45);
    assertTrue(c.removeAll(Arrays.asList(m_ordered30, m_ordered20)));
    assertEquals(1, c.size());
    assertEquals(Collections.singletonList(m_ordered10), c.getOrderedList());
  }

  @Test
  public void testRemoveAllOrderChangedTwoElementsIncludingTheOneWithChangedOrder() {
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    assertTrue(c.addOrdered(m_ordered30));
    m_ordered10.setOrder(45);
    assertTrue(c.removeAll(Arrays.asList(m_ordered10, m_ordered20)));
    assertEquals(1, c.size());
    assertEquals(Collections.singletonList(m_ordered30), c.getOrderedList());
  }

  @Test
  public void testGetOrderedEmpty() {
    assertEquals(Collections.<IOrdered> emptyList(), c.getOrderedList());
  }

  @Test
  public void testGetOrdered() {
    assertTrue(c.addOrdered(m_ordered30));
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    assertEquals(Arrays.asList(m_ordered10, m_ordered20, m_ordered30), c.getOrderedList());
  }

  @Test
  public void testGetOrderedOrderChanged() {
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    assertTrue(c.addOrdered(m_ordered30));
    m_ordered10.setOrder(45);
    assertEquals(Arrays.asList(m_ordered20, m_ordered30, m_ordered10), c.getOrderedList());
  }

  @Test
  public void testClear() {
    assertTrue(c.isEmpty());
    c.clear();
    assertTrue(c.isEmpty());
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    assertTrue(c.addOrdered(m_ordered30));
    assertFalse(c.isEmpty());
    c.clear();
    assertTrue(c.isEmpty());
    c.clear();
    assertTrue(c.isEmpty());
  }

  @Test
  public void testAddFirst() {
    assertFalse(c.addFirst(null));
    assertTrue(c.addOrdered(m_ordered10));
    Ordered o = new Ordered();
    assertTrue(c.addFirst(o));
    assertEquals(-990, o.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertTrue(c.addFirst(m_ordered20));
    assertEquals(-1990, m_ordered20.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddFirstInEmptyCollection() {
    Ordered o = new Ordered();
    assertTrue(c.addFirst(o));
    assertEquals(OrderedCollection.DEFAULT_EMPTY_ORDER, o.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddLast() {
    assertFalse(c.addLast(null));
    assertTrue(c.addOrdered(m_ordered10));
    Ordered o = new Ordered();
    assertTrue(c.addLast(o));
    assertEquals(1010, o.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertTrue(c.addLast(m_ordered20));
    assertEquals(2010, m_ordered20.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddLastInEmptyCollection() {
    Ordered o = new Ordered();
    assertTrue(c.addLast(o));
    assertEquals(OrderedCollection.DEFAULT_EMPTY_ORDER, o.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddBeforeNull() {
    assertFalse(c.addBefore((IOrdered) null, (IOrdered) null));
    assertFalse(c.addBefore((IOrdered) null, m_ordered10));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddBeforeNullReferenceObject() {
    c.addBefore(m_ordered10, (IOrdered) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddBeforeForeignReferenceObject() {
    c.addBefore(m_ordered10, m_ordered20);
  }

  @Test
  public void testAddBeforeBetweenTwoElements() {
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o = new Ordered();
    c.addBefore(o, m_ordered20);
    assertEquals(15, o.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddBeforeBetweenTwoElementsWithSameOrder() {
    assertTrue(c.addOrdered(m_ordered10));
    Ordered o1 = new Ordered(10);
    assertTrue(c.addOrdered(o1));
    Ordered o = new Ordered();
    c.addBefore(o, o1);
    assertEquals(10, o.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(Arrays.asList(m_ordered10, o, o1), c.getOrderedList());
  }

  @Test
  public void testAddBeforeBeforeFirstElement() {
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o = new Ordered();
    c.addBefore(o, m_ordered20);
    assertEquals(-980, o.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddBeforeClassNull() {
    assertFalse(c.addBefore((IOrdered) null, (Class<? extends IOrdered>) null));
    assertFalse(c.addBefore((IOrdered) null, Ordered.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddBeforeClassNullReferenceObject() {
    c.addBefore(m_ordered10, (Class<? extends IOrdered>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddBeforeClassForeignReferenceObject() {
    c.addBefore(m_ordered10, OtherOrdered.class);
  }

  @Test
  public void testAddBeforeClassBetweenTwoElements() {
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o = new Ordered();
    c.addBefore(o, OtherOrdered.class);
    assertEquals(15, o.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddBeforeClassBeforeFirstElement() {
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o = new Ordered();
    c.addBefore(o, OtherOrdered.class);
    assertEquals(-980, o.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAfterClassNull() {
    assertFalse(c.addAfter((IOrdered) null, (Class<? extends IOrdered>) null));
    assertFalse(c.addAfter((IOrdered) null, Ordered.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddAfterClassNullReferenceObject() {
    c.addAfter(m_ordered10, (Class<? extends IOrdered>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddAfterClassForeignReferenceObject() {
    c.addAfter(m_ordered10, OtherOrdered.class);
  }

  @Test
  public void testAddAfterClassBetweenTwoElements() {
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o = new Ordered();
    c.addAfter(o, Ordered.class);
    assertEquals(15, o.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAfterClassAfterLastElement() {
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o = new Ordered();
    c.addAfter(o, OtherOrdered.class);
    assertEquals(1020, o.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAfterNull() {
    assertFalse(c.addAfter((IOrdered) null, (IOrdered) null));
    assertFalse(c.addAfter((IOrdered) null, m_ordered10));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddAfterNullReferenceObject() {
    c.addAfter(m_ordered10, (IOrdered) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddAfterForeignReferenceObject() {
    c.addAfter(m_ordered10, m_ordered20);
  }

  @Test
  public void testAddAfterBetweenTwoElements() {
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o = new Ordered();
    c.addAfter(o, m_ordered10);
    assertEquals(15, o.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAfterAfterLastElement() {
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o = new Ordered();
    c.addAfter(o, m_ordered20);
    assertEquals(1020, o.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllFirstNullOrEmptyOrNullElements() {
    assertFalse(c.addAllFirst(null));
    assertFalse(c.addAllFirst(Collections.<IOrdered> emptyList()));
    assertFalse(c.addAllFirst(Collections.<IOrdered> singleton(null)));
    assertFalse(c.addAllFirst(Arrays.<IOrdered> asList(null, null)));
  }

  @Test
  public void testAddAllFirst() {
    assertTrue(c.addOrdered(m_ordered10));
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    assertTrue(c.addAllFirst(Arrays.asList(o1, o2)));
    assertEquals(-1990, o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(-990, o2.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertTrue(c.addAllFirst(Collections.singleton(m_ordered20)));
    assertEquals(-2990, m_ordered20.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllFirstInEmptyCollection() {
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    assertTrue(c.addAllFirst(Arrays.asList(o1, o2)));
    assertEquals(OrderedCollection.DEFAULT_EMPTY_ORDER - IOrdered.DEFAULT_ORDER_STEP, o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(OrderedCollection.DEFAULT_EMPTY_ORDER, o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllLastNullOrEmptyOrNullElements() {
    assertFalse(c.addAllLast(null));
    assertFalse(c.addAllLast(Collections.<IOrdered> emptyList()));
    assertFalse(c.addAllLast(Collections.<IOrdered> singleton(null)));
    assertFalse(c.addAllLast(Arrays.<IOrdered> asList(null, null)));
  }

  @Test
  public void testAddAllLastFirst() {
    assertTrue(c.addOrdered(m_ordered10));
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    assertTrue(c.addAllLast(Arrays.asList(o1, o2)));
    assertEquals(1010, o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(2010, o2.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertTrue(c.addAllLast(Collections.singleton(m_ordered20)));
    assertEquals(3010, m_ordered20.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllLastInEmptyCollection() {
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    assertTrue(c.addAllLast(Arrays.asList(o1, o2)));
    assertEquals(OrderedCollection.DEFAULT_EMPTY_ORDER, o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(OrderedCollection.DEFAULT_EMPTY_ORDER + IOrdered.DEFAULT_ORDER_STEP, o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllBeforeNullOrEmptyOrNullElements() {
    // null
    assertFalse(c.addAllBefore((Collection<IOrdered>) null, (IOrdered) null));
    assertFalse(c.addAllBefore((Collection<IOrdered>) null, m_ordered10));
    // empty
    assertFalse(c.addAllBefore(Collections.<IOrdered> emptyList(), (IOrdered) null));
    assertFalse(c.addAllBefore(Collections.<IOrdered> emptyList(), m_ordered10));
    // null elements
    assertFalse(c.addAllBefore(Collections.<IOrdered> singleton(null), (IOrdered) null));
    assertFalse(c.addAllBefore(Collections.<IOrdered> singleton(null), m_ordered10));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddAllBeforeNullReferenceObject() {
    c.addAllBefore(Collections.singleton(m_ordered10), (IOrdered) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddAllBeforeForeignReferenceObject() {
    c.addAllBefore(Collections.singleton(m_ordered10), m_ordered20);
  }

  @Test
  public void testAddAllBeforeBetweenTwoElements() {
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    c.addAllBefore(Arrays.asList(o1, o2), m_ordered20);
    assertEquals(10d + 1 * (10d / 3d), o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(10d + 2 * (10d / 3d), o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllBeforeBetweenTwoElementsContainsNullELement() {
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    c.addAllBefore(Arrays.asList(o1, null, o2), m_ordered20);
    assertEquals(10d + 1 * (10d / 3d), o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(10d + 2 * (10d / 3d), o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllBeforeBeforeFirstElement() {
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    c.addAllBefore(Arrays.asList(o1, o2), m_ordered20);
    assertEquals(-1980, o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(-980, o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllBeforeBeforeFirstElementContainsNullElement() {
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    c.addAllBefore(Arrays.asList(o1, null, o2), m_ordered20);
    assertEquals(-1980, o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(-980, o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllAfterNullOrEmptyOrNullElements() {
    // null
    assertFalse(c.addAllAfter((Collection<IOrdered>) null, (IOrdered) null));
    assertFalse(c.addAllAfter((Collection<IOrdered>) null, m_ordered10));
    // empty
    assertFalse(c.addAllAfter(Collections.<IOrdered> emptyList(), (IOrdered) null));
    assertFalse(c.addAllAfter(Collections.<IOrdered> emptyList(), m_ordered10));
    // null elements
    assertFalse(c.addAllAfter(Collections.<IOrdered> singleton(null), (IOrdered) null));
    assertFalse(c.addAllAfter(Collections.<IOrdered> singleton(null), m_ordered10));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddAllAfterNullReferenceObject() {
    c.addAllAfter(Collections.singleton(m_ordered10), (IOrdered) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddAllAfterForeignReferenceObject() {
    c.addAllAfter(Collections.singleton(m_ordered10), m_ordered20);
  }

  @Test
  public void testAddAllAfterBetweenTwoElements() {
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    c.addAllAfter(Arrays.asList(o1, o2), m_ordered10);
    assertEquals(10d + 1 * (10d / 3d), o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(10d + 2 * (10d / 3d), o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllAfterBetweenTwoElementsContainsNullELement() {
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    c.addAllAfter(Arrays.asList(o1, null, o2), m_ordered10);
    assertEquals(10d + 1 * (10d / 3d), o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(10d + 2 * (10d / 3d), o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllAfterAfterLastElement() {
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    c.addAllAfter(Arrays.asList(o1, o2), m_ordered20);
    assertEquals(1020, o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(2020, o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllAfterAfterLastElementContainsNullElement() {
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    c.addAllAfter(Arrays.asList(o1, null, o2), m_ordered20);
    assertEquals(1020, o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(2020, o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllBeforeClassNullOrEmptyOrNullElements() {
    // null
    assertFalse(c.addAllBefore((Collection<IOrdered>) null, (Class<? extends IOrdered>) null));
    assertFalse(c.addAllBefore((Collection<IOrdered>) null, Ordered.class));
    // empty
    assertFalse(c.addAllBefore(Collections.<IOrdered> emptyList(), (Class<? extends IOrdered>) null));
    assertFalse(c.addAllBefore(Collections.<IOrdered> emptyList(), Ordered.class));
    // null elements
    assertFalse(c.addAllBefore(Collections.<IOrdered> singleton(null), (Class<? extends IOrdered>) null));
    assertFalse(c.addAllBefore(Collections.<IOrdered> singleton(null), Ordered.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddAllBeforeClassNullReferenceObject() {
    c.addAllBefore(Collections.singleton(m_ordered10), (Class<? extends IOrdered>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddAllBeforeClassForeignReferenceObject() {
    c.addAllBefore(Collections.singleton(m_ordered10), OtherOrdered.class);
  }

  @Test
  public void testAddAllBeforeClassBetweenTwoElements() {
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    c.addAllBefore(Arrays.asList(o1, o2), OtherOrdered.class);
    assertEquals(10d + 1 * (10d / 3d), o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(10d + 2 * (10d / 3d), o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllBeforeClassBetweenTwoElementsContainsNullELement() {
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    c.addAllBefore(Arrays.asList(o1, null, o2), OtherOrdered.class);
    assertEquals(10d + 1 * (10d / 3d), o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(10d + 2 * (10d / 3d), o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllBeforeClassBeforeFirstElement() {
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    c.addAllBefore(Arrays.asList(o1, o2), OtherOrdered.class);
    assertEquals(-1980, o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(-980, o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllBeforeClassBeforeFirstElementContainsNullElement() {
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    c.addAllBefore(Arrays.asList(o1, null, o2), OtherOrdered.class);
    assertEquals(-1980, o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(-980, o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllAfterClassNullOrEmptyOrNullElements() {
    // null
    assertFalse(c.addAllAfter((Collection<IOrdered>) null, (Class<? extends IOrdered>) null));
    assertFalse(c.addAllAfter((Collection<IOrdered>) null, Ordered.class));
    // empty
    assertFalse(c.addAllAfter(Collections.<IOrdered> emptyList(), (Class<? extends IOrdered>) null));
    assertFalse(c.addAllAfter(Collections.<IOrdered> emptyList(), Ordered.class));
    // null elements
    assertFalse(c.addAllAfter(Collections.<IOrdered> singleton(null), (Class<? extends IOrdered>) null));
    assertFalse(c.addAllAfter(Collections.<IOrdered> singleton(null), Ordered.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddAllAfterClassNullReferenceObject() {
    c.addAllAfter(Collections.singleton(m_ordered10), (Class<? extends IOrdered>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddAllAfterClassForeignReferenceObject() {
    c.addAllAfter(Collections.singleton(m_ordered10), OtherOrdered.class);
  }

  @Test
  public void testAddAllAfterClassBetweenTwoElements() {
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    c.addAllAfter(Arrays.asList(o1, o2), Ordered.class);
    assertEquals(10d + 1 * (10d / 3d), o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(10d + 2 * (10d / 3d), o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllAfterClassBetweenTwoElementsContainsNullELement() {
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    c.addAllAfter(Arrays.asList(o1, null, o2), Ordered.class);
    assertEquals(10d + 1 * (10d / 3d), o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(10d + 2 * (10d / 3d), o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllAfterClassAfterLastElement() {
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    c.addAllAfter(Arrays.asList(o1, o2), OtherOrdered.class);
    assertEquals(1020, o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(2020, o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testAddAllAfterClassAfterLastElementContainsNullElement() {
    assertTrue(c.addOrdered(m_ordered20));
    Ordered o1 = new Ordered();
    Ordered o2 = new Ordered();
    c.addAllAfter(Arrays.asList(o1, null, o2), OtherOrdered.class);
    assertEquals(1020, o1.getOrder(), MAX_ACCEPTABLE_DELTA);
    assertEquals(2020, o2.getOrder(), MAX_ACCEPTABLE_DELTA);
  }

  @Test
  public void testMoveOrdered() {
    assertTrue(c.isEmpty());
    // adding a new element
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    m_ordered10.setOrder(30);
    assertEquals(Arrays.asList(m_ordered20, m_ordered10), c.getOrderedList());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRemoveAtNegativeIndex() {
    c.removeAt(-1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRemoveAtIndexOutOfBounds() {
    try {
      assertTrue(c.addOrdered(m_ordered10));
    }
    catch (Exception e) {
      fail("expected to work");
    }
    c.removeAt(1);
  }

  @Test
  public void testRemoveAtFirst() {
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    c.removeAt(0);
    assertEquals(Collections.singletonList(m_ordered20), c.getOrderedList());
  }

  @Test
  public void testRemoveAtLast() {
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    c.removeAt(1);
    assertEquals(Collections.singletonList(m_ordered10), c.getOrderedList());
  }

  @Test
  public void testRemoveAtInBetween() {
    assertTrue(c.addOrdered(m_ordered10));
    assertTrue(c.addOrdered(m_ordered20));
    assertTrue(c.addOrdered(m_ordered30));
    c.removeAt(1);
    assertEquals(Arrays.asList(m_ordered10, m_ordered30), c.getOrderedList());
  }

  @Test
  public void testDefaultOrderValue() {
    double d = IOrdered.DEFAULT_ORDER;
    double prev = d;
    int count = 0;
    while (count < 100000) {
      d = d - IOrdered.DEFAULT_ORDER_STEP;
      assertFalse(d + " - " + IOrdered.DEFAULT_ORDER_STEP + " results in the same value!", d == prev);
      prev = d;
      count++;
    }
  }

  public static abstract class AbstractOrdered implements IOrdered {
    private double m_order;

    public AbstractOrdered() {
    }

    public AbstractOrdered(double order) {
      m_order = order;
    }

    @Override
    public double getOrder() {
      return m_order;
    }

    @Override
    public void setOrder(double order) {
      m_order = order;
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "(" + m_order + ")";
    }
  }

  public static class Ordered extends AbstractOrdered {

    public Ordered() {
      super();
    }

    public Ordered(double order) {
      super(order);
    }
  }

  public static class OtherOrdered extends AbstractOrdered {

    public OtherOrdered() {
      super();
    }

    public OtherOrdered(double order) {
      super(order);
    }
  }
}
