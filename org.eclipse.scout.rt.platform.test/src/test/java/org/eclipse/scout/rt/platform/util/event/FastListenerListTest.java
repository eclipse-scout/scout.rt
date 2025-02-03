/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.event;

import static org.junit.Assert.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link FastListenerList}
 */
public class FastListenerListTest {

  /**
   * Test for {@link FastListenerList#add(java.util.EventListener)}
   */
  @Test
  public void testAdd() {
    FastListenerList<EventListener> listenerList = new FastListenerList<>();
    EventListener listener = new EventListener() {
    };
    listenerList.add(listener);
    listenerList.add(listener);
    Assert.assertEquals(1, listenerList.indexes().size());
  }

  /**
   * Test for {@link FastListenerList#add(java.util.EventListener)}
   */
  @Test
  public void testRemove() {
    FastListenerList<EventListener> listenerList = new FastListenerList<>();
    EventListener listener = new EventListener() {
    };
    listenerList.add(listener);
    listenerList.add(listener);
    listenerList.remove(listener);
    Assert.assertEquals(0, listenerList.indexes().size());
  }

  /**
   * Test for {@link FastListenerList#removeAll(java.util.EventListener)}
   */
  @Test
  public void testRemoveAll() {
    FastListenerList<EventListener> listenerList = new FastListenerList<>();
    EventListener listener = new EventListener() {
    };
    listenerList.add(listener);
    listenerList.add(listener);
    listenerList.remove(listener);
    Assert.assertEquals(0, listenerList.indexes().size());
  }

  @Test
  public void testLazyRemove() {
    FastListenerList<EventListener> listenerList = new FastListenerList<>();
    EventListener[] a = new EventListener[10];
    for (int i = 0; i < a.length; i++) {
      a[i] = new EventListener() {
      };
      listenerList.add(a[i]);
    }
    Assert.assertEquals(10, listenerList.indexes().size());
    Assert.assertEquals(expectedList(9, 8, 7, 6, 5, 4, 3, 2, 1, 0), iteratorDump(a, listenerList));

    listenerList.remove(a[1]);
    Assert.assertEquals(9, listenerList.indexes().size());
    Assert.assertEquals(expectedList(9, 8, 7, 6, 5, 4, 3, 2, 0), iteratorDump(a, listenerList));

    listenerList.remove(a[3]);
    Assert.assertEquals(8, listenerList.indexes().size());
    Assert.assertEquals(expectedList(9, 8, 7, 6, 5, 4, 2, 0), iteratorDump(a, listenerList));

    listenerList.remove(a[5]);
    Assert.assertEquals(7, listenerList.indexes().size());
    Assert.assertEquals(expectedList(9, 8, 7, 6, 4, 2, 0), iteratorDump(a, listenerList));

    listenerList.remove(a[7]);
    Assert.assertEquals(6, listenerList.indexes().size());
    Assert.assertEquals(expectedList(9, 8, 6, 4, 2, 0), iteratorDump(a, listenerList));

    listenerList.remove(a[9]);
    //now 5 null values, list.size=10, map.size=5 -> 10 ?> 2*5 NO
    Assert.assertEquals(5, listenerList.indexes().size());
    Assert.assertEquals(expectedList(8, 6, 4, 2, 0), iteratorDump(a, listenerList));

    listenerList.remove(a[0]);
    //now 6 null values, list.size=10, map.size=4 -> 10 > 2*4 YES, rebuild list
    Assert.assertEquals(4, listenerList.indexes().size());
    Assert.assertEquals(expectedList(8, 6, 4, 2), iteratorDump(a, listenerList));

    listenerList.remove(a[2]);
    Assert.assertEquals(3, listenerList.indexes().size());
    Assert.assertEquals(expectedList(8, 6, 4), iteratorDump(a, listenerList));

    listenerList.remove(a[4]);
    Assert.assertEquals(2, listenerList.indexes().size());
    Assert.assertEquals(expectedList(8, 6), iteratorDump(a, listenerList));

    listenerList.remove(a[6]);
    //now 3 null values, list.size=4, map.size=1 -> 4 > 2*1 YES, rebuild list
    Assert.assertEquals(1, listenerList.indexes().size());
    Assert.assertEquals(expectedList(8), iteratorDump(a, listenerList));

    listenerList.remove(a[8]);
    //now 1 null value, list.size=1, map.size=0 -> empty map, rebuild list
    Assert.assertEquals(0, listenerList.indexes().size());
    Assert.assertEquals(expectedList(), iteratorDump(a, listenerList));
  }

