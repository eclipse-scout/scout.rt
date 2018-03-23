/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertNotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link SimpleEventListenerList}
 */
public class SimpleEventListenerListTest {

  /**
   * Test for {@link SimpleEventListenerList#add( java.util.EventListener)}
   */
  @Test
  public void testAdd() {
    SimpleEventListenerList<EventListener> listenerList = new SimpleEventListenerList<>();
    EventListener listener = new EventListener() {
    };
    listenerList.add(listener);
    listenerList.add(listener);
    Assert.assertEquals(1, listenerList.getListenerCount());
  }

  /**
   * Test for {@link SimpleEventListenerList#add( java.util.EventListener)}
   */
  @Test
  public void testRemove() {
    SimpleEventListenerList<EventListener> listenerList = new SimpleEventListenerList<>();
    EventListener listener = new EventListener() {
    };
    listenerList.add(listener);
    listenerList.add(listener);
    listenerList.remove(listener);
    Assert.assertEquals(0, listenerList.getListenerCount());
  }

  /**
   * Test for {@link SimpleEventListenerList#removeAll( java.util.EventListener)}
   */
  @Test
  public void testRemoveAll() {
    SimpleEventListenerList<EventListener> listenerList = new SimpleEventListenerList<>();
    EventListener listener = new EventListener() {
    };
    listenerList.add(listener);
    listenerList.add(listener);
    listenerList.remove(listener);
    Assert.assertEquals(0, listenerList.getListenerCount());
  }

  @Test
  public void testLazyRemove() {
    SimpleEventListenerList<EventListener> listenerList = new SimpleEventListenerList<>();
    EventListener[] a = new EventListener[10];
    for (int i = 0; i < a.length; i++) {
      a[i] = new EventListener() {
      };
      listenerList.add(a[i]);
    }
    Assert.assertEquals(10, listenerList.size());
    Assert.assertEquals(expectedList(9, 8, 7, 6, 5, 4, 3, 2, 1, 0), iteratorDump(a, listenerList));

    listenerList.remove(a[1]);
    Assert.assertEquals(9, listenerList.size());
    Assert.assertEquals(expectedList(9, 8, 7, 6, 5, 4, 3, 2, 0), iteratorDump(a, listenerList));

    listenerList.remove(a[3]);
    Assert.assertEquals(8, listenerList.size());
    Assert.assertEquals(expectedList(9, 8, 7, 6, 5, 4, 2, 0), iteratorDump(a, listenerList));

    listenerList.remove(a[5]);
    Assert.assertEquals(7, listenerList.size());
    Assert.assertEquals(expectedList(9, 8, 7, 6, 4, 2, 0), iteratorDump(a, listenerList));

    listenerList.remove(a[7]);
    Assert.assertEquals(6, listenerList.size());
    Assert.assertEquals(expectedList(9, 8, 6, 4, 2, 0), iteratorDump(a, listenerList));

    listenerList.remove(a[9]);
    //now 5 null values, list.size=10, map.size=5 -> 10 ?> 2*5 NO
    Assert.assertEquals(5, listenerList.size());
    Assert.assertEquals(expectedList(8, 6, 4, 2, 0), iteratorDump(a, listenerList));

    listenerList.remove(a[0]);
    //now 6 null values, list.size=10, map.size=4 -> 10 > 2*4 YES, rebuild list
    Assert.assertEquals(4, listenerList.size());
    Assert.assertEquals(expectedList(8, 6, 4, 2), iteratorDump(a, listenerList));

    listenerList.remove(a[2]);
    Assert.assertEquals(3, listenerList.size());
    Assert.assertEquals(expectedList(8, 6, 4), iteratorDump(a, listenerList));

    listenerList.remove(a[4]);
    Assert.assertEquals(2, listenerList.size());
    Assert.assertEquals(expectedList(8, 6), iteratorDump(a, listenerList));

    listenerList.remove(a[6]);
    //now 3 null values, list.size=4, map.size=1 -> 4 > 2*1 YES, rebuild list
    Assert.assertEquals(1, listenerList.size());
    Assert.assertEquals(expectedList(8), iteratorDump(a, listenerList));

    listenerList.remove(a[8]);
    //now 1 null value, list.size=1, map.size=0 -> empty map, rebuild list
    Assert.assertEquals(0, listenerList.size());
    Assert.assertEquals(expectedList(), iteratorDump(a, listenerList));
  }

  @Test
  public void testWeakRemove() {
    SimpleEventListenerList<EventListener> listenerList = new SimpleEventListenerList<>();
    EventListener[] a = new EventListener[5];
    for (int i = 0; i < a.length; i++) {
      a[i] = new EventListener() {
      };
      listenerList.add(a[i], true);
    }
    Assert.assertEquals(5, listenerList.size());
    Assert.assertEquals(expectedList(4, 3, 2, 1, 0), iteratorDump(a, listenerList));

    simulateGC(listenerList, a[1]);
    Assert.assertEquals(4, listenerList.indexes().size());
    Assert.assertEquals(5, listenerList.refs().size());
    Assert.assertEquals(4, listenerList.size());
    Assert.assertEquals(expectedList(4, 3, 2, 0), iteratorDump(a, listenerList));

    simulateGC(listenerList, a[3]);
    Assert.assertEquals(3, listenerList.indexes().size());
    Assert.assertEquals(5, listenerList.refs().size());
    Assert.assertEquals(3, listenerList.size());
    Assert.assertEquals(expectedList(4, 2, 0), iteratorDump(a, listenerList));

    simulateGC(listenerList, a[0]);
    Assert.assertEquals(2, listenerList.indexes().size());
    Assert.assertEquals(5, listenerList.refs().size());
    Assert.assertEquals(2, listenerList.size());
    Assert.assertEquals(expectedList(4, 2), iteratorDump(a, listenerList));
    //the getters triggered maintain(), 5>2*2 -> rebuild

    simulateGC(listenerList, a[2]);
    Assert.assertEquals(1, listenerList.indexes().size());
    Assert.assertEquals(2, listenerList.refs().size());
    Assert.assertEquals(1, listenerList.size());
    Assert.assertEquals(expectedList(4), iteratorDump(a, listenerList));

    simulateGC(listenerList, a[4]);
    Assert.assertEquals(0, listenerList.indexes().size());
    Assert.assertEquals(2, listenerList.refs().size());
    Assert.assertEquals(0, listenerList.size());
    Assert.assertEquals(expectedList(), iteratorDump(a, listenerList));
  }

  private static List<Integer> expectedList(int... indexes) {
    List<Integer> list = new ArrayList<>();
    for (int i : indexes) {
      list.add(i);
    }
    return list;
  }

  private static <T extends EventListener> List<Integer> iteratorDump(EventListener[] a, SimpleEventListenerList<T> listenerList) {
    List<EventListener> aList = Arrays.asList(a);
    List<Integer> list = new ArrayList<>();
    for (T listener : listenerList) {
      assertNotNull(listener);
      list.add(aList.indexOf(listener));
    }
    return list;
  }

  private static void simulateGC(SimpleEventListenerList<?> listenerList, EventListener listener) {
    WeakReference ref = null;
    for (Object o : listenerList.refs()) {
      if (o instanceof WeakReference && ((WeakReference) o).get() == listener) {
        ref = (WeakReference) o;
        break;
      }
    }
    assertNotNull(ref);
    listenerList.indexes().remove(ref.get());
    ref.clear();
  }
}
