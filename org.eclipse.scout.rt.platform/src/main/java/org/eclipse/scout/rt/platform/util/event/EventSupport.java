/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.util.event;

import java.util.EventObject;

/**
 * Maintains a list of event listeners. Events can be fired to all currently registered event listeners. This class is
 * intended to be used internally by objects that implement the {@link IEventEmitter} interface.
 *
 * @param <EVENT>
 *          Event type that is fired.
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
