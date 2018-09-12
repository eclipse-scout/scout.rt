/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.events;

import java.util.WeakHashMap;

/**
 * This class is Thread safe
 */
public final class ListenerListRegistry {
  private static final ListenerListRegistry INSTANCE = new ListenerListRegistry();
  private static final Object DUMMY_VALUE = null;

  public static ListenerListRegistry globalInstance() {
    return INSTANCE;
  }

  private final WeakHashMap<IListenerListWithManagement, Object> m_set = new WeakHashMap<>();

  private ListenerListRegistry() {
    //singleton
  }

  /**
   * Add a weak reference to a event listener list
   * <p>
   * NOTE: This monitor does not add a reference to the argument. If the passed argument is not referenced by the source
   * type, it is garbage collected almost immediately after the call to this method
   */
  public void registerAsWeakReference(IListenerListWithManagement eventListenerList) {
    synchronized (m_set) {
      m_set.put(eventListenerList, DUMMY_VALUE);
    }
  }

  public int getListenerListCount() {
    synchronized (m_set) {
      return m_set.size();
    }
  }

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
