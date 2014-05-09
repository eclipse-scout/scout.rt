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
package org.eclipse.scout.rt.ui.json;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.beans.IPropertyObserver;

public abstract class AbstractJsonPropertyObserverRenderer<T extends IPropertyObserver> extends AbstractJsonRenderer<T> {

  private P_PropertyChangeListener m_propertyChangeListener;

  private Set<String> m_propertiesToDelegate;

  public AbstractJsonPropertyObserverRenderer(T modelObject, IJsonSession jsonSession) {
    super(modelObject, jsonSession);
    m_propertiesToDelegate = new HashSet<>();
  }

  /**
   * Adds a delegated property. For these properties this class will create JSON property change events automatically.
   * 
   * @param name
   */
  protected void delegateProperty(String name) {
    m_propertiesToDelegate.add(name);
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_propertyChangeListener == null) {
      m_propertyChangeListener = new P_PropertyChangeListener();
      getModelObject().addPropertyChangeListener(m_propertyChangeListener);
    }
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_propertyChangeListener != null) {
      getModelObject().removePropertyChangeListener(m_propertyChangeListener);
      m_propertyChangeListener = null;
    }
  }

  protected void handleModelPropertyChange(String name, Object newValue) {
    if (m_propertiesToDelegate.contains(name)) {
      getJsonSession().currentJsonResponse().addPropertyChangeEvent(getId(), name, newValue);
    }
  }

  private class P_PropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent e) {
      handleModelPropertyChange(e.getPropertyName(), e.getNewValue());
    }
  }
}
