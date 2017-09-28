/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
  public boolean accept(PropertyChangeEvent event) {
    return m_acceptEvent;
  }

  @Override
  public String getPropertyName() {
    return m_propertyName;
  }

}
