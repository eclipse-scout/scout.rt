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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.EventListener;

import org.eclipse.scout.rt.platform.eventlistprofiler.EventListenerProfiler;
import org.eclipse.scout.rt.platform.eventlistprofiler.IEventListenerSnapshot;
import org.eclipse.scout.rt.platform.eventlistprofiler.IEventListenerSource;

/**
 * Thread safe Listener list supports @see WeakEventListener
 */
public class EventListenerList implements IEventListenerSource {
  private static final Object[] NULL_ARRAY = new Object[0];
  private final Object m_listenerListLock = new Object();
  private Object[] m_listenerList = NULL_ARRAY;

  public EventListenerList() {
    if (EventListenerProfiler.getInstance().isEnabled()) {
      EventListenerProfiler.getInstance().registerSourceAsWeakReference(this);
    }
  }

  @Override
  public void dumpListenerList(IEventListenerSnapshot snapshot) {
    synchronized (m_listenerListLock) {
      for (int i = m_listenerList.length - 2; i >= 0; i -= 2) {
        Class c = (Class) m_listenerList[i];
        Object o = m_listenerList[i + 1];
        if (o instanceof WeakReference) {
          snapshot.add(c, null, ((Reference) o).get());
        }
        else {
          snapshot.add(c, null, o);
        }
      }
    }
  }

  /**
   * @return the listener in reverse order in which they were added
   */
  @SuppressWarnings("unchecked")
  public <T extends EventListener> T[] getListeners(Class<T> t) {
    synchronized (m_listenerListLock) {
      maintainListNoLocking();
      Object[] lList = m_listenerList;
      int n = getListenerCountNoLock(t);
      T[] result = (T[]) Array.newInstance(t, n);
      int j = 0;
      int nullCount = 0;
      for (int i = lList.length - 2; i >= 0; i -= 2) {
        if (lList[i] == t) {
          Object ref = lList[i + 1];
          if (ref instanceof WeakReference) {
            result[j] = (T) ((Reference) ref).get();
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
        for (T aResult : result) {
          if (aResult != null) {
            tmp[j++] = aResult;
          }
        }
        result = (T[]) tmp;
      }
      return result;
    }
  }

  public <T extends EventListener> int getListenerCount(Class<T> t) {
    synchronized (m_listenerListLock) {
      return getListenerCountNoLock(t);
    }
  }

  private <T extends EventListener> int getListenerCountNoLock(Class<T> t) {
    int count = 0;
    Object[] lList = m_listenerList;
    for (int i = 0; i < lList.length; i += 2) {
      if (t == lList[i]) {
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
    synchronized (m_listenerListLock) {
      if (m_listenerList == NULL_ARRAY) {
        m_listenerList = new Object[]{t, ref};
      }
      else {
        int n = m_listenerList.length + 2;
        int k = 0;
        Object[] tmp = new Object[n];
        if (k < n - 2) {
          System.arraycopy(m_listenerList, k, tmp, k + 2, n - 2 - k);
        }
        tmp[k] = t;
        tmp[k + 1] = ref;
        m_listenerList = tmp;
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
    synchronized (m_listenerListLock) {
      if (m_listenerList == NULL_ARRAY) {
        m_listenerList = new Object[]{t, ref};
      }
      else {
        int i = m_listenerList.length;
        Object[] tmp = new Object[i + 2];
        System.arraycopy(m_listenerList, 0, tmp, 0, i);
        tmp[i] = t;
        tmp[i + 1] = ref;
        m_listenerList = tmp;
      }
      maintainListNoLocking();
    }
  }

  /**
   * Do not call this method directly, it must be called from within a listener synchronized block (e.g. use
   * listenerListLock).
   */
  private <T extends EventListener> boolean removeInternal(Class<T> t, T listener) {
    if (listener == null) {
      return false;
    }
    int index = -1;
    for (int i = m_listenerList.length - 2; i >= 0; i -= 2) {
      if (m_listenerList[i] == t) {
        if (m_listenerList[i + 1] instanceof WeakReference) {
          if (((Reference) m_listenerList[i + 1]).get() == listener) {
            index = i;
            break;
          }
        }
        else {
          if (m_listenerList[i + 1] == listener) {
            index = i;
            break;
          }
        }
      }
    }
    if (index != -1) {
      Object[] tmp = new Object[m_listenerList.length - 2];
      System.arraycopy(m_listenerList, 0, tmp, 0, index);
      if (index < tmp.length) {
        System.arraycopy(m_listenerList, index + 2, tmp, index, tmp.length - index);
      }
      m_listenerList = (tmp.length == 0) ? NULL_ARRAY : tmp;
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
    synchronized (m_listenerListLock) {
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
    synchronized (m_listenerListLock) {
      while (removeInternal(t, listener)) {
        // nop
      }
      maintainListNoLocking();
    }
  }

  private void maintainListNoLocking() {
    int j = 0;
    int nullCount = 0;
    for (int i = m_listenerList.length - 2; i >= 0; i -= 2) {
      Object ref = m_listenerList[i + 1];
      if (ref instanceof WeakReference) {
        if (((Reference) ref).get() == null) {
          m_listenerList[i + 1] = null;
          nullCount++;
        }
      }
      else {
        if (ref == null) {
          m_listenerList[i + 1] = null;
          nullCount++;
        }
      }
    }
    if (nullCount > 0) {
      Object[] tmp = new Object[m_listenerList.length - nullCount * 2];
      j = 0;
      for (int i = 0; i < m_listenerList.length; i = i + 2) {
        if (m_listenerList[i + 1] != null) {
          tmp[j] = m_listenerList[i];
          tmp[j + 1] = m_listenerList[i + 1];
          j = j + 2;
        }
      }
      m_listenerList = tmp;
    }
  }

  @Override
  public String toString() {
    synchronized (m_listenerListLock) {
      Object[] lList = m_listenerList;
      StringBuilder sb = new StringBuilder("EventListenerList: ");
      sb.append(lList.length / 2).append(" listeners: ");
      for (int i = 0; i <= lList.length - 2; i += 2) {
        sb.append(" type ").append(((Class) lList[i]).getName());
        sb.append(" listener ").append(lList[i + 1]);
      }
      return sb.toString();
    }
  }
}
