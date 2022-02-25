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
