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
import org.eclipse.scout.commons.exception.ProcessingException;

public abstract class AbstractJsonRenderer<T extends IPropertyObserver> implements IJsonRenderer {
  private IJsonEnvironment m_jsonEnvironment;
  private T m_scoutObject;
  private P_PropertyChangeListener m_propertyChangeListener;
  private String m_id;

  public AbstractJsonRenderer(T scoutObject, IJsonEnvironment jsonEnvironment) {
    if (scoutObject == null) {
      throw new IllegalArgumentException("scoutObject must not be null");
    }
    m_scoutObject = scoutObject;
    m_jsonEnvironment = jsonEnvironment;
    m_id = jsonEnvironment.createUniqueIdFor(this);
  }

  public IJsonEnvironment getJsonEnvironment() {
    return m_jsonEnvironment;
  }

  public T getScoutObject() {
    return m_scoutObject;
  }

  @Override
  public String getId() {
    return m_id;
  }

  protected void attachScout() throws ProcessingException {
    if (m_propertyChangeListener == null) {
      m_propertyChangeListener = new P_PropertyChangeListener();
      m_scoutObject.addPropertyChangeListener(m_propertyChangeListener);
    }
  }

  protected void detachScout() throws ProcessingException {
    if (m_propertyChangeListener != null) {
      m_scoutObject.removePropertyChangeListener(m_propertyChangeListener);
      m_propertyChangeListener = null;
    }
  }

  @Override
  public void init() throws ProcessingException {
    getJsonEnvironment().registerJsonRenderer(getId(), this);
    attachScout();
  }

  @Override
  public void dispose() throws ProcessingException {
    detachScout();
    getJsonEnvironment().unregisterJsonRenderer(getId());
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
