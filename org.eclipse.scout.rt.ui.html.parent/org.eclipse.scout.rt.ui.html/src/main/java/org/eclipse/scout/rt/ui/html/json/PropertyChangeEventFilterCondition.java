/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

public class PropertyChangeEventFilterCondition {
  private String m_propertyName;
  private Object m_value;

  public PropertyChangeEventFilterCondition(String propertyName, Object value) {
    m_propertyName = propertyName;
    m_value = value;
  }

  public String getPropertyName() {
    return m_propertyName;
  }

  public Object getValue() {
    return m_value;
  }
}
