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

import java.util.List;

/**
 * High performance event listener list with support for frequent add/remove and weak listeners.
 * <p>
 * The high performance is reached by setting removed and garbage collected weak listeners to null instead on completely
 * removing them. The rebuild of the internal listener list is done lazy when there are enough accumulated null values.
 * <p>
 * This implementation is thread-safe.
 *
 * @since 8.0
 */
public class FastListenerList<LISTENER> extends UnsafeFastListenerList<LISTENER> {

  protected Object lockObject() {
    return indexes();
  }

  @Override
  public boolean isEmpty() {
    synchronized (lockObject()) {
      return super.isEmpty();
    }
  }

  @Override
  public int size() {
    synchronized (lockObject()) {
      return super.size();
    }
  }

  @Override
  public void add(LISTENER listener, boolean weak) {
    synchronized (lockObject()) {
      super.add(listener, weak);
    }
  }

  @Override
  public void addAll(UnsafeFastListenerList<LISTENER> srcList) {
    synchronized (lockObject()) {
      super.addAll(srcList);
    }
  }

  @Override
  public void remove(LISTENER listener) {
    synchronized (lockObject()) {
      super.remove(listener);
    }
  }

  @Override
  public List<LISTENER> list() {
    synchronized (lockObject()) {
      return super.list();
    }
  }
}
