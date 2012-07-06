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
package org.eclipse.scout.rt.client.mobile.ui.form.fields;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.scout.commons.beans.IPropertyObserver;

public class PropertyDelegator<SENDER extends IPropertyObserver, RECEIVER extends IPropertyObserver> {
  private P_PropertyChangeListener m_propertyChangeListener;
  private SENDER m_sender;
  private RECEIVER m_receiver;

  public PropertyDelegator(SENDER sender, RECEIVER receiver) {
    m_sender = sender;
    m_receiver = receiver;

    m_propertyChangeListener = new P_PropertyChangeListener();
    m_sender.addPropertyChangeListener(m_propertyChangeListener);
  }

  public SENDER getSender() {
    return m_sender;
  }

  public RECEIVER getReceiver() {
    return m_receiver;
  }

  /**
   * Fills the properties of the receiver with the values of the sender. Typically used at the initialization of objects
   * f.e. at initConfig() of a form field.
   */
  public void init() {
  }

  protected void handlePropertyChange(String name, Object newValue) {
  }

  private class P_PropertyChangeListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      handlePropertyChange(evt.getPropertyName(), evt.getNewValue());
    }

  }
}
