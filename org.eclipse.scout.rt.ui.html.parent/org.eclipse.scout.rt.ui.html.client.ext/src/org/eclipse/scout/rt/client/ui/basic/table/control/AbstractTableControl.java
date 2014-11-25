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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.form.IForm;

public abstract class AbstractTableControl extends AbstractAction implements ITableControl {
  private String m_group;

  public AbstractTableControl() {
    this(true);
  }

  public AbstractTableControl(boolean callInitializer) {
    if (callInitializer) {
      callInitializer();
    }
  }

  protected IForm execStartForm() throws ProcessingException {
    return null;
  }

  public void setForm(IForm form) {
    propertySupport.setProperty(PROP_FORM, form);
  }

  public void setGroup(String group) {
    m_group = group;
  }

  @Override
  public String getGroup() {
    return m_group;
  }

  @Override
  protected boolean getConfiguredToggleAction() {
    return true;
  }

  @Override
  public final IForm getForm() {
    return (IForm) propertySupport.getProperty(PROP_FORM);
  }

  @Override
  protected void execSelectionChanged(boolean selected) throws ProcessingException {
    if (selected && getForm() == null) {
      IForm form = execStartForm();
      if (form != null) {
        setForm(form);
      }
    }
  }

}
