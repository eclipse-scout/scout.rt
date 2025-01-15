/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.services.common.context;

import java.io.Serializable;
import java.util.Map;

public class SharedContextChangedNotification implements Serializable {
  private static final long serialVersionUID = 1L;

  private final Map<String, Object> m_sharedVariableMap;

  public SharedContextChangedNotification(Map<String, Object> sharedVariableMap) {
    m_sharedVariableMap = sharedVariableMap;
  }

  public Map<String, Object> getSharedVariableMap() {
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
