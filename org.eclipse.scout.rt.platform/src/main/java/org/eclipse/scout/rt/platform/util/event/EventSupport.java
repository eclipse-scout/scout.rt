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

import java.util.EventObject;

/**
 * Maintains a list of event listeners. Events can be fired to all currently registered event listeners. This class is
 * intended to be used internally by objects that implement the {@link IEventEmitter} interface.
 *
 * @param <EVENT>
 *     Event type that is fired.
 * @since 22.0
 */
public class EventSupport<EVENT extends EventObject> {

  private final FastListenerList<IEventListener<EVENT>> m_listenerList = new FastListenerList<>();

  public IFastListenerList<IEventListener<EVENT>> getListeners() {
    return m_listenerList;
  }

  public void addListener(IEventListener<EVENT> listener) {
    m_listenerList.add(listener);
  }

  public void removeListener(IEventListener<EVENT> listener) {
    m_listenerList.remove(listener);
  }

  public void fireEvent(EVENT event) {
    m_listenerList.list().forEach(listener -> listener.fireEvent(event));
  }
}
