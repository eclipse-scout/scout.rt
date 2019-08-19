/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util.event;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.TuningUtility;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @since 8.0
 */
public class AbstractGroupedListenerListTest {

  private int fireCount;

  /**
   * show that {@link ArrayList} and arrays have more or less same acceptable performance
   */
  @Ignore
  @Test
  public void testArrayVsList() {
    int n = 10000000;
    FixtureEventListener[] data = new FixtureEventListener[n];
    for (int i = 0; i < n; i++) {
      data[i] = event -> {
      };
    }

    TuningUtility.startTimer();
    FixtureEventListener[] array = new FixtureEventListener[n];
    for (int i = 0; i < n; i++) {
      array[i] = data[i];
    }
    TuningUtility.stopTimer("fill array[" + array.length + "]");

    TuningUtility.startTimer();
    ArrayList<FixtureEventListener> list = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      list.add(data[i]);
    }
    TuningUtility.stopTimer("fill list[" + list.size() + "]");

    long count = 0;
    TuningUtility.startTimer();
    for (int i = 0; i < n; i++) {
      if (array[i] != null) {
        count++;
      }
    }
    TuningUtility.stopTimer("loop over array[" + count + "]");

    count = 0;
    TuningUtility.startTimer();
    for (int i = 0; i < n; i++) {
      if (list.get(i) != null) {
        count++;
      }
    }
    TuningUtility.stopTimer("loop over list[" + count + "]");

    TuningUtility.startTimer();
    FixtureEventListener[] tmp = new FixtureEventListener[array.length * 2];
    System.arraycopy(array, 0, tmp, 0, array.length);
    array = tmp;
    for (int i = 0; i < n; i++) {
      array[n + i] = data[i];
    }
    TuningUtility.stopTimer("extend array[" + array.length + "]");

    TuningUtility.startTimer();
    for (int i = 0; i < n; i++) {
      list.add(data[i]);
    }
    TuningUtility.stopTimer("extend list[" + list.size() + "]");

    TuningUtility.startTimer();
    array = new FixtureEventListener[0];
    TuningUtility.stopTimer("clear array[" + array.length + "]");

