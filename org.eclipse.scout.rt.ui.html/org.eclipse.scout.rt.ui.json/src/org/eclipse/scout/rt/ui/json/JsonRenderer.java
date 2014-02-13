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

public class JsonRenderer<T extends IPropertyObserver> {
  private IJsonEnvironment m_jsonEnvironment;
  private T m_scoutObject;
  private P_PropertyChangeListener m_propertyChangeListener;

  public JsonRenderer(T scoutObject, IJsonEnvironment jsonEnvironment) {
    if (scoutObject == null) {
      throw new IllegalArgumentException("scoutObject must not be null");
    }
    m_scoutObject = scoutObject;
    m_jsonEnvironment = jsonEnvironment;
  }

  public IJsonEnvironment getJsonEnvironment() {
    return m_jsonEnvironment;
  }

  public T getScoutObject() {
    return m_scoutObject;
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

  public void init() throws ProcessingException {
    attachScout();
  }

  public void dispose() throws ProcessingException {
    detachScout();
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
