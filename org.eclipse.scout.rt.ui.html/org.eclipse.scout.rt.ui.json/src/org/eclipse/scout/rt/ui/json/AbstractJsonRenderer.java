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

public abstract class AbstractJsonRenderer<T extends IPropertyObserver> implements IJsonRenderer {
  private final IJsonSession m_jsonSession;
  private final T m_modelObject;
  private final String m_id;
  private P_PropertyChangeListener m_propertyChangeListener;

  public AbstractJsonRenderer(T modelObject, IJsonSession jsonSession) {
    if (modelObject == null) {
      throw new IllegalArgumentException("modelObject must not be null");
    }
    m_modelObject = modelObject;
    m_jsonSession = jsonSession;
    m_id = jsonSession.createUniqueIdFor(this);
  }

  @Override
  public String getId() {
    return m_id;
  }

  protected IJsonSession getJsonSession() {
    return m_jsonSession;
  }

  protected T getModelObject() {
    return m_modelObject;
  }

  @Override
  public void init() throws JsonUIException {
    getJsonSession().registerJsonRenderer(getId(), this);
    attachModel();
  }

  protected void attachModel() throws JsonUIException {
    if (m_propertyChangeListener == null) {
      m_propertyChangeListener = new P_PropertyChangeListener();
      m_modelObject.addPropertyChangeListener(m_propertyChangeListener);
    }
  }

  @Override
  public void dispose() throws JsonUIException {
    detachModel();
    getJsonSession().unregisterJsonRenderer(getId());
  }

  protected void detachModel() throws JsonUIException {
    if (m_propertyChangeListener != null) {
      m_modelObject.removePropertyChangeListener(m_propertyChangeListener);
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