    TuningUtility.startTimer();
    list.clear();
    TuningUtility.stopTimer("clear list[" + list.size() + "]");
  }

  /*
  #TUNING: add1 50000 took 150ms
  #TUNING: add1-last 50000 took 170ms
  #TUNING: add2 50000 took 30ms
  #TUNING: add2-last 50000 took 29ms
  #TUNING: event1 took 36ms
  #TUNING: event2 took 17ms
  #TUNING: remove1 50000 took 312ms
  #TUNING: remove2 50000 took 46ms
  */
  @Test
  public void testBasicOperations() {
    int n = 50000;
    ArrayList<FixtureEventListener> listeners = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      listeners.add(e -> fireCount++);
    }
    FixtureEventListeners observer = new FixtureEventListeners();

    TuningUtility.startTimer();
    for (FixtureEventListener listener : listeners) {
      observer.add(listener, false);
    }
    TuningUtility.stopTimer("add1 " + listeners.size());
    UnsafeFastListenerList<?> map1 = observer.internalListenerMap(false).get(null);
    assertEquals(n, map1.refs().size());
    assertEquals(n, map1.indexes().size());

    TuningUtility.startTimer();
    for (FixtureEventListener listener : listeners) {
      observer.addLastCalled(listener, false);
    }
    TuningUtility.stopTimer("add1-last " + listeners.size());
    UnsafeFastListenerList<?> map1Last = observer.internalListenerMap(true).get(null);
    assertEquals(n, map1Last.refs().size());
    assertEquals(n, map1Last.indexes().size());

    TuningUtility.startTimer();
    for (FixtureEventListener listener : listeners) {
      observer.add(listener, false, FixtureEvent.TYPE_NODE_ACTION);
    }
    TuningUtility.stopTimer("add2 " + listeners.size());
    UnsafeFastListenerList<?> map2 = observer.internalListenerMap(false).get(FixtureEvent.TYPE_NODE_ACTION);
    assertEquals(n, map2.refs().size());
    assertEquals(n, map2.indexes().size());

    TuningUtility.startTimer();
    for (FixtureEventListener listener : listeners) {
      observer.addLastCalled(listener, false, FixtureEvent.TYPE_NODE_ACTION);
    }
    TuningUtility.stopTimer("add2-last " + listeners.size());
    UnsafeFastListenerList<?> map2Last = observer.internalListenerMap(true).get(FixtureEvent.TYPE_NODE_ACTION);
    assertEquals(n, map2Last.refs().size());
    assertEquals(n, map2Last.indexes().size());

    fireCount = 0;
    TuningUtility.startTimer();
    observer.fireEvent(new FixtureEvent(FixtureEvent.TYPE_REQUEST_FOCUS));
    TuningUtility.stopTimer("event1");
    assertEquals(2 * n, fireCount);

    fireCount = 0;
    TuningUtility.startTimer();
    observer.fireEvent(new FixtureEvent(FixtureEvent.TYPE_NODE_ACTION));
    TuningUtility.stopTimer("event2");
    assertEquals(2 * n, fireCount);

    TuningUtility.startTimer();
    for (FixtureEventListener listener : listeners) {
      observer.remove(listener);
    }
    TuningUtility.stopTimer("remove1 " + listeners.size());
    assertEquals(0, map1.refs().size());
    assertEquals(0, map1.indexes().size());

    TuningUtility.startTimer();
    for (FixtureEventListener listener : listeners) {
      observer.remove(listener, FixtureEvent.TYPE_NODE_ACTION);
    }
    TuningUtility.stopTimer("remove2 " + listeners.size());
    assertEquals(0, map2.refs().size());
    assertEquals(0, map2.indexes().size());

    TuningUtility.finishAll();
  }

  @Test
  public void testAddAll() {
    FixtureEventListeners src = new FixtureEventListeners();
    FixtureEventListener a = createListener("a");
    FixtureEventListener aw = createListener("aw");
    FixtureEventListener a1 = createListener("a1");
    FixtureEventListener a12w = createListener("a12w");
    FixtureEventListener b = createListener("b");
    FixtureEventListener bw = createListener("bw");
    FixtureEventListener b1 = createListener("b1");
    FixtureEventListener b12w = createListener("b12w");
    src.add(a, false);//add for all event types
    src.add(aw, true);//same, weak
    src.add(a1, false, 1);//add for unknown event type 1
    src.add(a12w, true, 1, FixtureEvent.TYPE_NODE_ACTION);//add for known event type 20, weak
    src.addLastCalled(b, false);
    src.addLastCalled(bw, true);
    src.addLastCalled(b1, false, 1);
    src.addLastCalled(b12w, true, 1, FixtureEvent.TYPE_NODE_ACTION);
    assertEquals(Arrays.asList(a12w, a1, aw, a, b12w, b1, bw, b), src.list(1));
    assertEquals(Arrays.asList(a12w, aw, a, b12w, bw, b), src.list(FixtureEvent.TYPE_NODE_ACTION));
    assertEquals(Arrays.asList(aw, a, bw, b), src.list(9));

    FixtureEventListeners dst = new FixtureEventListeners();
    dst.addAll(src);
    assertEquals(Arrays.asList(a12w, a1, aw, a, b12w, b1, bw, b), dst.list(1));
    assertEquals(Arrays.asList(a12w, aw, a, b12w, bw, b), dst.list(FixtureEvent.TYPE_NODE_ACTION));
    assertEquals(Arrays.asList(aw, a, bw, b), dst.list(9));
  }

  private static FixtureEventListener createListener(String name) {
    return new FixtureEventListener() {
      @Override
      public void handle(FixtureEvent event) {
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  private static class FixtureEvent {
    public static final int TYPE_REQUEST_FOCUS = 10;
    public static final int TYPE_NODE_ACTION = 20;

    public final int type;

    FixtureEvent(int type) {
      this.type = type;
    }
  }

  private static interface FixtureEventListener {
    void handle(FixtureEvent event);
  }

  private static class FixtureEventListeners extends AbstractGroupedListenerList<FixtureEventListener, FixtureEvent, Integer> {
    private static final Set<Integer> KNOWN_EVENT_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        FixtureEvent.TYPE_REQUEST_FOCUS,
        FixtureEvent.TYPE_NODE_ACTION)));

    @Override
    protected Integer eventType(FixtureEvent event) {
      return event.type;
    }

    @Override
    protected Set<Integer> knownEventTypes() {
      return KNOWN_EVENT_TYPES;
    }

    @Override
    protected Integer otherEventsType() {
      return null;
    }

    @Override
    protected void handleEvent(FixtureEventListener listener, FixtureEvent event) {
      listener.handle(event);
    }
  }
}
