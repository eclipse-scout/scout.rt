/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
  public boolean accept(PropertyChangeEvent event) {
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
