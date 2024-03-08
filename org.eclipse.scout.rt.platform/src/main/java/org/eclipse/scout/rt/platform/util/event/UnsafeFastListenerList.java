/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util.event;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.scout.rt.platform.util.WeakEventListener;

/**
 * High performance event listener list with support for frequent add/remove and weak listeners.
 * <p>
 * The high performance is reached by setting removed and garbage collected weak listeners to null instead on completely
 * removing them. The rebuild of the internal listener list is done lazy when there are enough accumulated null values.
 * <p>
 * This listener list is not thread-safe
 *
 * @since 8.0
 */
public class UnsafeFastListenerList<LISTENER> implements IFastListenerList<LISTENER> {
  private final List<Object> m_refs = new ArrayList<>();
  private final Map<LISTENER, Integer> m_indexes = new WeakHashMap<>();

  @Override
  public int size() {
    return m_indexes.size();
  }

  @Override
  public boolean isEmpty() {
    return m_indexes.isEmpty();
  }

  @Override
  public void add(LISTENER listener) {
    add(listener, false);
  }

  @Override
  public void add(LISTENER listener, boolean weak) {
    if (m_indexes.containsKey(listener)) {
      return;
    }
    Object ref;
    if (weak || listener instanceof WeakEventListener) {
      ref = new WeakReference<>(listener);
    }
    else {
      ref = listener;
    }
    int i = m_refs.size();
    m_refs.add(ref);
    m_indexes.put(listener, i);
    maintain();
  }

  public void addAll(UnsafeFastListenerList<LISTENER> srcList) {
    for (Object ref : srcList.m_refs) {
      if (ref instanceof WeakReference) {
        @SuppressWarnings("unchecked")
        LISTENER listener = (LISTENER) ((Reference) ref).get();
        if (listener != null) {
          add(listener, true);
        }
      }
      else {
        @SuppressWarnings("unchecked")
        LISTENER listener = (LISTENER) ref;
        if (listener != null) {
          add(listener, false);
        }
      }
    }
  }

  @Override
  public void remove(LISTENER listener) {
    Integer i = m_indexes.remove(listener);
    if (i != null) {
      m_refs.set(i, null);
    }
    maintain();
  }

  /**
   * Iterates all listeners in the order to be called. Null values are skipped automatically.
   */
  @Override
  public List<LISTENER> list() {
    maintain();
    if (m_indexes.isEmpty()) {
      return Collections.emptyList();
    }
    ArrayList<LISTENER> result = new ArrayList<>(m_indexes.size());
    for (int i = m_refs.size() - 1; i >= 0; i--) {
      LISTENER listener = get(i);
      if (listener != null) {
        result.add(listener);
      }
    }
    return result;
  }

  /**
   * listeners are in reverse order, last listener must handle event first
   * <p>
   *
   * @return the listener, potentially null if a weak referenced listener was garbage collected
   */
  @SuppressWarnings("unchecked")
  protected LISTENER get(int i) {
    Object ref = m_refs.get(i);
    if (ref instanceof WeakReference) {
      ref = ((Reference) ref).get();
      if (ref == null) {
        m_refs.set(i, null);
      }
    }
    return (LISTENER) ref;
  }

  protected void maintain() {
    if (m_indexes.isEmpty()) {
      if (!m_refs.isEmpty()) {
        m_refs.clear();
      }
      return;
    }
    if (m_refs.size() <= 2 * m_indexes.size()) {
      return;
    }
    ArrayList<Object> tmp = new ArrayList<>(m_indexes.size());
    for (int i = 0; i < m_refs.size(); i++) {
      Object ref = m_refs.get(i);
      if (ref instanceof WeakReference) {
        if (((WeakReference) ref).get() != null) {
          tmp.add(ref);
        }
      }
      else if (ref != null) {
        tmp.add(ref);
      }
    }
    m_refs.clear();
    m_indexes.clear();
    if (!tmp.isEmpty()) {
      for (int i = 0; i < tmp.size(); i++) {
        Object ref = tmp.get(i);
        @SuppressWarnings("unchecked")
        LISTENER listener = (LISTENER) (ref instanceof WeakReference ? ((WeakReference) ref).get() : ref);
        m_refs.add(ref);
        m_indexes.put(listener, i);
      }
    }
  }

  /**
   * Used for unit testing
   */
  protected List<Object> refs() {
    return m_refs;
  }

  /**
   * Used for unit testing
   */
  protected Map<LISTENER, Integer> indexes() {
    return m_indexes;
  }
}
