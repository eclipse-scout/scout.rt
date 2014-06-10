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
package org.eclipse.scout.rt.client.ui.basic.table.control;

import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public class AbstractTableControl extends AbstractPropertyObserver implements ITableControl {
  private String m_cssClass;
  private String m_group;

  public AbstractTableControl() {
    this(true);
  }

  public AbstractTableControl(boolean callInitializer) {
    if (callInitializer) {
      callInitializer();
    }
  }

  protected void callInitializer() {
    initConfig();
    setEnabled(true);
  }

  protected void initConfig() {
  }

  protected void execControlActivated() throws ProcessingException {
    if (getForm() == null) {
      IForm form = execStartForm();

      setForm(form);
    }

    setSelected(!isSelected());
  }

  protected IForm execStartForm() throws ProcessingException {
    return null;
  }

  public void setSelected(boolean selected) {
    propertySupport.setPropertyBool(PROP_SELECTED, selected);
  }

  @Override
  public boolean isSelected() {
    return propertySupport.getPropertyBool(PROP_SELECTED);
  }

  public void setEnabled(boolean enabled) {
    propertySupport.setPropertyBool(PROP_ENABLED, enabled);
  }

  @Override
  public boolean isEnabled() {
    return propertySupport.getPropertyBool(PROP_ENABLED);
  }

  @Override
  public String getLabel() {
    return propertySupport.getPropertyString(PROP_LABEL);
  }

  public void setLabel(String label) {
    propertySupport.setPropertyString(PROP_LABEL, label);
  }

  public void setForm(IForm form) {
    propertySupport.setProperty(PROP_FORM, form);
  }

  public void setCssClass(String cssClass) {
    m_cssClass = cssClass;
  }

  @Override
  public String getCssClass() {
    return m_cssClass;
  }

  public void setGroup(String group) {
    m_group = group;
  }

  @Override
  public String getGroup() {
    return m_group;
  }

  // FIXME CGU create ui facade
  @Override
  public void fireActivatedFromUI() {
    try {
      execControlActivated();
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
  }

  @Override
  public IForm getForm() {
    return (IForm) propertySupport.getProperty(PROP_FORM);
  }

}
