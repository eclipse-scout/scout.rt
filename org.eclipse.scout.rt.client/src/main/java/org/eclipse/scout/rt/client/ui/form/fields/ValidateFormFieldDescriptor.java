/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields;

import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.ITabBox;
import org.eclipse.scout.rt.platform.status.IStatus;

/**
 * This interface is used to check fields for valid content and - in case invalid - activate / select / focus the
 * appropriate location
 * <p>
 * see {@link IFormField#validateContent()}
 */
public class ValidateFormFieldDescriptor implements IValidateContentDescriptor {
  private final IFormField m_field;
  private final IStatus m_errorStatus;

  public ValidateFormFieldDescriptor(IFormField field) {
    m_field = field;
    m_errorStatus = field.getErrorStatus();
  }

  @Override
  public String getDisplayText() {
    return m_field.getFullyQualifiedLabel(": ");
  }

  @Override
  public IStatus getErrorStatus() {
    return m_errorStatus;
  }

  @Override
  public void activateProblemLocation() {
    //make sure the table is showing (activate parent tabs)
    IGroupBox g = m_field.getParentGroupBox();
    while (g != null) {
      if (g.getParentField() instanceof ITabBox) {
        ITabBox t = (ITabBox) g.getParentField();
        if (t.getSelectedTab() != g) {
          t.setSelectedTab(g);
        }
      }
      g = g.getParentGroupBox();
    }
    m_field.requestFocus();
  }
}
