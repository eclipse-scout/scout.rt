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

public interface IListenerListProfiler {

  ListenerListSnapshot createSnapshot();

  /**
   * Add a weak reference to a event listener list
   * <p>
   * NOTE: This monitor does not add a reference to the argument. If the passed argument is not referenced by the source
   * type, it is garbage collected almost immediately after the call to this method
   */
  void registerAsWeakReference(IListenerListWithManagement eventListenerList);

  int getListenerListCount();
}