  @Test
  public void testWeakRemove() {
    FastListenerList<EventListener> listenerList = new FastListenerList<>();
    EventListener[] a = new EventListener[5];
    for (int i = 0; i < a.length; i++) {
      a[i] = new EventListener() {
      };
      listenerList.add(a[i], true);
    }
    Assert.assertEquals(5, listenerList.indexes().size());
    Assert.assertEquals(expectedList(4, 3, 2, 1, 0), iteratorDump(a, listenerList));

    simulateGC(listenerList, a[1]);
    Assert.assertEquals(4, listenerList.indexes().size());
    Assert.assertEquals(5, listenerList.refs().size());
    Assert.assertEquals(expectedList(4, 3, 2, 0), iteratorDump(a, listenerList));

    simulateGC(listenerList, a[3]);
    Assert.assertEquals(3, listenerList.indexes().size());
    Assert.assertEquals(5, listenerList.refs().size());
    Assert.assertEquals(expectedList(4, 2, 0), iteratorDump(a, listenerList));

    simulateGC(listenerList, a[0]);
    Assert.assertEquals(2, listenerList.indexes().size());
    Assert.assertEquals(5, listenerList.refs().size());
    Assert.assertEquals(expectedList(4, 2), iteratorDump(a, listenerList));
    //the getters triggered maintain(), 5>2*2 -> rebuild

    simulateGC(listenerList, a[2]);
    Assert.assertEquals(1, listenerList.indexes().size());
    Assert.assertEquals(2, listenerList.refs().size());
    Assert.assertEquals(expectedList(4), iteratorDump(a, listenerList));

    simulateGC(listenerList, a[4]);
    Assert.assertEquals(0, listenerList.indexes().size());
    Assert.assertEquals(2, listenerList.refs().size());
    Assert.assertEquals(expectedList(), iteratorDump(a, listenerList));
  }

  @Test
  public void testRemoveOnFire() {
    final FastListenerList<EventListener> listenerList = new FastListenerList<>();
    EventListener[] a = new EventListener[5];
    for (int i = 0; i < a.length; i++) {
      a[i] = new FixtureEventListenerThatRemovesOnFire() {
        @Override
        public void handle(Object event) {
          listenerList.remove(this);
        }
      };
      listenerList.add(a[i], true);
    }
    Assert.assertEquals(5, listenerList.indexes().size());
    Assert.assertEquals(expectedList(4, 3, 2, 1, 0), iteratorDump(a, listenerList));

    for (EventListener listener : listenerList.list()) {
      ((FixtureEventListenerThatRemovesOnFire) listener).handle(null);
    }

    Assert.assertEquals(0, listenerList.indexes().size());
    Assert.assertTrue(listenerList.isEmpty());
    Assert.assertEquals(expectedList(), iteratorDump(a, listenerList));
  }

  @Test
  public void testAddAll() {
    FastListenerList<EventListener> src = new FastListenerList<>();
    EventListener a = new EventListener() {
    };
    EventListener aw = new EventListener() {
    };
    src.add(a, false);
    src.add(aw, true);
    assertEquals(Arrays.asList(aw, a), src.list());

    FastListenerList<EventListener> dst = new FastListenerList<>();
    dst.addAll(src);
    assertEquals(Arrays.asList(aw, a), dst.list());
  }

  private static List<Integer> expectedList(int... indexes) {
    List<Integer> list = new ArrayList<>();
    for (int i : indexes) {
      list.add(i);
    }
    return list;
  }

  private static <T extends EventListener> List<Integer> iteratorDump(EventListener[] a, FastListenerList<T> listenerList) {
    List<EventListener> aList = Arrays.asList(a);
    List<Integer> list = new ArrayList<>();
    for (T listener : listenerList.list()) {
      assertNotNull(listener);
      list.add(aList.indexOf(listener));
    }
    return list;
  }

  private static void simulateGC(FastListenerList<?> listenerList, EventListener listener) {
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

  private static interface FixtureEventListenerThatRemovesOnFire extends EventListener {
    void handle(Object event);
  }
}
