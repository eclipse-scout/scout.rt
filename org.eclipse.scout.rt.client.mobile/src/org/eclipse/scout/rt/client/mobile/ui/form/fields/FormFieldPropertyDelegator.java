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

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public class FormFieldPropertyDelegator<SENDER extends IFormField, RECEIVER extends IFormField> {
  private P_PropertyChangeListener m_propertyChangeListener;
  private SENDER m_sendingFormField;
  private RECEIVER m_receivingFormField;

  public FormFieldPropertyDelegator(SENDER sendingField, RECEIVER receivingField) {
    m_sendingFormField = sendingField;
    m_receivingFormField = receivingField;

    m_propertyChangeListener = new P_PropertyChangeListener();
    m_sendingFormField.addPropertyChangeListener(m_propertyChangeListener);
  }

  public SENDER getSendingFormField() {
    return m_sendingFormField;
  }

  public RECEIVER getReceivingFormField() {
    return m_receivingFormField;
  }

  public void init() {
    m_receivingFormField.setVisible(getSendingFormField().isVisible());
    if (!getSendingFormField().isVisible()) {
      //Since AbstractFormField#calculateVisibleInternal may ignore this property only set it if it hasn't been ignored (to not override those rules)
      m_receivingFormField.setVisibleGranted(getSendingFormField().isVisibleGranted());
    }
    m_receivingFormField.setEnabled(getSendingFormField().isEnabled());
    if (!getSendingFormField().isEnabled()) {
      //Since AbstractFormField#calculateEnabled may ignore this property only set it if it hasn't been ignored (to not override those rules)
      m_receivingFormField.setEnabledGranted(getSendingFormField().isEnabledGranted());
    }
    m_receivingFormField.setLabel(getSendingFormField().getLabel());
    m_receivingFormField.setTooltipText(getSendingFormField().getTooltipText());
  }

  protected void handlePropertyChange(String name, Object newValue) {
    if (name.equals(IFormField.PROP_VISIBLE)) {
      m_receivingFormField.setVisible(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IFormField.PROP_ENABLED)) {
      m_receivingFormField.setEnabled(((Boolean) newValue).booleanValue());
    }
    else if (name.equals(IFormField.PROP_LABEL)) {
      m_receivingFormField.setLabel(((String) newValue));
    }
    else if (name.equals(IFormField.PROP_TOOLTIP_TEXT)) {
      m_receivingFormField.setTooltipText((String) newValue);
    }
  }

  private class P_PropertyChangeListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      handlePropertyChange(evt.getPropertyName(), evt.getNewValue());
    }

  }
}
