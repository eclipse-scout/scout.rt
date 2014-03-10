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

import org.eclipse.scout.commons.beans.IPropertyObserver;

public abstract class AbstractJsonPropertyObserverRenderer<T extends IPropertyObserver> extends AbstractJsonRenderer<T> implements IJsonRenderer {
  private P_PropertyChangeListener m_propertyChangeListener;

  public AbstractJsonPropertyObserverRenderer(T modelObject, IJsonSession jsonSession) {
    super(modelObject, jsonSession);
  }

  @Override
  protected void attachModel() throws JsonUIException {
    super.attachModel();
    if (m_propertyChangeListener == null) {
      m_propertyChangeListener = new P_PropertyChangeListener();
      getModelObject().addPropertyChangeListener(m_propertyChangeListener);
    }
  }

  @Override
  protected void detachModel() throws JsonUIException {
    super.detachModel();
    if (m_propertyChangeListener != null) {
      getModelObject().removePropertyChangeListener(m_propertyChangeListener);
      m_propertyChangeListener = null;
    }
  }

  protected void handleScoutPropertyChange(String name, Object newValue) {
  }

  private class P_PropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent e) {
      handleScoutPropertyChange(e.getPropertyName(), e.getNewValue());
    }
  }
}
