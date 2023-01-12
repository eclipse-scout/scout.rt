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

public class DisabledListenerListProfiler implements IListenerListProfiler {

  @Override
  public ListenerListSnapshot createSnapshot() {
    return new ListenerListSnapshot();
  }

  @Override
  public void registerAsWeakReference(IListenerListWithManagement eventListenerList) {
    // nop
  }

  @Override
  public int getListenerListCount() {
    return 0;
  }
}
