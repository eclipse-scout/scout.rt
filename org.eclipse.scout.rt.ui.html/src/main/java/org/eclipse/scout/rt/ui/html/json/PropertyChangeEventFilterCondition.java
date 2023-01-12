/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json;

import java.beans.PropertyChangeEvent;

public class PropertyChangeEventFilterCondition implements IPropertyChangeEventFilterCondition {

  private final String m_propertyName;
  private Object m_value;

  public PropertyChangeEventFilterCondition(String propertyName, Object value) {
    m_propertyName = propertyName;
    m_value = value;
  }

  @Override
  public String getPropertyName() {
    return m_propertyName;
  }

  @Override
  public boolean test(PropertyChangeEvent event) {
    Object newValue = event.getNewValue();

    // Ignore if null == null
    if (m_value == null) {
      if (newValue == null) {
        return false;
      }
    }
    // Ignore if value is the same
    else if (m_value.equals(newValue)) {
      return false;
    }

    // When value is not ignored, we update the value to filter
    m_value = newValue;
    return true;
  }
}
