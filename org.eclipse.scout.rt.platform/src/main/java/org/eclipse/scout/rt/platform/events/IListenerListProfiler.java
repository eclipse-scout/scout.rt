/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
