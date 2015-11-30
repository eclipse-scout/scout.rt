/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.EventListener;

import org.eclipse.scout.commons.eventlistprofiler.EventListenerProfiler;
import org.eclipse.scout.commons.eventlistprofiler.IEventListenerSnapshot;
import org.eclipse.scout.commons.eventlistprofiler.IEventListenerSource;

/**
 * Thread safe Listener list supports @see WeakEventListener
 */
public class EventListenerList implements IEventListenerSource {
  private static final Object[] NULL_ARRAY = new Object[0];
  private final Object listenerListLock = new Object();
  private transient Object[] listenerList = NULL_ARRAY;

  public EventListenerList() {
    if (EventListenerProfiler.getInstance().isEnabled()) {
      EventListenerProfiler.getInstance().registerSourceAsWeakReference(this);
    }
  }

  @Override
  public void dumpListenerList(IEventListenerSnapshot snapshot) {
    synchronized (listenerListLock) {
      for (int i = listenerList.length - 2; i >= 0; i -= 2) {
        Class c = (Class) listenerList[i];
        Object o = listenerList[i + 1];
        if (o instanceof WeakReference) {
          snapshot.add(c, null, ((WeakReference) o).get());
        }
        else {
          snapshot.add(c, null, o);
        }
      }
    }
  }

  /**
   * @returns the listener in reverse order in which they were added
   */
  @SuppressWarnings("unchecked")
  public <T extends EventListener> T[] getListeners(Class<T> t) {
    synchronized (listenerListLock) {
      maintainListNoLocking();
      Object[] lList = listenerList;
      int n = getListenerCountNoLock(t);
      T[] result = (T[]) Array.newInstance(t, n);
      int j = 0;
      int nullCount = 0;
      for (int i = lList.length - 2; i >= 0; i -= 2) {
        if (lList[i] == t) {
          Object ref = lList[i + 1];
          if (ref instanceof WeakReference) {
            result[j] = (T) ((WeakReference) ref).get();
            if (result[j] == null) {
              nullCount++;
            }
            j++;
          }
          else {
            result[j++] = (T) ref;
          }
        }
      }
      if (nullCount > 0) {
        EventListener[] tmp = new EventListener[result.length - nullCount];
        j = 0;
        for (int i = 0; i < result.length; i++) {
          if (result[i] != null) {
            tmp[j++] = result[i];
          }
        }
        result = (T[]) tmp;
      }
      return result;
    }
  }

  public <T extends EventListener> int getListenerCount(Class<T> t) {
    synchronized (listenerListLock) {
      return getListenerCountNoLock(t);
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends EventListener> int getListenerCountNoLock(Class<T> t) {
    int count = 0;
    Object[] lList = listenerList;
    for (int i = 0; i < lList.length; i += 2) {
      if (t == (Class<T>) lList[i]) {
        count++;
      }
    }
    return count;
  }

  /**
   * Add the listener at the top (front) of the listener list (so it is called as LAST listener).
   */
  public <T extends EventListener> void insertAtFront(Class<T> t, T listener) {
    if (listener == null) {
      return;
    }
    Object ref;
    if (listener instanceof WeakEventListener) {
      ref = new WeakReference<EventListener>(listener);
    }
    else {
      ref = listener;
    }
    synchronized (listenerListLock) {
      if (listenerList == NULL_ARRAY) {
        listenerList = new Object[]{t, ref};
      }
      else {
        int n = listenerList.length + 2;
        int k = 0;
        Object[] tmp = new Object[n];
        if (k < n - 2) {
          System.arraycopy(listenerList, k, tmp, k + 2, n - 2 - k);
        }
        tmp[k] = t;
        tmp[k + 1] = ref;
        listenerList = tmp;
      }
      maintainListNoLocking();
    }
  }

  /**
   * The last listener added is the first to be called
   */
  public <T extends EventListener> void add(Class<T> t, T listener) {
    if (listener == null) {
      return;
    }
    Object ref;
    if (listener instanceof WeakEventListener) {
      ref = new WeakReference<EventListener>(listener);
    }
    else {
      ref = listener;
    }
    synchronized (listenerListLock) {
      if (listenerList == NULL_ARRAY) {
        listenerList = new Object[]{t, ref};
      }
      else {
        int i = listenerList.length;
        Object[] tmp = new Object[i + 2];
        System.arraycopy(listenerList, 0, tmp, 0, i);
        tmp[i] = t;
        tmp[i + 1] = ref;
        listenerList = tmp;
      }
      maintainListNoLocking();
    }
  }

  /**
   * Do not call this method directly, it must be called from within a listener synchronized block
   * (e.g. use listenerListLock).
   */
  private <T extends EventListener> boolean removeInternal(Class<T> t, T listener) {
    if (listener == null) {
      return false;
    }
    int index = -1;
    for (int i = listenerList.length - 2; i >= 0; i -= 2) {
      if (listenerList[i] == t) {
        if (listenerList[i + 1] instanceof WeakReference) {
          if (((WeakReference) listenerList[i + 1]).get() == listener) {
            index = i;
            break;
          }
        }
        else {
          if (listenerList[i + 1] == listener) {
            index = i;
            break;
          }
        }
      }
    }
    if (index != -1) {
      Object[] tmp = new Object[listenerList.length - 2];
      System.arraycopy(listenerList, 0, tmp, 0, index);
      if (index < tmp.length) {
        System.arraycopy(listenerList, index + 2, tmp, index, tmp.length - index);
      }
      listenerList = (tmp.length == 0) ? NULL_ARRAY : tmp;
    }
    return index != -1;
  }

  /**
   * Removes the first listener which is equal to the listener parameter from this listener list.
   */
  public <T extends EventListener> void remove(Class<T> t, T listener) {
    if (listener == null) {
      return;
    }
    synchronized (listenerListLock) {
      removeInternal(t, listener);
      maintainListNoLocking();
    }
  }

  /**
   * Removes all listeners which are equal to the listener parameter from this listener list.
   */
  public <T extends EventListener> void removeAll(Class<T> t, T listener) {
    if (listener == null) {
      return;
    }
    synchronized (listenerListLock) {
      while (removeInternal(t, listener)) {
        // nop
      }
      maintainListNoLocking();
    }
  }

  private void maintainListNoLocking() {
    int j = 0;
    int nullCount = 0;
    for (int i = listenerList.length - 2; i >= 0; i -= 2) {
      Object ref = listenerList[i + 1];
      if (ref instanceof WeakReference) {
        if (((WeakReference) ref).get() == null) {
          listenerList[i + 1] = null;
          nullCount++;
        }
      }
      else {
        if (ref == null) {
          listenerList[i + 1] = null;
          nullCount++;
        }
      }
    }
    if (nullCount > 0) {
      Object[] tmp = new Object[listenerList.length - nullCount * 2];
      j = 0;
      for (int i = 0; i < listenerList.length; i = i + 2) {
        if (listenerList[i + 1] != null) {
          tmp[j] = listenerList[i];
          tmp[j + 1] = listenerList[i + 1];
          j = j + 2;
        }
      }
      listenerList = tmp;
    }
  }

  @Override
  public String toString() {
    synchronized (listenerListLock) {
      Object[] lList = listenerList;
      String s = "EventListenerList: ";
      s += lList.length / 2 + " listeners: ";
      for (int i = 0; i <= lList.length - 2; i += 2) {
        s += " type " + ((Class) lList[i]).getName();
        s += " listener " + lList[i + 1];
      }
      return s;
    }
  }
}
