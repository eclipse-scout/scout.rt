/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.services.common.context;

import java.io.Serializable;

public class SharedContextChangedNotification implements Serializable {
  private static final long serialVersionUID = 1L;

  private SharedVariableMap m_sharedVariableMap;

  public SharedContextChangedNotification(SharedVariableMap sharedVariableMap) {
    m_sharedVariableMap = sharedVariableMap;
  }

  public SharedVariableMap getSharedVariableMap() {
    return m_sharedVariableMap;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder(getClass().getSimpleName());
    b.append("[");
    b.append("]");
    return b.toString();
  }

}
