/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.events;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * This class is Thread safe
 */
public class DefaultListenerListProfiler implements IListenerListProfiler {

  private final Map<IListenerListWithManagement, Object> m_set = new WeakHashMap<>();

  /**
   * Add a weak reference to a event listener list
   * <p>
   * NOTE: This monitor does not add a reference to the argument. If the passed argument is not referenced by the source
   * type, it is garbage collected almost immediately after the call to this method
   */
  @Override
  public void registerAsWeakReference(IListenerListWithManagement eventListenerList) {
    synchronized (m_set) {
      m_set.put(eventListenerList, null);
    }
  }

  @Override
  public int getListenerListCount() {
    synchronized (m_set) {
      return m_set.size();
    }
  }

  @Override
  public ListenerListSnapshot createSnapshot() {
    ListenerListSnapshot snapshot = new ListenerListSnapshot();
    synchronized (m_set) {
      for (IListenerListWithManagement list : m_set.keySet()) {
        if (list != null) {
          list.createSnapshot((context, listener) -> snapshot.add(list, context, listener));
        }
      }
    }
    return snapshot;
  }
}
