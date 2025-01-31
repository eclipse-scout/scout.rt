/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop;

import java.beans.PropertyChangeEvent;

import org.eclipse.scout.rt.ui.html.json.IPropertyChangeEventFilterCondition;

/**
 * This simple boolean filter is used to allow or suppress a property change event depending on an external condition.
 */
public class BooleanPropertyChangeFilter implements IPropertyChangeEventFilterCondition {

  private final String m_propertyName;

  private final boolean m_acceptEvent;

  public BooleanPropertyChangeFilter(String propertyName, boolean acceptEvent) {
    m_propertyName = propertyName;
    m_acceptEvent = acceptEvent;
  }

  @Override
  public boolean test(PropertyChangeEvent event) {
    return m_acceptEvent;
  }

  @Override
  public String getPropertyName() {
    return m_propertyName;
  }
}
